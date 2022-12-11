package io.Adrestus.core;

import com.google.common.base.Objects;
import io.Adrestus.Trie.MerkleProofs;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

import java.util.ArrayList;
import java.util.List;

public class Receipt {
    private int zoneFrom;
    private int zoneTo;
    private List<ReceiptProofs> receiptProofs;

    public Receipt(int zoneFrom,
                   int zoneTo,
                   List<ReceiptProofs> receiptProofs) {
        this.zoneFrom = zoneFrom;
        this.zoneTo = zoneTo;
        this.receiptProofs = receiptProofs;
    }


    public Receipt(@Deserialize("zoneFrom") int zoneFrom, @Deserialize("zoneTo") int zoneTo) {
        this.zoneFrom = zoneFrom;
        this.zoneTo = zoneTo;
        this.receiptProofs = new ArrayList<>();
    }

    public Receipt() {
        this.zoneFrom = 0;
        this.zoneTo = 0;
        this.receiptProofs = new ArrayList<>();
    }

    @Serialize
    public int getZoneFrom() {
        return this.zoneFrom;
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
    public List<ReceiptProofs> getReceiptProofs() {
        return this.receiptProofs;
    }

    public void setReceiptProofs(List<ReceiptProofs> receiptProofs) {
        this.receiptProofs = receiptProofs;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Receipt receipt = (Receipt) o;
        return zoneFrom == receipt.zoneFrom && zoneTo == receipt.zoneTo && Objects.equal(receiptProofs, receipt.receiptProofs);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(zoneFrom, zoneTo, receiptProofs);
    }

    @Override
    public String toString() {
        return "Receipt{" +
                "zoneFrom=" + zoneFrom +
                ", zoneTo=" + zoneTo +
                ", receiptProofs=" + receiptProofs +
                '}';
    }

    public static final class ReceiptProofs {
        private Transaction transaction;
        private int height;
        private int generation;
        private int position;
        private ReceiptData receiptdata;
        private MerkleProofs proofs;

        public ReceiptProofs() {
            this.transaction = new RegularTransaction();
            this.height = 0;
            this.generation = 0;
            this.position = 0;
            this.receiptdata = new ReceiptData();
            this.proofs = new MerkleProofs();
        }

        public ReceiptProofs(@Deserialize("transaction") Transaction transaction,
                             @Deserialize("height") int height,
                             @Deserialize("generation") int generation,
                             @Deserialize("position") int position,
                             @Deserialize("receiptdata") ReceiptData receiptdata,
                             @Deserialize("proofs") MerkleProofs proofs) {
            this.transaction = transaction;
            this.height = height;
            this.generation = generation;
            this.position = position;
            this.receiptdata = receiptdata;
            this.proofs = proofs;
        }

        public ReceiptProofs(Transaction transaction,
                             int height,
                             int generation,
                             int position,
                             ReceiptData receiptdata) {
            this.transaction = transaction;
            this.height = height;
            this.generation = generation;
            this.position = position;
            this.receiptdata = receiptdata;
        }


        @Serialize
        public Transaction getTransaction() {
            return transaction;
        }

        @Serialize
        public MerkleProofs getProofs() {
            return proofs;
        }

        @Serialize
        public int getPosition() {
            return position;
        }

        @Serialize
        public int getHeight() {
            return height;
        }

        @Serialize
        public int getGeneration() {
            return generation;
        }

        @Serialize
        public ReceiptData getReceiptdata() {
            return receiptdata;
        }

        public void setReceiptdata(ReceiptData receiptdata) {
            this.receiptdata = receiptdata;
        }

        public void setProofs(MerkleProofs proofs) {
            this.proofs = proofs;
        }

        public void setTransaction(Transaction transaction) {
            this.transaction = transaction;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public void setGeneration(int generation) {
            this.generation = generation;
        }

        public void setPosition(int position) {
            this.position = position;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ReceiptProofs that = (ReceiptProofs) o;
            return height == that.height && generation == that.generation && position == that.position && Objects.equal(transaction, that.transaction) && Objects.equal(receiptdata, that.receiptdata) && Objects.equal(proofs, that.proofs);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(transaction, height, generation, position, receiptdata, proofs);
        }

        @Override
        public String toString() {
            return "ReceiptProofs{" +
                    "transaction=" + transaction +
                    ", Height=" + height +
                    ", Generation=" + generation +
                    ", position=" + position +
                    ", receiptData=" + receiptdata +
                    ", proofs=" + proofs +
                    '}';
        }
    }

    public static final class ReceiptData {
        private String address;
        private Double amount;

        public ReceiptData() {
            this.address = "";
            this.amount = 0.0;
        }

        public ReceiptData(@Deserialize("address") String address, @Deserialize("amount") Double amount) {
            this.address = address;
            this.amount = amount;
        }

        @Serialize
        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        @Serialize
        public Double getAmount() {
            return amount;
        }

        public void setAmount(Double amount) {
            this.amount = amount;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ReceiptData that = (ReceiptData) o;
            return Objects.equal(address, that.address) && Objects.equal(amount, that.amount);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(address, amount);
        }

        @Override
        public String toString() {
            return "ReceiptData{" +
                    "Address='" + address + '\'' +
                    ", amount=" + amount +
                    '}';
        }
    }


}
