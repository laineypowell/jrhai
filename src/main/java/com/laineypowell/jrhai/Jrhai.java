package com.laineypowell.jrhai;


import java.io.IOException;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.system.MemoryUtil.memGetByte;

public final class Jrhai {
    private final Linker linker;

    private final Arena shared;

    private final MethodHandle createEngine;

    private final MethodHandle destroyEngine;

    private final MethodHandle engineRun;

    private final MethodHandle engineSetModuleResolver;

    public Jrhai() throws IOException {
        var path = Paths.get("jrhai.dll");
        Files.deleteIfExists(path);

        if (!Files.isRegularFile(path)) {
            try (var inputStream = Jrhai.class.getClassLoader().getResource("jrhai.dll").openStream()) {
                Files.copy(inputStream, path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        System.load(path.toAbsolutePath().toString());

        linker = Linker.nativeLinker();

        shared = Arena.ofShared();

        var lookup = SymbolLookup.loaderLookup();

        createEngine = get(linker, lookup, "create_engine", FunctionDescriptor.of(ValueLayout.ADDRESS));

        destroyEngine = get(linker, lookup, "destroy_engine", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));

        engineRun = get(linker, lookup, "engine_run", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS));

        engineSetModuleResolver = get(linker, lookup, "engine_set_module_resolver", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    }

    private MethodHandle get(Linker linker, SymbolLookup lookup, String function, FunctionDescriptor descriptor) {
        return linker.downcallHandle(lookup.findOrThrow(function), descriptor);
    }

    public Engine createEngine() throws Throwable {
        return new Engine(this, (MemorySegment) createEngine.invoke());
    }

    public MethodHandle getDestroyEngine() {
        return destroyEngine;
    }

    public MethodHandle getEngineRun() {
        return engineRun;
    }

    public void setEngineSetModuleResolver(MemorySegment engine, ModuleResolver resolver) throws Throwable {
        var obj = new Object() {
            public MemorySegment resolve(long address, long length) {
                try (var arena = Arena.ofConfined()) {
                    var bytes = new byte[(int) (length -= 1)];
                    for (var i = 0; i < length; i++) {
                        bytes[i] = memGetByte(address + i);
                    }

                    return arena.allocateFrom(resolver.resolve(new String(bytes)));
                }
            }

        };

        var target = MethodHandles.lookup().findVirtual(obj.getClass(), "resolve", MethodType.methodType(MemorySegment.class, long.class, long.class)).bindTo(obj);

        engineSetModuleResolver.invoke(engine, linker.upcallStub(target, FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG), shared));
    }

}
