package io.Adrestus.util;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import io.Adrestus.config.AdrestusConfiguration;
import io.activej.codegen.DefiningClassLoader;
import io.activej.serializer.BinarySerializer;
import io.activej.serializer.CompatibilityLevel;
import io.activej.serializer.SerializerFactory;
import io.activej.serializer.def.SerializerDef;
import io.activej.types.scanner.TypeScannerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static java.nio.charset.StandardCharsets.UTF_8;

public class SerializationUtil<T> {
    private static final Logger LOG = LoggerFactory.getLogger(SerializationUtil.class);
    private static final DefiningClassLoader DEFINING_CLASS_LOADER = DefiningClassLoader.create();
    private static final byte[] END_BYTES = "\r\n".getBytes(UTF_8);
    private static Integer[] size;

    private final Class<T> type;
    private final BinarySerializer<T> serializer;
    private final BinarySerializer<List<T>> listSerializer;
    private byte[] buffer;

    static {
        ObjectSizeCalculator.disableAccessWarnings();
        Init();
    }

    public SerializationUtil(Class<T> type) {
        this.type = type;
        this.serializer = SerializerFactory.defaultInstance().create(this.type);
        this.listSerializer = null;
    }


    public SerializationUtil(Type type) {
        this.serializer = SerializerFactory.builder().build().create(type);
        this.listSerializer = null;
        this.type = null;
    }

    public SerializationUtil(Class cls, List<Mapping> list, boolean bool) {
        this.type = cls;
        var factory = SerializerFactory.builder();
        list.forEach(val -> factory.with(val.getType(), val.getSerializerDefMapping()));
        var f = setModelAndGetCorrespondingList2(cls);
        this.listSerializer = factory.build().create(f);
        this.serializer = null;
    }

    public SerializationUtil(Type type, List<Mapping> list) {
        this.type = null;
        var factory = SerializerFactory.builder();
        list.forEach(val -> factory.with(val.getType(), val.getSerializerDefMapping()));
        this.serializer = factory.build().create(type);
        this.listSerializer = null;
    }

    public SerializationUtil(Class<T> cls, List<Mapping> list) {
        this.type = cls;
        var factory = SerializerFactory.builder();
        list.forEach(val -> factory.with(val.getType(), val.getSerializerDefMapping()));
        this.serializer = factory.build().create(cls);
        this.listSerializer = null;
    }


    private static void Init() {
        ArrayList<Integer> list = new ArrayList<>();
        int sum = 0;
        int count = 0;
        int mul = 8;
        list.add(1024);
        while (sum <= AdrestusConfiguration.MAXIMU_BLOCK_SIZE) {
            list.add(list.get(0) * mul);
            sum = list.get(count);
            count++;
            mul = mul + 8;
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

    private static int getIndex(int value) {

        if (value < size[0]) {
            return 0;
        }
        if (value > size[size.length - 1]) {
            return size.length - 1;
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
                return mid;
            }
        }
        // lo == hi + 1
        return lo;
    }

    private int getBuffSize(int buff_size) {
        if (size[0] == buff_size)
            return size[1];
        if (size[1] == buff_size)
            return size[2];

        for (int i = 3; i < size.length; i++) {
            if (size[i] == buff_size) {
                return size[i - 1];
            }
        }
        return size[1];
    }

    public Class getType() {
        return type;
    }


    public synchronized byte[] encode(T value) {
        // int buff_size2 = search((int) (ObjectSizer.retainedSize(value)));
        int buff_size = search((int) (ObjectSizeCalculator.getObjectSize(value)));
        buffer = new byte[buff_size];
        try {
            serializer.encode(buffer, 0, value);
        } catch (ArrayIndexOutOfBoundsException ex) {
            ex.printStackTrace();
            LOG.info(ex.toString());
            int index = getIndex((int) (ObjectSizeCalculator.getObjectSize(value)));
            buff_size = size[index + 1];
            buffer = new byte[buff_size];
            serializer.encode(buffer, 0, value);
        }
        return buffer;
    }

    public synchronized byte[] encodeNotOptimal(T value, int length) {
        // int buff_size2 = search((int) (ObjectSizer.retainedSize(value)));
        int buff_size = search(length);
        buffer = new byte[buff_size];
        serializer.encode(buffer, 0, value);
        return buffer;
    }

    public byte[] encode_list(List<T> value) {
        try {
            int buff_size = search((int) (ObjectSizeCalculator.getObjectSize(value)));
            buffer = new byte[buff_size];
            listSerializer.encode(buffer, 0, value);
            return buffer;
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Error at serialization");
    }


    public synchronized byte[] encode(T value, int size) {
        buffer = new byte[size];
        try {
            serializer.encode(buffer, 0, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buffer;
    }

    public byte[] encode_special(T value, int size) {
        int buff_size = search(size);
        buffer = new byte[buff_size];
        try {
            serializer.encode(buffer, 0, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public synchronized T decode(byte[] buffer) {
        return serializer.decode(buffer, 0);
    }

    public List<T> decode_list(byte[] buffer) {
        return listSerializer.decode(buffer, 0);
    }

    public BinarySerializer<T> getSerializer() {
        return serializer;
    }


    public static int sizeof(Object obj) throws IOException {
        try (var byteOutputStream = new ByteArrayOutputStream();
             var objectOutputStream = new ObjectOutputStream(byteOutputStream)) {
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
            return byteOutputStream.toByteArray().length;
        }
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
