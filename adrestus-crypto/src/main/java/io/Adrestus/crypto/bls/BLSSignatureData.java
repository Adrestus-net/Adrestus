package io.Adrestus.crypto.bls;

import io.Adrestus.crypto.bls.model.Signature;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeNullable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class BLSSignatureData implements Serializable {
    private Signature[] signature;
    private String[] messageHash;


    public BLSSignatureData(int size) {
        this.signature = new Signature[size];
        this.messageHash = new String[size];
    }

    public BLSSignatureData() {
        this.signature = new Signature[2];
        this.messageHash = new String[2];
    }


    @Serialize
    @SerializeNullable
    public String[] getMessageHash() {
        return messageHash;
    }

    public void setMessageHash(String[] messageHash) {
        this.messageHash = messageHash;
    }

    @Serialize
    @SerializeNullable
    public Signature[] getSignature() {
        return signature;
    }

    public void setSignature(Signature[] signature) {
        this.signature = signature;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BLSSignatureData that = (BLSSignatureData) o;
        return Objects.deepEquals(signature, that.signature) && Objects.deepEquals(messageHash, that.messageHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(signature), Arrays.hashCode(messageHash));
    }


    @Override
    public String toString() {
        return "BLSSignatureData{" +
                "signature=" + Arrays.toString(signature) +
                ", messageHash=" + Arrays.toString(messageHash) +
                '}';
    }
}
