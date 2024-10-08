package io.Adrestus.consensus;

import io.Adrestus.core.SortSignatureMapByBlsPublicKey;
import io.Adrestus.crypto.bls.BLSSignatureData;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeNullable;

import java.io.Serializable;
import java.util.TreeMap;

public class ConsensusMessage<T> implements Serializable {
    private ConsensusMessageType messageType;
    private ConsensusStatusType statusType;
    private T data;
    private String hash;
    private ChecksumData checksumData;
    private TreeMap<BLSPublicKey, BLSSignatureData> signatures;

    public ConsensusMessage(@Deserialize("data") T data) {
        this.signatures = new TreeMap<BLSPublicKey, BLSSignatureData>(new SortSignatureMapByBlsPublicKey());
        this.checksumData = new ChecksumData();
        this.messageType = ConsensusMessageType.ANNOUNCE;
        this.statusType = ConsensusStatusType.PENDING;
        this.data = data;
        this.hash = "";
    }

    @SerializeNullable
    @Serialize
    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Serialize
    public ChecksumData getChecksumData() {
        return checksumData;
    }

    public void setChecksumData(ChecksumData checksumData) {
        this.checksumData = checksumData;
    }


    @Serialize
    public ConsensusMessageType getMessageType() {
        return messageType;
    }


    @Serialize
    public TreeMap<BLSPublicKey, BLSSignatureData> getSignatures() {
        return signatures;
    }

    public void setSignatures(TreeMap<BLSPublicKey, BLSSignatureData> signatures) {
        this.signatures = signatures;
    }

    public void setMessageType(ConsensusMessageType messageType) {
        this.messageType = messageType;
    }

    @Serialize
    public ConsensusStatusType getStatusType() {
        return statusType;
    }

    public void setStatusType(ConsensusStatusType statusType) {
        this.statusType = statusType;
    }


    @Serialize
    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void clear() {
        this.signatures.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsensusMessage<?> that = (ConsensusMessage<?>) o;
        return messageType == that.messageType && statusType == that.statusType && java.util.Objects.equals(data, that.data) && java.util.Objects.equals(hash, that.hash) && java.util.Objects.equals(checksumData, that.checksumData) && java.util.Objects.equals(signatures, that.signatures);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(messageType, statusType, data, hash, checksumData, signatures);
    }

    @Override
    public String toString() {
        return "ConsensusMessage{" +
                "messageType=" + messageType +
                ", statusType=" + statusType +
                ", data=" + data +
                ", hash='" + hash + '\'' +
                ", checksumData=" + checksumData +
                ", signatures=" + signatures +
                '}';
    }

}
