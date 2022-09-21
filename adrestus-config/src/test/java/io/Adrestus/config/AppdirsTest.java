package io.Adrestus.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;

public class AppdirsTest {

    @Test
    public void AppPathTest() throws IOException {
        String path = Directory.getConfigPath();
        System.out.println(path);
    }
}
