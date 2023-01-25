package io.Adrestus.core;

import com.google.common.base.Objects;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.Signature;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeNullable;

import java.util.Arrays;

public class SignatureData {
    private Signature[] signature;
    private BLSPublicKey blsPublicKey;

    public SignatureData(Signature[] signature, BLSPublicKey blsPublicKey) {
        this.signature = signature;
        this.blsPublicKey = blsPublicKey;
    }

    public SignatureData(BLSPublicKey blsPublicKey) {
        this.blsPublicKey = blsPublicKey;
        this.signature = new Signature[2];
    }

    public SignatureData() {
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
        SignatureData that = (SignatureData) o;
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
