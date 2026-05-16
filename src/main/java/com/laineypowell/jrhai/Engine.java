package com.laineypowell.jrhai;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public final class Engine {
    private final Jrhai jrhai;

    private final MemorySegment engine;

    public Engine(Jrhai jrhai, MemorySegment engine) throws Throwable {
        this.jrhai = jrhai;

        this.engine = engine;
    }

    public void run(String script) throws Throwable {
        try (var arena = Arena.ofConfined()) {
            jrhai.getEngineRun().invoke(engine, arena.allocateFrom(script));
        }
    }

    public void setModuleResolver(ModuleResolver resolver) throws Throwable {
        jrhai.engineSetModuleResolver(engine, resolver);
    }

    public void destroy() throws Throwable {
        jrhai.getDestroyEngine().invoke(engine);
    }

}
