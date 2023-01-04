package io.Adrestus.crypto.elliptic.mapper;

import io.activej.serializer.*;
import org.apache.tuweni.bytes.Bytes;

public class BytesSerializer extends SimpleSerializerDef<Bytes> {

    @Override
    protected BinarySerializer<Bytes> createSerializer(int version, CompatibilityLevel compatibilityLevel) {
        return new BinarySerializer<Bytes>() {
            @Override
            public void encode(BinaryOutput out, Bytes item) {
                out.writeVarInt(item.size());
                out.write(item.toArray());
            }

            @Override
            public Bytes decode(BinaryInput in) throws CorruptedDataException {
                byte[] bytes = new byte[in.readVarInt()];
                in.read(bytes);
                return Bytes.wrap(bytes);
            }
        };
    }
}
