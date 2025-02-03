package io.Adrestus.rpc;

import lombok.Getter;

import java.util.HashMap;

public class CachedSerializableErasureObject {

    private static volatile CachedSerializableErasureObject instance;
    @Getter
    private static volatile HashMap<String, byte[]> serializableErasureObject;

    private CachedSerializableErasureObject() {
        // to prevent instantiating by Reflection call
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        serializableErasureObject = new HashMap<>();
    }

    public static CachedSerializableErasureObject getInstance() {
        var result = instance;
        if (result == null) {
            synchronized (CachedSerializableErasureObject.class) {
                result = instance;
                if (result == null) {
                    result = new CachedSerializableErasureObject();
                    instance = result;
                }
            }
        }
        return result;
    }

    public void setSerializableErasureObject(String key, byte[] value) {
        serializableErasureObject.put(key, value);
    }

    public byte[] getSerializableErasureObject(String key) {
        return serializableErasureObject.get(key);
    }

    public void removeSerializableErasureObject(String key) {
        serializableErasureObject.remove(key);
    }

    public boolean containsKey(String key) {
        return serializableErasureObject.containsKey(key);
    }

    public void clear() {
        instance = null;
        serializableErasureObject.clear();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
