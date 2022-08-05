package io.Adrestus.crypto.elliptic;

import java.util.Arrays;

public class SignatureData {
    private byte v;
    private byte[] r;
    private byte[] s;
    private byte[] pub;


    public SignatureData() {
    }

    public SignatureData(byte v, byte[] r, byte[] s) {
        this.v = v;
        this.r = r;
        this.s = s;
        pub = null;
    }

    public SignatureData(byte v, byte[] r, byte[] s, byte[] pub) {
        this.v = v;
        this.r = r;
        this.s = s;
        this.pub = pub;
    }

    public byte getV() {
        return v;
    }

    public byte[] getR() {
        return r;
    }

    public byte[] getS() {
        return s;
    }

    public byte[] getPub() {
        return pub;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SignatureData that = (SignatureData) o;

        if (v != that.v) {
            return false;
        }
        if (!Arrays.equals(r, that.r)) {
            return false;
        }
        return Arrays.equals(s, that.s);
    }

    @Override
    public int hashCode() {
        int result = (int) v;
        result = 31 * result + Arrays.hashCode(r);
        result = 31 * result + Arrays.hashCode(s);
        result = 63 * result + Arrays.hashCode(pub);
        return result;
    }
}
