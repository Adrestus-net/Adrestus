package io.Adrestus.protocol;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class CachedConfigurationProperties {
    private static volatile CachedConfigurationProperties instance;

    private static Properties prop;

    private CachedConfigurationProperties() {
        // to prevent instantiating by Reflection call
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        prop = new Properties();
    }

    public static CachedConfigurationProperties getInstance() {

        var result = instance;
        if (result == null) {
            synchronized (CachedConfigurationProperties.class) {
                result = instance;
                if (result == null) {
                    instance = result = new CachedConfigurationProperties();
                }
            }
        }
        return result;
    }

    public static void setProp(String path) {
        try {
            prop.load(new FileInputStream(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Properties getProp() {
        return prop;
    }
}
