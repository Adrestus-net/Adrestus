package io.Adrestus.crypto.elliptic.mapper;

import io.activej.serializer.*;
import lombok.SneakyThrows;

import java.util.TreeMap;

public class CustomSerializerTreeMap extends SimpleSerializerDef<TreeMap<Object, Object>> {


    @Override
    protected BinarySerializer<TreeMap<Object, Object>> createSerializer(int version, CompatibilityLevel compatibilityLevel) {
        return new BinarySerializer<TreeMap<Object, Object>>() {
            @SneakyThrows
            @Override
            public void encode(BinaryOutput out, TreeMap<Object, Object> item) {
                byte[] bytes = CustomFurySerializer.getInstance().getFury().serialize(item);
                out.writeVarInt(bytes.length);
                out.write(bytes);
            }

            @SneakyThrows
            @Override
            public TreeMap<Object, Object> decode(BinaryInput in) throws CorruptedDataException {
                byte[] bytes = new byte[in.readVarInt()];
                in.read(bytes);
                return (TreeMap<Object, Object>) CustomFurySerializer.getInstance().getFury().deserialize(bytes);
            }
        };
    }
}
