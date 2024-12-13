package io.Adrestus.crypto.elliptic;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

import java.io.Serializable;
import java.util.Arrays;

public class ECDSASignatureData implements Serializable {
    private final byte v;
    private final byte[] r;
    private final byte[] s;
    private final byte[] pub;
    private final byte[] sig;

    public ECDSASignatureData() {
        this.v = 0;
        this.r = new byte[0];
        this.s = new byte[0];
        this.pub = new byte[0];
        this.sig = new byte[0];
    }

    public ECDSASignatureData(byte[] pub, byte[] sig) {
        this.v = 0;
        this.r = new byte[0];
        this.s = new byte[0];
        this.pub = pub;
        this.sig = sig;
    }

    public ECDSASignatureData(byte v, byte[] r, byte[] s) {
        this.v = v;
        this.r = r;
        this.s = s;
        this.pub = new byte[0];
        this.sig = new byte[0];
    }

    public ECDSASignatureData(byte v, byte[] r, byte[] s, byte[] sig) {
        this.v = v;
        this.r = r;
        this.s = s;
        this.sig = sig;
        this.pub = new byte[0];
    }

    public ECDSASignatureData(@Deserialize("v") byte v, @Deserialize("r") byte[] r, @Deserialize("s") byte[] s, @Deserialize("pub") byte[] pub, @Deserialize("sig") byte[] sig) {
        this.v = v;
        this.r = r;
        this.s = s;
        this.pub = pub;
        this.sig = sig;
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


    @Serialize
    public byte[] getSig() {
        return sig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ECDSASignatureData that = (ECDSASignatureData) o;

        if (v != that.v) {
            return false;
        }
        if (!Arrays.equals(r, that.r)) {
            return false;
        }
        if (!Arrays.equals(sig, that.sig)) {
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

    @Override
    public String toString() {
        return "ECDSASignatureData{" +
                "v=" + v +
                ", r=" + Arrays.toString(r) +
                ", s=" + Arrays.toString(s) +
                ", pub=" + Arrays.toString(pub) +
                ", sig=" + Arrays.toString(sig) +
                '}';
    }
}
