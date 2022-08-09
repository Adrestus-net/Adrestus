package io.Adrestus.consensus;

import com.google.common.base.Objects;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.Signature;

import java.util.List;

public class ConsensusMessage<T> {
    private T data;
    private Signature signature;
    private List<BLSPublicKey> BLSPubKeyList;
    private List<Signature> SignatureList;

    public ConsensusMessage(T data, Signature signature, List<BLSPublicKey> BLSPubKeyList, List<Signature> signatureList) {
        this.data = data;
        this.signature = signature;
        this.BLSPubKeyList = BLSPubKeyList;
        SignatureList = signatureList;
    }

    public ConsensusMessage(T data) {
        this.data = data;
    }

    public ConsensusMessage(Signature signature) {
        this.signature = signature;
    }

    public ConsensusMessage() {
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Signature getSignature() {
        return signature;
    }

    public void setSignature(Signature signature) {
        this.signature = signature;
    }

    public List<BLSPublicKey> getBLSPubKeyList() {
        return BLSPubKeyList;
    }

    public void setBLSPubKeyList(List<BLSPublicKey> BLSPubKeyList) {
        this.BLSPubKeyList = BLSPubKeyList;
    }

    public List<Signature> getSignatureList() {
        return SignatureList;
    }

    public void setSignatureList(List<Signature> signatureList) {
        SignatureList = signatureList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsensusMessage<?> that = (ConsensusMessage<?>) o;
        return Objects.equal(data, that.data) && Objects.equal(signature, that.signature) && Objects.equal(BLSPubKeyList, that.BLSPubKeyList) && Objects.equal(SignatureList, that.SignatureList);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(data, signature, BLSPubKeyList, SignatureList);
    }

    @Override
    public String toString() {
        return "ConsensusMessage{" +
                "data=" + data +
                ", signature=" + signature +
                ", BLSPubKeyList=" + BLSPubKeyList +
                ", SignatureList=" + SignatureList +
                '}';
    }
}
