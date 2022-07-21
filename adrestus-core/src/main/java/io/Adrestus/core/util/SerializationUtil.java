package io.Adrestus.core.util;

import io.activej.serializer.BinarySerializer;
import io.activej.serializer.SerializerBuilder;

public class SerializationUtil<T> {

    private Class type;
    private final BinarySerializer<T> serializer;
    private static final byte[] buffer = new byte[1024];

    public SerializationUtil(Class type) {
        this.type = type;
        serializer = SerializerBuilder.create().build(this.type);
    }


    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public byte[] encode(T value) {
        serializer.encode(buffer, 0, value);
        return buffer;
    }

    public T decode(byte[] buffer) {
        return serializer.decode(buffer, 0);
    }

}
