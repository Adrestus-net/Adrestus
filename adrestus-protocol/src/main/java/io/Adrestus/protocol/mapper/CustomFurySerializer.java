package io.Adrestus.protocol.mapper;

import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.fury.Fury;
import org.apache.fury.config.CompatibleMode;
import org.apache.fury.config.Language;
import org.apache.fury.logging.LoggerFactory;

public class CustomFurySerializer {
    @Getter
    private final Fury fury;
    private static volatile CustomFurySerializer instance;

    static {
        LoggerFactory.disableLogging();
    }

    private CustomFurySerializer() throws ClassNotFoundException {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        this.fury = Fury.builder()
                .withLanguage(Language.JAVA)
                .withRefTracking(false)
                .withClassVersionCheck(true)
                .withCompatibleMode(CompatibleMode.SCHEMA_CONSISTENT)
                .withAsyncCompilation(true)
                .withCodegen(false)
                .requireClassRegistration(false)
                .build();
    }

    @SneakyThrows
    public static CustomFurySerializer getInstance() {

        var result = instance;
        if (result == null) {
            synchronized (CustomFurySerializer.class) {
                result = instance;
                if (result == null) {
                    result = new CustomFurySerializer();
                    instance = result;
                }
            }
        }
        return result;
    }
}
