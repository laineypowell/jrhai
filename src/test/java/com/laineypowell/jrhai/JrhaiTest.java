package com.laineypowell.jrhai;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static java.util.Objects.requireNonNull;

public class JrhaiTest {

    @Test
    public void test() throws Throwable {
        var jrhai = new Jrhai();

        var engine = jrhai.createEngine();

        engine.setModuleResolver(new ModuleResolver() {
            @Override
            public String resolve(String path) {
                return readLines(String.format("rhai/%s.rhai", path));
            }
        });
        engine.run(readLines("rhai/example.rhai"));
        engine.destroy();
    }

    public static InputStream open(String path) throws IOException {
        return requireNonNull(Jrhai.class.getClassLoader().getResource(path), String.format("Missing resource %s", path)).openStream();
    }

    public static String readLines(String path) {
        try (var reader = new BufferedReader(new InputStreamReader(open(path)))) {
            var builder = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            return builder.toString();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
