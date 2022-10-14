package io.Adrestus.util;

import sun.misc.Unsafe;

import java.io.FilterOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings({"serial", "deprecation", "rawtypes", "unchecked"})
public class ObjectSizer implements Serializable {

    public static final Unsafe us = getUnsafe();

    public static boolean useCompressedOops = true;

    public static int retainedSize(Object obj) {
        return retainedSize(obj, new HashMap<Object, Object>());
    }


    @SuppressWarnings({"serial", "deprecation", "rawtypes", "unchecked"})
    private static int retainedSize(Object obj, HashMap<Object, Object> calculated) {
        try {
            if (obj == null)
                throw new NullPointerException();
            calculated.put(obj, obj);
            Class<?> cls = obj.getClass();
            if (cls.isArray()) {
                int arraysize = us.arrayBaseOffset(cls) + us.arrayIndexScale(cls) * Array.getLength(obj);
                if (!cls.getComponentType().isPrimitive()) {
                    Object[] arr = (Object[]) obj;
                    for (Object comp : arr) {
                        if (comp != null && !isCalculated(calculated, comp))
                            arraysize += retainedSize(comp, calculated);
                    }
                }
                return arraysize;
            } else {
                int objectsize = sizeof(cls);
                for (Field f : getAllNonStaticFields(obj.getClass())) {
                    Class<?> fcls = f.getType();
                    if (fcls.isPrimitive())
                        continue;
                    f.setAccessible(true);
                    Object ref = f.get(obj);

                    if (ref != null && !isCalculated(calculated, ref)) {
                        int referentSize = retainedSize(ref, calculated);
                        objectsize += referentSize;
                    }
                }

                return objectsize;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public void suppressWarning() throws Exception {
        Field f = FilterOutputStream.class.getDeclaredField("out");
        Runnable r = () -> {
            f.setAccessible(true);
            synchronized (this) {
                this.notify();
            }
        };
        Object errorOutput;
        synchronized (this) {
            synchronized (System.err) //lock System.err to delay the warning
            {
                new Thread(r).start(); //One of these 2 threads will
                new Thread(r).start(); //hang, the other will succeed.
                this.wait(); //Wait 1st thread to end.
                errorOutput = f.get(System.err); //Field is now accessible, set
                f.set(System.err, null); // it to null to suppress the warning

            } //release System.err to allow 2nd thread to complete.
            this.wait(); //Wait 2nd thread to end.
            f.set(System.err, errorOutput); //Restore System.err
        }
    }

    @SuppressWarnings("unchecked")
    public static void disableAccessWarnings() {
        try {
            Class unsafeClass = Class.forName("sun.misc.Unsafe");
            Field field = unsafeClass.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            Unsafe u = (Unsafe) field.get(null);

            Class cls = Class.forName("jdk.internal.module.IllegalAccessLogger");
            Field logger = cls.getDeclaredField("logger");
            u.putObjectVolatile(cls, u.staticFieldOffset(logger), null);
        } catch (Exception ignored) {
        }
    }

    public static void redirectToStdOut() {
        try {

            // get Unsafe
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field field = unsafeClass.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            Object unsafe = field.get(null);

            // get Unsafe's methods
            Method getObjectVolatile = unsafeClass.getDeclaredMethod("getObjectVolatile", Object.class, long.class);
            Method putObject = unsafeClass.getDeclaredMethod("putObject", Object.class, long.class, Object.class);
            Method staticFieldOffset = unsafeClass.getDeclaredMethod("staticFieldOffset", Field.class);
            Method objectFieldOffset = unsafeClass.getDeclaredMethod("objectFieldOffset", Field.class);

            // get information about the global logger instance and warningStream fields
            Class<?> loggerClass = Class.forName("jdk.internal.module.IllegalAccessLogger");
            Field loggerField = loggerClass.getDeclaredField("logger");
            Field warningStreamField = loggerClass.getDeclaredField("warningStream");

            Long loggerOffset = (Long) staticFieldOffset.invoke(unsafe, loggerField);
            Long warningStreamOffset = (Long) objectFieldOffset.invoke(unsafe, warningStreamField);

            // get the global logger instance
            Object theLogger = getObjectVolatile.invoke(unsafe, loggerClass, loggerOffset);
            // replace the warningStream with System.out
            putObject.invoke(unsafe, theLogger, warningStreamOffset, System.out);
        } catch (Throwable ignored) {
        }
    }

    public static int sizeof(Class<?> cls) {

        if (cls == null)
            throw new NullPointerException();

        if (cls.isArray())
            throw new IllegalArgumentException();

        if (cls.isPrimitive())
            return primsize(cls);

        int lastOffset = Integer.MIN_VALUE;
        Class<?> lastClass = null;

        for (Field f : getAllNonStaticFields(cls)) {
            if (Modifier.isStatic(f.getModifiers()))
                continue;

            int offset = (int) us.objectFieldOffset(f);
            if (offset > lastOffset) {
                lastOffset = offset;
                lastClass = f.getClass();
            }
        }
        if (lastOffset > 0)
            return modulo8(lastOffset + primsize(lastClass));
        else
            return 16;
    }

    private static Field[] getAllNonStaticFields(Class<?> cls) {
        if (cls == null)
            throw new NullPointerException();

        List<Field> fieldList = new ArrayList<Field>();
        while (cls != Object.class) {
            for (Field f : cls.getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers()))
                    fieldList.add(f);
            }
            cls = cls.getSuperclass();
        }
        Field[] fs = new Field[fieldList.size()];
        fieldList.toArray(fs);
        return fs;
    }

    private static boolean isCalculated(HashMap<Object, Object> calculated, Object test) {
        Object that = calculated.get(test);
        return that != null && that == test;
    }

    private static int primsize(Class<?> cls) {
        if (cls == byte.class)
            return 1;
        if (cls == boolean.class)
            return 1;
        if (cls == char.class)
            return 2;
        if (cls == short.class)
            return 2;
        if (cls == int.class)
            return 4;
        if (cls == float.class)
            return 4;
        if (cls == long.class)
            return 8;
        if (cls == double.class)
            return 8;
        else
            return useCompressedOops ? 4 : 8;
    }

    private static int modulo8(int value) {
        return (value & 0x7) > 0 ? (value & ~0x7) + 8 : value;
    }

    public static sun.misc.Unsafe UNSAFE;

    public static Unsafe getUnsafe() {
        Object theUnsafe = null;
        Exception exception = null;
        try {
            Class<?> uc = Class.forName("sun.misc.Unsafe");
            Field f = uc.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            theUnsafe = f.get(uc);
        } catch (Exception e) {
            exception = e;
        }
        UNSAFE = (sun.misc.Unsafe) theUnsafe;
        if (UNSAFE == null) throw new Error("Could not obtain access to sun.misc.Unsafe", exception);
        return UNSAFE;
    }
}
