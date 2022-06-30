package io.Adrestus.crypto;


import java.math.BigInteger;
import java.util.Arrays;

import static org.spongycastle.util.BigIntegers.asUnsignedByteArray;

public class EncodeUtil {
    private static final int SIZE_THRESHOLD = 56;
    private static final int OFFSET_SHORT_LIST = 0xc0;
    private static final int OFFSET_LONG_LIST = 0xf7;
    private static final int OFFSET_SHORT_ITEM = 0x80;
    private static final int OFFSET_LONG_ITEM = 0xb7;
    public static byte[] encodeElement(byte[] srcData) {

        // [0x80]
        if (ByteUtil.isNullOrZeroArray(srcData)) {
            return new byte[]{(byte) OFFSET_SHORT_ITEM};

            // [0x00]
        } else if (ByteUtil.isSingleZero(srcData)) {
            return srcData;

            // [0x01, 0x7f] - single byte, that byte is its own RLP encoding
        } else if (srcData.length == 1 && (srcData[0] & 0xFF) < 0x80) {
            return srcData;

            // [0x80, 0xb7], 0 - 55 bytes
        } else if (srcData.length < SIZE_THRESHOLD) {
            // length = 8X
            byte length = (byte) (OFFSET_SHORT_ITEM + srcData.length);
            byte[] data = Arrays.copyOf(srcData, srcData.length + 1);
            System.arraycopy(data, 0, data, 1, srcData.length);
            data[0] = length;

            return data;
            // [0xb8, 0xbf], 56+ bytes
        } else {
            // length of length = BX
            // prefix = [BX, [length]]
            int tmpLength = srcData.length;
            byte lengthOfLength = 0;
            while (tmpLength != 0) {
                ++lengthOfLength;
                tmpLength = tmpLength >> 8;
            }

            // set length Of length at first byte
            byte[] data = new byte[1 + lengthOfLength + srcData.length];
            data[0] = (byte) (OFFSET_LONG_ITEM + lengthOfLength);

            // copy length after first byte
            tmpLength = srcData.length;
            for (int i = lengthOfLength; i > 0; --i) {
                data[i] = (byte) (tmpLength & 0xFF);
                tmpLength = tmpLength >> 8;
            }

            // at last copy the number bytes after its length
            System.arraycopy(srcData, 0, data, 1 + lengthOfLength, srcData.length);

            return data;
        }
    }

    public static byte[] encodeList(byte[]... elements) {

        if (elements == null) {
            return new byte[]{(byte) OFFSET_SHORT_LIST};
        }

        int totalLength = 0;
        for (byte[] element1 : elements) {
            totalLength += element1.length;
        }

        byte[] data;
        int copyPos;
        if (totalLength < SIZE_THRESHOLD) {

            data = new byte[1 + totalLength];
            data[0] = (byte) (OFFSET_SHORT_LIST + totalLength);
            copyPos = 1;
        } else {
            // length of length = BX
            // prefix = [BX, [length]]
            int tmpLength = totalLength;
            byte byteNum = 0;
            while (tmpLength != 0) {
                ++byteNum;
                tmpLength = tmpLength >> 8;
            }
            tmpLength = totalLength;
            byte[] lenBytes = new byte[byteNum];
            for (int i = 0; i < byteNum; ++i) {
                lenBytes[byteNum - 1 - i] = (byte) ((tmpLength >> (8 * i)) & 0xFF);
            }
            // first byte = F7 + bytes.length
            data = new byte[1 + lenBytes.length + totalLength];
            data[0] = (byte) (OFFSET_LONG_LIST + byteNum);
            System.arraycopy(lenBytes, 0, data, 1, lenBytes.length);

            copyPos = lenBytes.length + 1;
        }
        for (byte[] element : elements) {
            System.arraycopy(element, 0, data, copyPos, element.length);
            copyPos += element.length;
        }
        return data;
    }

    public static byte[] encodeBigInteger(BigInteger srcBigInteger) {
        if (srcBigInteger.compareTo(BigInteger.ZERO) < 0) throw new RuntimeException("negative numbers are not allowed");

        if (srcBigInteger.equals(BigInteger.ZERO))
            return encodeByte((byte) 0);
        else
            return encodeElement(asUnsignedByteArray(srcBigInteger));
    }

    public static byte[] encodeByte(byte singleByte) {
        if ((singleByte & 0xFF) == 0) {
            return new byte[]{(byte) OFFSET_SHORT_ITEM};
        } else if ((singleByte & 0xFF) <= 0x7F) {
            return new byte[]{singleByte};
        } else {
            return new byte[]{(byte) (OFFSET_SHORT_ITEM + 1), singleByte};
        }
    }
}
