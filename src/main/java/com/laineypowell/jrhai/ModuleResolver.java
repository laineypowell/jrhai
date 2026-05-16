package com.laineypowell.jrhai;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static org.lwjgl.system.MemoryUtil.memGetByte;

public class ModuleResolver {

    public String resolve(String path) {
        throw new UnsupportedOperationException();
    }

    public MemorySegment resolve(long add, long len) {
        try (var arena = Arena.ofConfined()) {
            var bytes = new byte[(int) (len -= 1)];
            for (var i = 0; i < len; i++) {
                bytes[i] = memGetByte(add + i);
            }

            return arena.allocateFrom(resolve(new String(bytes)));
        }
    }

}
