package com.laineypowell.jrhai;

import org.junit.jupiter.api.Test;

public class JrhaiTest {

    @Test
    public void test() throws Throwable {
        var jrhai = new Jrhai();

        var engine = jrhai.createEngine();

        engine.setModuleResolver(new ModuleResolver() {
            @Override
            public String resolve(String path) {

                return "";
            }
        });
        engine.run("print(40 + 2);");


        engine.destroy();

    }
}
