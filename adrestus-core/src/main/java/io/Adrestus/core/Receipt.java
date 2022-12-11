package io.Adrestus.core;

import com.google.common.base.Objects;
import io.Adrestus.Trie.MerkleProofs;
import io.activej.serializer.annotations.Serialize;

import java.util.ArrayList;
import java.util.List;

public class Receipt {
    private int ZoneFrom;
    private int ZoneTo;
    private List<ReceiptProofs> receiptProofs;

    public Receipt(int zoneFrom, int zoneTo, List<ReceiptProofs> receiptProofs) {
        this.ZoneFrom = zoneFrom;
        this.ZoneTo = zoneTo;
        this.receiptProofs = receiptProofs;
    }


    public Receipt(int zoneFrom, int zoneTo) {
        this.ZoneFrom = zoneFrom;
        this.ZoneTo = zoneTo;
        this.receiptProofs = new ArrayList<>();
    }


    @Serialize
    public int getZoneFrom() {
        return ZoneFrom;
    }

    public void setZoneFrom(int zoneFrom) {
        ZoneFrom = zoneFrom;
    }
    @Serialize
    public int getZoneTo() {
        return ZoneTo;
    }

    public void setZoneTo(int zoneTo) {
        ZoneTo = zoneTo;
    }


    @Serialize
    public List<ReceiptProofs> getReceiptProofs() {
        return receiptProofs;
    }

    public void setReceiptProofs(List<ReceiptProofs> receiptProofs) {
        this.receiptProofs = receiptProofs;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Receipt receipt = (Receipt) o;
        return ZoneFrom == receipt.ZoneFrom && ZoneTo == receipt.ZoneTo && Objects.equal(receiptProofs, receipt.receiptProofs);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(ZoneFrom, ZoneTo, receiptProofs);
    }

    @Override
    public String toString() {
        return "Receipt{" +
                "ZoneFrom=" + ZoneFrom +
                ", ZoneTo=" + ZoneTo +
                ", receiptProofs=" + receiptProofs +
                '}';
    }

    public static final class ReceiptProofs{
        private final Transaction transaction;
        private final int Height;
        private final int Generation;
        private final int position;
        private final ReceiptData receiptData;

        private MerkleProofs proofs;

        public ReceiptProofs(Transaction transaction, int height, int generation, int position, ReceiptData receiptData, MerkleProofs proofs) {
            this.transaction = transaction;
            this.Height = height;
            this.Generation = generation;
            this.position = position;
            this.receiptData = receiptData;
            this.proofs = proofs;
        }

        public ReceiptProofs(Transaction transaction,int height, int generation, int position, ReceiptData receiptData) {
            this.transaction = transaction;
            this.Height = height;
            this.Generation = generation;
            this.position = position;
            this.receiptData = receiptData;
        }

        @Serialize
        public ReceiptData getReceiptData() {
            return receiptData;
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
            return Height;
        }
        @Serialize
        public int getGeneration() {
            return Generation;
        }

        public void setProofs(MerkleProofs proofs) {
            this.proofs = proofs;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ReceiptProofs that = (ReceiptProofs) o;
            return Height == that.Height && Generation == that.Generation && position == that.position && Objects.equal(transaction, that.transaction) && Objects.equal(receiptData, that.receiptData) && Objects.equal(proofs, that.proofs);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(transaction, Height, Generation, position, receiptData, proofs);
        }

        @Override
        public String toString() {
            return "ReceiptProofs{" +
                    "transaction=" + transaction +
                    ", Height=" + Height +
                    ", Generation=" + Generation +
                    ", position=" + position +
                    ", receiptData=" + receiptData +
                    ", proofs=" + proofs +
                    '}';
        }
    }

    public static final class ReceiptData{
        private String Address;
        private Double amount;

        public ReceiptData(String address, Double amount) {
            Address = address;
            this.amount = amount;
        }
        @Serialize
        public String getAddress() {
            return Address;
        }

        public void setAddress(String address) {
            Address = address;
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
            return Objects.equal(Address, that.Address) && Objects.equal(amount, that.amount);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(Address, amount);
        }

        @Override
        public String toString() {
            return "ReceiptData{" +
                    "Address='" + Address + '\'' +
                    ", amount=" + amount +
                    '}';
        }
    }


}
