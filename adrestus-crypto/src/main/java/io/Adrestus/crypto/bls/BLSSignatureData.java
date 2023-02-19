package io.Adrestus.crypto.bls;

import com.google.common.base.Objects;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.Signature;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeNullable;

import java.io.Serializable;
import java.util.Arrays;

public class BLSSignatureData implements Serializable {
    private Signature[] signature;
    private BLSPublicKey blsPublicKey;

    public BLSSignatureData(Signature[] signature, BLSPublicKey blsPublicKey) {
        this.signature = signature;
        this.blsPublicKey = blsPublicKey;
    }

    public BLSSignatureData(BLSPublicKey blsPublicKey) {
        this.blsPublicKey = blsPublicKey;
        this.signature = new Signature[2];
    }

    public BLSSignatureData() {
        this.blsPublicKey = new BLSPublicKey();
        this.signature = new Signature[2];
    }

    @Serialize
    @SerializeNullable
    public Signature[] getSignature() {
        return signature;
    }

    public void setSignature(Signature[] signature) {
        this.signature = signature;
    }

    @Serialize
    @SerializeNullable
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
        BLSSignatureData that = (BLSSignatureData) o;
        return Objects.equal(signature, that.signature) && Objects.equal(blsPublicKey, that.blsPublicKey);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(signature, blsPublicKey);
    }

    @Override
    public String toString() {
        return "SignatureData{" +
                "signature=" + Arrays.toString(signature) +
                ", blsPublicKey=" + blsPublicKey +
                '}';
    }
}
