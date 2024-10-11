package io.Adrestus.crypto.elliptic.mapper;

import io.activej.serializer.*;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class BigDecimalSerializer extends SimpleSerializerDef<BigDecimal> {
    private static final String PATTERN = "\\.";
    private static final String EXISTENCE = ".";
    private static final String LAST_BIT_ZERO_LEFT = "0";
    private static final String LAST_BIT_NO_DECIMAL = "1";

    protected BinarySerializer<BigDecimal> createSerializer(int version, CompatibilityLevel compatibilityLevel) {
        return new BinarySerializer<BigDecimal>() {
            @Override
            public void encode(BinaryOutput out, BigDecimal item) {
                int lef_bit, sum_bit;
                BigInteger theBigInt = item.unscaledValue();
                byte[] bytes = theBigInt.toByteArray();
                String[] splitter = item.toString().split(PATTERN);
                String serial;
                if (!item.toString().contains(EXISTENCE)) {
                    lef_bit = (int) item.toString().chars().filter(Character::isDigit).count();
                    sum_bit = lef_bit;
                    serial = bytes.length + String.valueOf(lef_bit) + String.valueOf(sum_bit) + LAST_BIT_NO_DECIMAL;
                } else {
                    lef_bit = (int) splitter[0].chars().filter(Character::isDigit).count();
                    sum_bit = ((int) theBigInt.toString().chars().filter(Character::isDigit).count());
                    serial = bytes.length + String.valueOf(lef_bit) + String.valueOf(sum_bit);
                }
                if (item.toString().charAt(0) == '0')
                    serial = serial + LAST_BIT_ZERO_LEFT;
                out.writeVarInt(Integer.parseInt(serial));
                out.write(bytes);
            }

            @Override
            public BigDecimal decode(BinaryInput in) throws CorruptedDataException {
                String result = String.valueOf(in.readVarInt());
                int sum_bit = 0, left_bit = 0, length = 0;
                boolean zeroLeft = false;
                boolean noDecimal = false;
                if (result.charAt(result.length() - 1) == LAST_BIT_ZERO_LEFT.toCharArray()[0]) {
                    result = StringUtils.substring(result, 0, result.length() - 1);
                    zeroLeft = true;
                } else if (result.charAt(result.length() - 1) == LAST_BIT_NO_DECIMAL.toCharArray()[0]) {
                    result = StringUtils.substring(result, 0, result.length() - 1);
                    noDecimal = true;
                }

                sum_bit = Integer.parseInt(result.substring(result.length() - 1));
                left_bit = Integer.parseInt(result.substring(result.length() - 2, result.length() - 1));
                length = Integer.parseInt(result.substring(0, result.length() - 2));

                byte[] bytes = new byte[length];
                in.read(bytes);
                BigInteger bi2 = new BigInteger(bytes);

                if (bi2.equals(BigInteger.ZERO))
                    return BigDecimal.ZERO;

                if (zeroLeft)
                    return new BigDecimal(bi2, sum_bit);

                if (noDecimal)
                    return new BigDecimal(bi2);

                if (left_bit == 1 && sum_bit == 1)
                    return new BigDecimal(bi2, 1);
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
