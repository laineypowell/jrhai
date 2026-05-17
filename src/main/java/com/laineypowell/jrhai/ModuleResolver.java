package com.laineypowell.jrhai;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class ModuleResolver {

    public String resolve(String path) {
        throw new UnsupportedOperationException();
    }

    public MemorySegment resolve(long add, long len) {
        try (var arena = Arena.ofConfined()) {
            var segment = MemorySegment.ofAddress(add).reinterpret(len);

            var bytes = new byte[(int) (len -= 1)];
            for (var i = 0; i < len; i++) {
                bytes[i] = segment.get(ValueLayout.JAVA_BYTE, i);
            }

            return arena.allocateFrom(resolve(new String(bytes)));
        }
    }

}
