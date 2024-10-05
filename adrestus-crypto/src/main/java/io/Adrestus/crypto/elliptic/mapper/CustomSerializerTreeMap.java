package io.Adrestus.crypto.elliptic.mapper;
import io.activej.serializer.*;
import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.TreeMap;

public class CustomSerializerTreeMap extends SimpleSerializerDef<TreeMap<Object, Object>> {


    @Override
    protected BinarySerializer<TreeMap<Object, Object>> createSerializer(int version, CompatibilityLevel compatibilityLevel) {
        return new BinarySerializer<TreeMap<Object, Object>>() {
            @SneakyThrows
            @Override
            public void encode(BinaryOutput out, TreeMap<Object, Object> item) {
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                ObjectOutputStream outstream = new ObjectOutputStream(byteOut);
                outstream.writeObject(item);
                byte[] bytes = byteOut.toByteArray();
                out.writeVarInt(bytes.length);
                out.write(bytes);
            }

            @SneakyThrows
            @Override
            public TreeMap<Object, Object> decode(BinaryInput in) throws CorruptedDataException {
                byte[] bytes = new byte[in.readVarInt()];
                in.read(bytes);
                ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
                ObjectInputStream instream = new ObjectInputStream(byteIn);
                TreeMap<Object, Object> treemap = (TreeMap<Object, Object>) instream.readObject();
                return treemap;
            }
        };
    }
}
