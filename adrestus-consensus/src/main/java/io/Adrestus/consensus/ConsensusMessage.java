package io.Adrestus.consensus;

import com.google.common.base.Objects;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.Signature;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

import java.util.ArrayList;
import java.util.List;

public class ConsensusMessage<T> {
    private ConsensusMessageType type;
    private T data;
    private ChecksumData checksumData;
    private List<ChecksumData> signatures;


    public ConsensusMessage(@Deserialize("data") T data) {
        this.signatures = new ArrayList<>();
        this.checksumData = new ChecksumData();
        this.type = ConsensusMessageType.ANNOUNCE;
        this.data = data;
    }

    /*public ConsensusMessage() {
        this.checksumData=new ChecksumData();
        this.type=ConsensusMessageType.ANNOUNCE;
        this.signatures = new ArrayList<>();
    }*/

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
    public List<ChecksumData> getSignatures() {
        return signatures;
    }

    public void setSignatures(List<ChecksumData> signatures) {
        this.signatures = signatures;
    }

    @Serialize
    public ConsensusMessageType getType() {
        return type;
    }

    public void setType(ConsensusMessageType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsensusMessage<?> that = (ConsensusMessage<?>) o;
        return type == that.type && Objects.equal(data, that.data) && Objects.equal(checksumData, that.checksumData) && Objects.equal(signatures, that.signatures);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type, data, checksumData, signatures);
    }

    @Override
    public String toString() {
        return "ConsensusMessage{" +
                "type=" + type +
                ", data=" + data +
                ", checksumData=" + checksumData +
                ", signaturelist=" + signatures +
                '}';
    }

    public static class ChecksumData {
        private Signature signature;
        private BLSPublicKey blsPublicKey;

        public ChecksumData(Signature signature, BLSPublicKey blsPublicKey) {
            this.signature = signature;
            this.blsPublicKey = blsPublicKey;
        }

        public ChecksumData() {
        }

        public Signature getSignature() {
            return signature;
        }

        public void setSignature(Signature signature) {
            this.signature = signature;
        }

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
        public String toString() {
            return "ChecksumData{" +
                    "signature=" + signature +
                    ", blsPublicKey=" + blsPublicKey +
                    '}';
        }
    }

}
