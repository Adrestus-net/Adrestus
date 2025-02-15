package io.Adrestus.util;

import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.fury.Fury;
import org.apache.fury.ThreadSafeFury;
import org.apache.fury.config.CompatibleMode;
import org.apache.fury.config.Language;
import org.apache.fury.logging.LoggerFactory;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeMap;


public class SerializationFuryUtil {
    private int DEPTH = 130;
    @Getter
    private final ThreadSafeFury fury;
    private final HashSet<Class> classes;
    private final HashSet<String> class_names;
    private final HashSet<Class> ignore_class_names;
    private final ArrayList<Object> toSerialize;
    private static volatile SerializationFuryUtil instance;

    static {
        LoggerFactory.disableLogging();
    }

    private SerializationFuryUtil() throws ClassNotFoundException {
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
                .withRefCopy(false)
                .withClassVersionCheck(true)
                .withCompatibleMode(CompatibleMode.SCHEMA_CONSISTENT)
                .withAsyncCompilation(true)
                .withCodegen(false)
                .requireClassRegistration(false)
                .buildThreadSafeFury();
        this.Setup();
    }

    @SneakyThrows
    public static SerializationFuryUtil getInstance() {

        var result = instance;
        if (result == null) {
            synchronized (SerializationFuryUtil.class) {
                result = instance;
                if (result == null) {
                    result = new SerializationFuryUtil();
                    instance = result;
                }
            }
        }
        return result;
    }

    public SerializationFuryUtil withDataType(Object data) {
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
        this.ignore_class_names.add(ClassLoader.class);
    }
}
