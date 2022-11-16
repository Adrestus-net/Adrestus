package io.Adrestus.util;

import io.activej.serializer.*;
import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.TreeMap;

public class CustomSerializerMap extends SimpleSerializerDef<TreeMap<Double, String>> {
    private int length;
    @Override
    protected BinarySerializer<TreeMap<Double, String>> createSerializer(int version, CompatibilityLevel compatibilityLevel) {
        return new BinarySerializer<TreeMap<Double, String>>() {
            @SneakyThrows
            @Override
            public void encode(BinaryOutput out, TreeMap<Double, String> item) {
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                ObjectOutputStream outstream = new ObjectOutputStream(byteOut);
                outstream.writeObject(item);
                byte[] toWrite=byteOut.toByteArray();
                length=toWrite.length;
                out.write(byteOut.toByteArray());
            }

            @SneakyThrows
            @Override
            public TreeMap<Double, String> decode(BinaryInput in) throws CorruptedDataException {
                byte[] bytes = new byte[length];
                in.read(bytes);
                ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
                ObjectInputStream instream = new ObjectInputStream(byteIn);
                TreeMap<Double, String> treemap = (TreeMap<Double, String>) instream.readObject();
                return treemap;
            }
        };
    }
}
