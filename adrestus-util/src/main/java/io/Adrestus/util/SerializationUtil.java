package io.Adrestus.util;

import io.Adrestus.config.AdrestusConfiguration;
import io.activej.serializer.BinarySerializer;
import io.activej.serializer.SerializerBuilder;

import java.lang.reflect.Type;

public class SerializationUtil<T> {

    private Class type;
    private final BinarySerializer<T> serializer;
    private byte[] buffer;

    public SerializationUtil(Class type) {
        this.type = type;
        serializer = SerializerBuilder.create().build(this.type);
    }

    public SerializationUtil(Type type) {
        serializer = SerializerBuilder.create().build(type);
    }


    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public byte[] encode(T value) {
        buffer = new byte[AdrestusConfiguration.BUFFER_SIZE];
        serializer.encode(buffer, 0, value);
        return buffer;
    }

    public T decode(byte[] buffer) {
        return serializer.decode(buffer, 0);
    }

}
