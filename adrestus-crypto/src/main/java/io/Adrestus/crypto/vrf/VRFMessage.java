package io.Adrestus.crypto.vrf;

import com.google.common.base.Objects;
import io.activej.serializer.annotations.Serialize;
import org.spongycastle.util.encoders.Hex;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class VRFMessage implements Serializable {
    private vrfMessageType type;
    private String BlockHash;
    private byte[] prnd;
    private VRFData data;
    private List<VRFData> signers;

    public VRFMessage() {
        this.type = vrfMessageType.INIT;
        this.BlockHash = "";
        this.prnd = new byte[100];
        Arrays.fill(prnd, (byte) 1);
        this.data = new VRFData();
        this.signers = new ArrayList<VRFData>();
    }

    @Serialize
    public vrfMessageType getType() {
        return type;
    }

    public void setType(vrfMessageType type) {
        this.type = type;
    }

    @Serialize
    public byte[] getPrnd() {
        return prnd;
    }

    public void setPrnd(byte[] prnd) {
        this.prnd = prnd;
    }

    @Serialize
    public VRFData getData() {
        return data;
    }

    public void setData(VRFData data) {
        this.data = data;
    }

    @Serialize
    public List<VRFData> getSigners() {
        return signers;
    }

    public void setSigners(List<VRFData> signers) {
        this.signers = signers;
    }

    @Serialize
    public String getBlockHash() {
        return BlockHash;
    }

    public void setBlockHash(String blockHash) {
        BlockHash = blockHash;
    }

    public enum vrfMessageType {
        INIT, CALCULATE, AGGREGATE, ABORT
    }


    public static class VRFData implements Cloneable, Serializable {
        private byte[] bls_pubkey;
        private byte[] ri;
        private byte[] pi;

        public VRFData() {
            this.bls_pubkey = new byte[100];
            this.ri = new byte[100];
            this.pi = new byte[100];
            Arrays.fill(bls_pubkey, (byte) 1);
            Arrays.fill(ri, (byte) 1);
            Arrays.fill(pi, (byte) 1);
        }

        public VRFData(byte[] bls_pubkey, byte[] ri, byte[] pi) {
            this.bls_pubkey = bls_pubkey;
            this.ri = ri;
            this.pi = pi;
        }

        @Serialize
        public byte[] getBls_pubkey() {
            return bls_pubkey;
        }

        public void setBls_pubkey(byte[] bls_pubkey) {
            this.bls_pubkey = bls_pubkey;
        }

        @Serialize
        public byte[] getRi() {
            return ri;
        }

        public void setRi(byte[] ri) {
            this.ri = ri;
        }

        @Serialize
        public byte[] getPi() {
            return pi;
        }

        public void setPi(byte[] pi) {
            this.pi = pi;
        }

        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VRFData vrfData = (VRFData) o;
            return java.util.Objects.deepEquals(bls_pubkey, vrfData.bls_pubkey) && java.util.Objects.deepEquals(ri, vrfData.ri) && java.util.Objects.deepEquals(pi, vrfData.pi);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(Arrays.hashCode(bls_pubkey), Arrays.hashCode(ri), Arrays.hashCode(pi));
        }

        @Override
        public String toString() {
            return "VRFData{" +
                    "bls_pubkey=" + Hex.toHexString(bls_pubkey) +
                    ", ri=" + Hex.toHexString(ri) +
                    ", pi=" + Hex.toHexString(pi) +
                    '}';
        }
    }


    //NEVER DELETE THIS Arrays.equalS(prnd, message.prnd) SHOULD EXIST
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VRFMessage message = (VRFMessage) o;
        return type == message.type && Objects.equal(BlockHash, message.BlockHash) && Arrays.equals(prnd, message.prnd) && Objects.equal(data, message.data) && Objects.equal(signers, message.signers);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type, BlockHash, prnd, data, signers);
    }

    @Override
    public String toString() {
        return "VRFMessage{" +
                "type=" + type +
                ", BlockHash='" + BlockHash + '\'' +
                ", prnd=" + prnd +
                ", data=" + data +
                ", signers=" + signers +
                '}';
    }
}
