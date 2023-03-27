package io.Adrestus.crypto.elliptic;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

import java.io.Serializable;
import java.util.Arrays;

public class ECDSASignatureData implements Serializable {
    private byte v;
    private byte[] r;
    private byte[] s;
    private byte[] pub;


    public ECDSASignatureData() {
        this.v = 0;
        this.r = new byte[0];
        this.s = new byte[0];
        this.pub = new byte[0];
    }

    public ECDSASignatureData(byte v, byte[] r, byte[] s) {
        this.v = v;
        this.r = r;
        this.s = s;
        this.pub = new byte[0];
    }

    public ECDSASignatureData(@Deserialize("v") byte v, @Deserialize("r") byte[] r, @Deserialize("s") byte[] s, @Deserialize("pub") byte[] pub) {
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


    public void setV(byte v) {
        this.v = v;
    }

    public void setR(byte[] r) {
        this.r = r;
    }

    public void setS(byte[] s) {
        this.s = s;
    }

    public void setPub(byte[] pub) {
        this.pub = pub;
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
