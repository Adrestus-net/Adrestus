package io.Adrestus.util;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import io.Adrestus.config.AdrestusConfiguration;
import io.activej.serializer.BinarySerializer;
import io.activej.serializer.SerializerBuilder;
import io.activej.serializer.SerializerDef;
import io.activej.types.scanner.TypeScannerRegistry;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SerializationUtil<T> {


    private static final byte[] END_BYTES = "\r\n".getBytes(UTF_8);
    private Class type;
    private final BinarySerializer<T> serializer;
    private BinarySerializer<List<T>> list_serializer;
    private static byte[] buffer;
    private static Integer size[];

    static {
        ObjectSizeCalculator.disableAccessWarnings();
        Init();
    }

    public SerializationUtil(Class type) {

        this.type = type;
        serializer = SerializerBuilder.create().build(this.type);
    }


    public SerializationUtil(Type type) {
        serializer = SerializerBuilder.create().build(type);
    }

    public SerializationUtil(Class cls, List<Mapping> list, boolean bool) {
        this.serializer = null;
        SerializerBuilder builder = SerializerBuilder.create();
        list.forEach(val -> {
            builder.with(val.type, val.serializerDefMapping);
        });
        Type f = setModelAndGetCorrespondingList2(cls);
        list_serializer = builder.build(f);
    }

    public SerializationUtil(Type type, List<Mapping> list) {
        SerializerBuilder builder = SerializerBuilder.create();
        list.forEach(val -> {
            builder.with(val.type, val.serializerDefMapping);
        });
        serializer = builder.build(type);
    }

    public SerializationUtil(Class clas, List<Mapping> list) {
        SerializerBuilder builder = SerializerBuilder.create();
        list.forEach(val -> {
            builder.with(val.type, val.serializerDefMapping);
        });
        serializer = builder.build(clas);
    }


    private static void Init() {
        ArrayList<Integer> list = new ArrayList<>();
        int sum = 0;
        int count = 0;
        int mul = 4;
        list.add(1024);
        while (sum <= AdrestusConfiguration.MAXIMU_BLOCK_SIZE) {
            list.add(list.get(0) * mul);
            sum = list.get(count);
            count++;
            mul = mul + 4;
        }
        size = list.stream().toArray(Integer[]::new);
    }

    private static int search(int value) {

        if (value < size[0]) {
            return size[0];
        }
        if (value > size[size.length - 1]) {
            return size[size.length - 1];
        }

        int lo = 0;
        int hi = size.length - 1;

        while (lo <= hi) {
            int mid = (hi + lo) / 2;

            if (value < size[mid]) {
                hi = mid - 1;
            } else if (value > size[mid]) {
                lo = mid + 1;
            } else {
                return size[mid];
            }
        }
        // lo == hi + 1
        return size[lo];
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public byte[] encode(T value) {
        // int buff_size = search((int) (ObjectSizer.retainedSize(value)));
        int buff_size = search((int) (ObjectSizeCalculator.getObjectSize(value)));
        buffer = new byte[buff_size];
        serializer.encode(buffer, 0, value);
        return buffer;
    }

    public byte[] encode_list(List<T> value) {
        int buff_size = search((int) (ObjectSizeCalculator.getObjectSize(value)));
        buffer = new byte[buff_size];
        list_serializer.encode(buffer, 0, value);
        return buffer;
    }

    public byte[] encode(T value, int size) {
        buffer = new byte[size];
        serializer.encode(buffer, 0, value);
        byte[] test = trim(buffer);
        return buffer;
    }

    public byte[] encodeWithTerminatedBytes(T value) {
        int buff_size = search((int) (ObjectSizeCalculator.getObjectSize(value)));
        buffer = new byte[buff_size];
        serializer.encode(buffer, 0, value);
        int j = buffer.length - 1;
        for (int i = END_BYTES.length - 1; i > 0; i--) {
            buffer[j] = END_BYTES[i];
            j--;
        }
        return buffer;
    }

    private <T> Type setModelAndGetCorrespondingList2(Class<T> type) {
        return new TypeToken<ArrayList<T>>() {
        }
                .where(new TypeParameter<T>() {
                }, type)
                .getType();
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

    public List<T> decode_list(byte[] buffer) {
        return list_serializer.decode(buffer, 0);
    }

    public BinarySerializer<T> getSerializer() {
        return serializer;
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
