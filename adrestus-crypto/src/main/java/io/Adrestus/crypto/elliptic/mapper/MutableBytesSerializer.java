package io.Adrestus.crypto.elliptic.mapper;

import io.activej.serializer.*;
import org.apache.tuweni.bytes.MutableBytes;

public class MutableBytesSerializer extends SimpleSerializerDef<MutableBytes> {

    @Override
    protected BinarySerializer<MutableBytes> createSerializer(int version, CompatibilityLevel compatibilityLevel) {
        return new BinarySerializer<MutableBytes>() {
            @Override
            public void encode(BinaryOutput out, MutableBytes item) {
                out.writeVarInt(item.size());
                out.write(item.toArray());
            }

            @Override
            public MutableBytes decode(BinaryInput in) throws CorruptedDataException {
                byte[] bytes = new byte[in.readVarInt()];
                in.read(bytes);
                return MutableBytes.wrap(bytes);
            }
        };
    }
}
