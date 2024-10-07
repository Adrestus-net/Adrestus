package io.Adrestus.consensus;

import com.google.common.base.Objects;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.Signature;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

import java.io.Serializable;

public class ChecksumData implements Serializable, Cloneable {
    private Signature signature;
    private BLSPublicKey blsPublicKey;

    public ChecksumData(@Deserialize("signature") Signature signature, @Deserialize("blsPublicKey") BLSPublicKey blsPublicKey) {
        this.signature = signature;
        this.blsPublicKey = blsPublicKey;
    }

    public ChecksumData() {
        this.signature = new Signature();
        this.blsPublicKey = new BLSPublicKey();
    }

    @Serialize
    public Signature getSignature() {
        return signature;
    }

    public void setSignature(Signature signature) {
        this.signature = signature;
    }

    @Serialize
    public BLSPublicKey getBlsPublicKey() {
        return blsPublicKey;
    }

    public void setBlsPublicKey(BLSPublicKey blsPublicKey) {
        this.blsPublicKey = blsPublicKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChecksumData that = (ChecksumData) o;
        return Objects.equal(signature, that.signature) && Objects.equal(blsPublicKey, that.blsPublicKey);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(signature, blsPublicKey);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "ChecksumData{" +
                "signature=" + signature.getPoint().getValue().toString() +
                ", blsPublicKey=" + blsPublicKey.toString() +
                '}';
    }
}
