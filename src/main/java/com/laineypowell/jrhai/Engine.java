package com.laineypowell.jrhai;

import java.lang.foreign.MemorySegment;

public class Engine {
    private final Jrhai jrhai = new Jrhai();

    private final MemorySegment ptr = jrhai.createEngine();

    public Engine() throws Throwable {
    }

    public void run(String script) throws Throwable {
        jrhai.engineRun(ptr, script);
    }

    public void destroy() throws Throwable {
        jrhai.destroyEngine(ptr);
    }

}
