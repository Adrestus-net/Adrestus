package io.Adrestus.consensus;

import com.google.common.base.Objects;
import io.Adrestus.core.SortSignatureMapByBlsPublicKey;
import io.Adrestus.crypto.bls.BLSSignatureData;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.Signature;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class ConsensusMessage<T> implements Serializable {
    private ConsensusMessageType messageType;
    private ConsensusStatusType statusType;
    private T data;
    private String hash;
    private ChecksumData checksumData;
    private Map<BLSPublicKey, BLSSignatureData> signatures;

    public ConsensusMessage(@Deserialize("data") T data) {
        this.signatures = new TreeMap<BLSPublicKey, BLSSignatureData>(new SortSignatureMapByBlsPublicKey());
        this.checksumData = new ChecksumData();
        this.messageType = ConsensusMessageType.ANNOUNCE;
        this.statusType = ConsensusStatusType.PENDING;
        this.data = data;
        this.hash = "";
    }

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
    public Map<BLSPublicKey, BLSSignatureData> getSignatures() {
        return signatures;
    }

    public void setSignatures(Map<BLSPublicKey, BLSSignatureData> signatures) {
        this.signatures = signatures;
    }

    @Serialize
    public ConsensusMessageType getMessageType() {
        return messageType;
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
        return Objects.hashCode(messageType, statusType, data, checksumData, signatures,hash);
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

    public static class ChecksumData implements Serializable, Cloneable {
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

}
