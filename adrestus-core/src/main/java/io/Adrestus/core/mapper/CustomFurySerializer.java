package io.Adrestus.core.mapper;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import io.Adrestus.Trie.HashMapDB;
import io.Adrestus.core.comparators.SortSignatureMapByBlsPublicKey;
import io.Adrestus.core.comparators.StakingValueComparator;
import io.Adrestus.crypto.bls.BLSSignatureData;
import io.Adrestus.crypto.bls.model.Signature;
import io.Adrestus.crypto.elliptic.mapper.StakingData;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.fury.Fury;
import org.apache.fury.config.CompatibleMode;
import org.apache.fury.config.Language;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.AccessControlContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeMap;

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
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("org.apache.fury");
        rootLogger.setLevel(ch.qos.logback.classic.Level.OFF);
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
                .withAsyncCompilation(false)
                .withRefTracking(false)
                .withClassVersionCheck(true)
                .withCompatibleMode(CompatibleMode.SCHEMA_CONSISTENT)
                .withAsyncCompilation(true)
                .withCodegen(false)
                .requireClassRegistration(true)
                .build();
        this.Setup();
        this.withDataType(new KademliaData())
                .withDataType(new StakingData())
                .withDataType(new SortSignatureMapByBlsPublicKey())
                .withDataType(new StakingValueComparator())
                .withDataType(new BLSSignatureData())
                .withDataType(new Signature())
                .Construct();
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

    public CustomFurySerializer withDataType(Object data) {
        this.toSerialize.add(data);
        return this;
    }

    @SneakyThrows
    public void Construct() {
        for (Object obj : toSerialize) {
            getMembers(obj.getClass(), DEPTH);
            this.classes.add(obj.getClass());
        }

        for (String name : class_names) {
            try {
                Class<?> act = Class.forName(name);
                this.classes.add(act);
            } catch (ClassNotFoundException e) {
            }
        }

        for (Class val : classes) {
            this.fury.register(val);
        }
        this.fury.register(TreeMap.class);
    }

    private void getMembers(Class c, int depth) throws ClassNotFoundException {
        if (depth < 0)
            return;

        this.class_names.add(c.getName());
        Field[] fields = c.getDeclaredFields();
        for (Field f : fields) {
            if (f.getType().isPrimitive() || f.getType().equals(String.class)) {
            } else if (this.ignore_class_names.contains(f.getType())) {
                this.class_names.add(f.getType().getName());
            } else {
                if (Collection.class.isAssignableFrom(f.getType())) {
                    String s = f.toGenericString();
                    String type = s.split("\\<")[1].split("\\>")[0];
                    Class clazz = Class.forName(type);
                    //System.out.println(c.getSimpleName() + ": " + f.getName() + " is a collection of " + clazz.getSimpleName());
                    this.class_names.add(clazz.getName());
                    this.class_names.add(f.getClass().getName());
                    DEPTH--;
                    getMembers(clazz, DEPTH);
                } else {
                    //System.out.println(c.getSimpleName() + ": " + f.getName() + " is a " + f.getType().getSimpleName());
                    //this.classes.add(c.getClass());
                    this.class_names.add(f.getType().getName());
                    DEPTH--;
                    getMembers(f.getType(), DEPTH);
                }
            }
        }
    }

    private void Setup() {
        this.ignore_class_names.add(BigInteger.class);
        this.ignore_class_names.add(BigDecimal.class);
        this.ignore_class_names.add(ThreadGroup.class);
        this.ignore_class_names.add(HashMapDB.class);
        this.ignore_class_names.add(ClassLoader.class);
        this.ignore_class_names.add(AccessControlContext.class);

    }
}
