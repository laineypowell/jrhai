package com.laineypowell.jrhai;

import java.io.IOException;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Jrhai {
    private final MethodHandle createEngine;

    private final MethodHandle destroyEngine;

    private final MethodHandle engineRun;

    public Jrhai() {
        var path = Paths.get("jrhai.dll");

        if (!Files.isRegularFile(path)) {
            try (var inputStream = Jrhai.class.getClassLoader().getResource("jrhai.dll").openStream()) {
                Files.copy(inputStream, path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        System.load(path.toAbsolutePath().toString());

        var linker = Linker.nativeLinker();

        var lookup = SymbolLookup.loaderLookup();

        createEngine = get(linker, lookup, "create_engine", FunctionDescriptor.of(ValueLayout.ADDRESS));

        destroyEngine = get(linker, lookup, "destroy_engine", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));

        engineRun = get(linker, lookup, "engine_run", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    }

    private MethodHandle get(Linker linker, SymbolLookup lookup, String function, FunctionDescriptor descriptor) {
        return linker.downcallHandle(lookup.findOrThrow(function), descriptor);
    }

    public MemorySegment createEngine() throws Throwable {
        return (MemorySegment) createEngine.invoke();
    }

    public void destroyEngine(MemorySegment ptr) throws Throwable {
        destroyEngine.invoke(ptr);
    }

    public void engineRun(MemorySegment ptr, String script) throws Throwable {
        try (var arena = Arena.ofConfined()) {
            engineRun.invoke(ptr, arena.allocateFrom(script));
        }
    }

}
