package io.Adrestus.util;

import io.Adrestus.config.AdrestusConfiguration;
import io.activej.serializer.BinarySerializer;
import io.activej.serializer.SerializerBuilder;
import io.activej.serializer.SerializerDef;
import io.activej.types.scanner.TypeScannerRegistry;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

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

    public SerializationUtil(Class clas, List<Mapping> list) {
        SerializerBuilder builder = SerializerBuilder.create();
        list.forEach(val -> {
            builder.with(val.type, val.serializerDefMapping);
        });
        serializer = builder.build(clas);
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

    public byte[] encode(T value, int size) {
        buffer = new byte[size];
        serializer.encode(buffer, 0, value);
        byte[]test=trim(buffer);
        return buffer;
    }

    public static byte[] trim(byte[] data) {
        int idx = 0;
        while (idx < data.length) {
            if (data[idx] != 0) {
                break;
            }
            idx++;
        }
        byte[] trimmedData;
        if (idx > 0 && idx < data.length) {
            trimmedData = Arrays.copyOfRange(data, idx, data.length);
        } else {
            trimmedData = data;
        }
        return trimmedData;
    }

    public T decode(byte[] buffer) {
        return serializer.decode(buffer, 0);
    }


    public static final class Mapping {
        private final Type type;
        private final TypeScannerRegistry.Mapping<SerializerDef> serializerDefMapping;

        public Mapping(Type type, TypeScannerRegistry.Mapping<SerializerDef> serializerDefMapping) {
            this.type = type;
            this.serializerDefMapping = serializerDefMapping;
        }

        public Type getType() {
            return type;
        }

        public TypeScannerRegistry.Mapping<SerializerDef> getSerializerDefMapping() {
            return serializerDefMapping;
        }
    }
}
