package com.laineypowell.jrhai;

import org.junit.jupiter.api.Test;

public class JrhaiTest {

    @Test
    public void test() throws Throwable {
        var engine = new Engine();
        engine.run("print(40 + 2);");
        engine.destroy();

    }
}
