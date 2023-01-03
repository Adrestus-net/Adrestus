package io.Adrestus.mapper;

import io.Adrestus.MemoryTreePool;
import io.activej.serializer.*;
import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class MemoryTreePoolSerializer extends SimpleSerializerDef<MemoryTreePool> {

    @Override
    protected BinarySerializer<MemoryTreePool> createSerializer(int version, CompatibilityLevel compatibilityLevel) {
        return new BinarySerializer<MemoryTreePool>() {
            @SneakyThrows
            @Override
            public void encode(BinaryOutput out, MemoryTreePool item) {
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                ObjectOutputStream outstream = new ObjectOutputStream(byteOut);
                outstream.writeObject(item);
                byte[] bytes = byteOut.toByteArray();
                out.writeVarInt(bytes.length);
                out.write(bytes);
            }

            @SneakyThrows
            @Override
            public MemoryTreePool decode(BinaryInput in) throws CorruptedDataException {
                byte[] bytes = new byte[in.readVarInt()];
                in.read(bytes);
                ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
                ObjectInputStream instream = new ObjectInputStream(byteIn);
                MemoryTreePool tree = (MemoryTreePool) instream.readObject();
                return tree;
            }
        };
    }
}
