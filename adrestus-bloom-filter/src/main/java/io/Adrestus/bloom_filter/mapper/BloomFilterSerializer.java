package io.Adrestus.bloom_filter.mapper;

import io.Adrestus.bloom_filter.BloomFilter;
import io.activej.serializer.*;
import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class BloomFilterSerializer extends SimpleSerializerDef<BloomFilter> {
    @Override
    protected BinarySerializer<BloomFilter> createSerializer(int version, CompatibilityLevel compatibilityLevel) {
        return new BinarySerializer<BloomFilter>() {
            @SneakyThrows
            @Override
            public void encode(BinaryOutput out, BloomFilter item) {
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                ObjectOutputStream outstream = new ObjectOutputStream(byteOut);
                outstream.writeObject(item);

                byte[] bytes = byteOut.toByteArray();
                out.writeVarInt(bytes.length);
                out.write(bytes);
            }

            @SneakyThrows
            @Override
            public BloomFilter decode(BinaryInput in) throws CorruptedDataException {
                byte[] bytes = new byte[in.readVarInt()];
                in.read(bytes);
                ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
                ObjectInputStream instream = new ObjectInputStream(byteIn);
                BloomFilter item = (BloomFilter) instream.readObject();
                return item;
            }
        };
    }
}
