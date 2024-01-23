package io.Adrestus.bloom_filter.Util;

import java.nio.ByteBuffer;
import java.util.BitSet;

public class UtilBloomFilter {

    public static long[] convertByteArrayToLongArray(byte[] bytes) {
        if (bytes == null)
            return null;

        int count = bytes.length / 8;
        long[] longArray = new long[count];
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        for (int i = 0; i < count; i++) {
            longArray[i] = byteBuffer.getLong();
        }
        return longArray;
    }

    public static int[] bits2Ints(BitSet bs) {
        int[] temp = new int[bs.size()];

        for (int i = 0; i < temp.length; i++)
            if (bs.get(i))
                temp[i] = 1;
            else
                temp[i] = 0;

        return temp;
    }

    public static BitSet Ints2Bits(int arr[]) {
        BitSet temp = new BitSet(arr.length);
        for (int i = 0; i < arr.length; i++)
            temp.set(arr[i]);

        return temp;
    }
}
