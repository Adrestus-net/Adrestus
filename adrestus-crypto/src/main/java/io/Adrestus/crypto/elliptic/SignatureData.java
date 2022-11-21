package io.Adrestus.crypto.elliptic;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

import java.io.Serializable;
import java.util.Arrays;

public class SignatureData implements Serializable {
    private byte v;
    private byte[] r;
    private byte[] s;
    private byte[] pub;


    public SignatureData() {
        this.v = 0;
        this.r = new byte[0];
        this.s = new byte[0];
        this.pub = new byte[0];
    }

    public SignatureData(byte v, byte[] r, byte[] s) {
        this.v = v;
        this.r = r;
        this.s = s;
        this.pub = new byte[0];
    }

    public SignatureData(@Deserialize("v") byte v, @Deserialize("r") byte[] r, @Deserialize("s") byte[] s, @Deserialize("pub") byte[] pub) {
        this.v = v;
        this.r = r;
        this.s = s;
        this.pub = pub;
    }

    @Serialize
    public byte getV() {
        return v;
    }

    @Serialize
    public byte[] getR() {
        return r;
    }

    @Serialize
    public byte[] getS() {
        return s;
    }

    @Serialize
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
