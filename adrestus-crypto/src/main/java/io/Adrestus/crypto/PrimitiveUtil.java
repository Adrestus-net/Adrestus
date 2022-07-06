package io.Adrestus.crypto;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

public class PrimitiveUtil {
    private static final String HEX_PREFIX = "0x";

    public static byte[] concatByteArrays(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public static char[] bytesToBinaryAsChars(byte[] bytes) {
        StringBuilder binaryStringBuilder = new StringBuilder();
        for (byte b : bytes) {
            binaryStringBuilder.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
        int binaryLength = binaryStringBuilder.length();
        char[] binaryChars = new char[binaryLength];
        binaryStringBuilder.getChars(0, binaryLength, binaryChars, 0);
        return binaryChars;
    }

    public static byte[] byteSubArray(byte[] source, int startIndex, int endIndex) {
        byte[] subArray = new byte[endIndex - startIndex];
        System.arraycopy(source, startIndex, subArray, 0, endIndex - startIndex);
        return subArray;
    }

    public static char[] charSubArray(char[] source, int startIndex, int endIndex) {
        char[] subArray = new char[endIndex - startIndex];
        System.arraycopy(source, startIndex, subArray, 0, endIndex - startIndex);
        return subArray;
    }

    public static byte[] last4BytesFromLong(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(x);
        return byteSubArray(buffer.array(), 4, 8);
    }

    public static int binaryCharsToInt(char[] binary) {
        int result = 0;
        for (int i = binary.length - 1; i >= 0; i--)
            if (binary[i] == '1')
                result += Math.pow(2, (binary.length - i - 1));
        return result;
    }

    public static byte[] toBytes(char[] chars) {

        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(charBuffer.array(), '\u0000'); // clear sensitive data
        Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
        return bytes;
    }

    public static char[] concatCharArrays(char[] a, char[] b) {

        char[] c = new char[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public static String cleanHexPrefix(String input) {
        if (containsHexPrefix(input)) {
            return input.substring(2);
        } else {
            return input;
        }
    }

    public static String prependHexPrefix(String input) {
        if (!containsHexPrefix(input)) {
            return HEX_PREFIX + input;
        } else {
            return input;
        }
    }

    public static boolean containsHexPrefix(String input) {
        return !isEmpty(input)
                && input.length() > 1
                && input.charAt(0) == '0'
                && input.charAt(1) == 'x';
    }

    public static BigInteger toBigInt(byte[] value) {
        return new BigInteger(1, value);
    }

    public static BigInteger toBigInt(String hexValue) {
        String cleanValue = cleanHexPrefix(hexValue);
        return toBigIntNoPrefix(cleanValue);
    }

    public static BigInteger toBigInt(byte[] value, int offset, int length) {
        return toBigInt((Arrays.copyOfRange(value, offset, offset + length)));
    }

    public static BigInteger toBigIntNoPrefix(String hexValue) {
        return new BigInteger(hexValue, 16);
    }

    public static String toHexStringWithPrefix(BigInteger value) {
        return HEX_PREFIX + value.toString(16);
    }

    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static byte[] toBytesPadded(BigInteger value, int length) {
        byte[] result = new byte[length];
        byte[] bytes = value.toByteArray();

        int bytesLength;
        int srcOffset;
        if (bytes[0] == 0) {
            bytesLength = bytes.length - 1;
            srcOffset = 1;
        } else {
            bytesLength = bytes.length;
            srcOffset = 0;
        }

        if (bytesLength > length) {
            throw new RuntimeException("Input is too large to put in byte array of size " + length);
        }

        int destOffset = length - bytesLength;
        System.arraycopy(bytes, srcOffset, result, destOffset, bytesLength);
        return result;
    }

    public static byte[] concat(final byte[]... arrays) {
        int totalSize = 0;
        for (final byte[] array : arrays) {
            totalSize += array.length;
        }

        int startIndex = 0;
        final byte[] result = new byte[totalSize];
        for (final byte[] array : arrays) {
            System.arraycopy(array, 0, result, startIndex, array.length);
            startIndex += array.length;
        }

        return result;
    }
}
