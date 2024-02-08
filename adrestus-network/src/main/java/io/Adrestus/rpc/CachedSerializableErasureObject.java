package io.Adrestus.rpc;

import java.util.Objects;

public class CachedSerializableErasureObject<V> {

    private static volatile CachedSerializableErasureObject instance;

    private CachedSerializableErasureObject() {
        // to prevent instantiating by Reflection call
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
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

    private V SerializableErasureObject;

    public V getSerializableErasureObject() {
        return SerializableErasureObject;
    }

    public void setSerializableErasureObject(V serializableErasureObject) {
        SerializableErasureObject = serializableErasureObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CachedSerializableErasureObject<?> that = (CachedSerializableErasureObject<?>) o;
        return Objects.equals(SerializableErasureObject, that.SerializableErasureObject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(SerializableErasureObject);
    }
}
