package io.Adrestus.crypto.elliptic.mapper;

import io.activej.serializer.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

public class BigDecimalSerializer extends SimpleSerializerDef<BigDecimal> {

    protected BinarySerializer<BigDecimal> createSerializer(int version, CompatibilityLevel compatibilityLevel) {
        return new BinarySerializer<BigDecimal>() {
            @Override
            public void encode(BinaryOutput out, BigDecimal item) {
                byte[] bytes = item.toString().getBytes();
                out.writeVarInt(bytes.length);
                out.write(bytes);
            }

            @Override
            public BigDecimal decode(BinaryInput in) throws CorruptedDataException {
                byte[] bytes = new byte[in.readVarInt()];
                in.read(bytes);
                return new BigDecimal(new String(bytes, StandardCharsets.UTF_8));
            }
        };
    }
}
