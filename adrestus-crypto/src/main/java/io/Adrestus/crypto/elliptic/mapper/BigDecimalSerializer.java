package io.Adrestus.crypto.elliptic.mapper;

import io.activej.serializer.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class BigDecimalSerializer extends SimpleSerializerDef<BigDecimal> {
    private static final String PATTERN = "\\.";

    protected BinarySerializer<BigDecimal> createSerializer(int version, CompatibilityLevel compatibilityLevel) {
        return new BinarySerializer<BigDecimal>() {
            @Override
            public void encode(BinaryOutput out, BigDecimal item) {
                BigInteger theBigInt = item.unscaledValue();
                byte[] bytes = theBigInt.toByteArray();
                int lef_bit = (int) item.toString().split(PATTERN)[0].chars().filter(Character::isDigit).count();
                int sum_bit = ((int) theBigInt.toString().chars().filter(Character::isDigit).count());
                String serial = bytes.length + String.valueOf(lef_bit) + String.valueOf(sum_bit);
                out.writeVarInt(Integer.parseInt(serial));
                out.write(bytes);
            }

            @Override
            public BigDecimal decode(BinaryInput in) throws CorruptedDataException {
                String result = String.valueOf(in.readVarInt());
                int sum_bit = Integer.parseInt(result.substring(result.length() - 1));
                int left_bit = Integer.parseInt(result.substring(result.length() - 2, result.length() - 1));
                int length = Integer.parseInt(result.substring(result.length() - 3, result.length() - 2));
                byte[] bytes = new byte[length];
                in.read(bytes);
                BigInteger bi2 = new BigInteger(bytes);
                return new BigDecimal(bi2, (sum_bit - left_bit - left_bit) + left_bit);
            }
        };
    }

    static public String bytesToString(byte[] buffer) {
        return bytesToString(buffer, 0, buffer.length);
    }

    static public String bytesToString(byte[] buffer, int index, int length) {
        return new String(buffer, index, length, StandardCharsets.UTF_8);
    }
}
