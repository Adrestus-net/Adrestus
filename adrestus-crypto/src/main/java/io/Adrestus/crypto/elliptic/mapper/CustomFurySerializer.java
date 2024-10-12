package io.Adrestus.crypto.elliptic.mapper;

import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.fury.Fury;
import org.apache.fury.config.CompatibleMode;
import org.apache.fury.config.Language;
import org.apache.fury.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;

public class CustomFurySerializer {
    private int DEPTH = 130;
    @Getter
    private final Fury fury;
    private final HashSet<Class> classes;
    private final HashSet<String> class_names;
    private final HashSet<Class> ignore_class_names;
    private final ArrayList<Object> toSerialize;
    private static volatile CustomFurySerializer instance;

    static {
        LoggerFactory.disableLogging();
    }

    private CustomFurySerializer() throws ClassNotFoundException {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        this.classes = new HashSet<Class>();
        this.class_names = new HashSet<String>();
        this.ignore_class_names = new HashSet<>();
        this.toSerialize = new ArrayList<>();
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
