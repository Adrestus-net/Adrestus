package io.Adrestus.crypto.elliptic.mapper;

import io.activej.serializer.*;
import org.apache.tuweni.bytes.Bytes32;


public class Bytes32Serializer extends SimpleSerializerDef<Bytes32> {

    @Override
    protected BinarySerializer<Bytes32> createSerializer(int version, CompatibilityLevel compatibilityLevel) {
        return new BinarySerializer<Bytes32>() {
            @Override
            public void encode(BinaryOutput out, Bytes32 item) {
                out.writeVarInt(item.size());
                out.write(item.toArray());
            }

            @Override
            public Bytes32 decode(BinaryInput in) throws CorruptedDataException {
                byte[] bytes = new byte[in.readVarInt()];
                in.read(bytes);
                return Bytes32.wrap(bytes);
            }
        };
    }

}
