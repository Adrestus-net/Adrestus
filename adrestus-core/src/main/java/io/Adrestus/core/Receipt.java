package io.Adrestus.core;

import io.Adrestus.Trie.MerkleProofs;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeNullable;

import java.io.Serializable;
import java.util.Objects;

public class Receipt implements Serializable, Cloneable {

    private int zoneFrom;
    private int zoneTo;
    private ReceiptBlock receiptBlock;
    private int position;
    private MerkleProofs proofs;


    public Receipt() {
        this.zoneFrom = 0;
        this.zoneTo = 0;
        this.receiptBlock = new ReceiptBlock();
        this.position = 0;
        this.proofs = new MerkleProofs();
    }

    public Receipt(int zoneFrom,
                   int zoneTo,
                   ReceiptBlock receiptBlock,
                   int position,
                   MerkleProofs proofs) {
        this.zoneFrom = zoneFrom;
        this.zoneTo = zoneTo;
        this.receiptBlock = receiptBlock;
        this.position = position;
        this.proofs = proofs;

    }

    public Receipt(int zoneFrom, int zoneTo, int position, MerkleProofs proof) {
        this.zoneFrom = zoneFrom;
        this.zoneTo = zoneTo;
        this.position = position;
        this.proofs = proofs;
    }

    public Receipt(int zoneFrom, int zoneTo) {
        this.zoneFrom = zoneFrom;
        this.zoneTo = zoneTo;
        this.proofs = new MerkleProofs();
        this.position = 0;
    }

    public Receipt(@Deserialize("zoneFrom") int zoneFrom,
                   @Deserialize("zoneTo") int zoneTo,
                   @Deserialize("receiptBlock") ReceiptBlock receiptBlock,
                   @Deserialize("proofs") MerkleProofs proofs,
                   @Deserialize("position") int position) {
        this.zoneFrom = zoneFrom;
        this.zoneTo = zoneTo;
        this.receiptBlock = receiptBlock;
        this.proofs = proofs;
        this.position = position;
    }

    public Receipt(int zoneFrom,
                   int zoneTo,
                   ReceiptBlock receiptBlock,
                   String transaction_hash) {
        this.zoneFrom = zoneFrom;
        this.zoneTo = zoneTo;
        this.receiptBlock = receiptBlock;
        this.proofs = new MerkleProofs();
        this.position = 0;
    }


    @Serialize
    public int getZoneFrom() {
        return zoneFrom;
    }

    public void setZoneFrom(int zoneFrom) {
        this.zoneFrom = zoneFrom;
    }

    @Serialize
    public int getZoneTo() {
        return zoneTo;
    }

    public void setZoneTo(int zoneTo) {
        this.zoneTo = zoneTo;
    }

    @Serialize
    @SerializeNullable
    public ReceiptBlock getReceiptBlock() {
        return receiptBlock;
    }

    public void setReceiptBlock(ReceiptBlock receiptBlock) {
        this.receiptBlock = receiptBlock;
    }


    @Serialize
    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Serialize
    @SerializeNullable
    public MerkleProofs getProofs() {
        return proofs;
    }

    public void setProofs(MerkleProofs proofs) {
        this.proofs = proofs;
    }

    public static Receipt merge(Receipt receipt) {
        return new Receipt(
                receipt.getZoneFrom(),
                receipt.getZoneTo(),
                null,
                receipt.getPosition(),
                receipt.getProofs());
    }


    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Receipt receipt = (Receipt) object;
        return zoneFrom == receipt.zoneFrom && zoneTo == receipt.zoneTo && position == receipt.position && Objects.equals(receiptBlock, receipt.receiptBlock) && Objects.equals(proofs, receipt.proofs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(zoneFrom, zoneTo, receiptBlock, position, proofs);
    }

    @Override
    public String toString() {
        return "Receipt{" +
                "zoneFrom=" + zoneFrom +
                ", zoneTo=" + zoneTo +
                ", receiptBlock=" + receiptBlock +
                ", position=" + position +
                ", proofs=" + proofs +
                '}';
    }

    public static final class ReceiptBlock implements Serializable {
        private int height;
        private int generation;
        private String outboundMerkleRoot;


        public ReceiptBlock() {
            this.height = 0;
            this.generation = 0;
            this.outboundMerkleRoot = "";
        }

        public ReceiptBlock(int height, int generation, String outboundMerkleRoot) {
            this.height = height;
            this.generation = generation;
            this.outboundMerkleRoot = outboundMerkleRoot;
        }

        @Serialize
        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        @Serialize
        public int getGeneration() {
            return generation;
        }

        public void setGeneration(int generation) {
            this.generation = generation;
        }

        @Serialize
        public String getOutboundMerkleRoot() {
            return outboundMerkleRoot;
        }

        public void setOutboundMerkleRoot(String outboundMerkleRoot) {
            this.outboundMerkleRoot = outboundMerkleRoot;
        }


        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            ReceiptBlock that = (ReceiptBlock) object;
            return height == that.height && generation == that.generation && Objects.equals(outboundMerkleRoot, that.outboundMerkleRoot);
        }

        @Override
        public int hashCode() {
            return Objects.hash(height, generation, outboundMerkleRoot);
        }

        @Override
        public String toString() {
            return "ReceiptBlock{" +
                    "height=" + height +
                    ", generation=" + generation +
                    ", outboundMerkleRoot='" + outboundMerkleRoot + '\'' +
                    '}';
        }
    }

}