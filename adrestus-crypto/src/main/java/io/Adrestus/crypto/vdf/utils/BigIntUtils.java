package io.Adrestus.crypto.vdf.utils;

import java.math.BigInteger;

public class BigIntUtils {

    public static BigInteger createBigInteger(int i) {
        byte[] buff = new byte[]{
                (byte) (i >> 24),
                (byte) (i >> 16),
                (byte) (i >> 8),
                (byte) i
        };

        return new BigInteger(buff);
    }

    public static BigInteger createBigInteger(long q) {
        byte[] buff = new byte[]{
                (byte) (q >> 56),
                (byte) (q >> 48),
                (byte) (q >> 40),
                (byte) (q >> 32),
                (byte) (q >> 24),
                (byte) (q >> 16),
                (byte) (q >> 8),
                (byte) q
        };
        return new BigInteger(buff);
    }

    public static BigInteger createBigInteger(byte[] buf, int offset, int len) {
        byte[] tmp = new byte[len];
        System.arraycopy(buf, offset, tmp, 0, len);
        return new BigInteger(tmp);
    }

    public static BigInteger createBigInteger(int signum, byte[] buf, int offset, int len) {
        byte[] tmp = new byte[len];
        System.arraycopy(buf, offset, tmp, 0, len);
        return new BigInteger(signum, tmp);
    }
}
