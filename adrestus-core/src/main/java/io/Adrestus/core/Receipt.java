package io.Adrestus.core;

import io.Adrestus.Trie.MerkleProofs;

import java.util.Objects;

public class Receipt {
    private int Height;
    private int ZoneFrom;
    private int ZoneTo;
    private int Position;
    private Transaction transaction;
    private MerkleProofs Proofs;

    public Receipt(int height, int zoneFrom, int zoneTo, int position, Transaction transaction, MerkleProofs proofs) {
        this.Height = height;
        this.ZoneFrom = zoneFrom;
        this.ZoneTo = zoneTo;
        this.Position = position;
        this.transaction = transaction;
        this.Proofs = proofs;
    }

    public int getHeight() {
        return Height;
    }

    public void setHeight(int height) {
        Height = height;
    }

    public int getZoneFrom() {
        return ZoneFrom;
    }

    public void setZoneFrom(int zoneFrom) {
        ZoneFrom = zoneFrom;
    }

    public int getZoneTo() {
        return ZoneTo;
    }

    public void setZoneTo(int zoneTo) {
        ZoneTo = zoneTo;
    }

    public int getPosition() {
        return Position;
    }

    public void setPosition(int position) {
        Position = position;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public MerkleProofs getProofs() {
        return Proofs;
    }

    public void setProofs(MerkleProofs proofs) {
        Proofs = proofs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Receipt receipt = (Receipt) o;
        return Height == receipt.Height && ZoneFrom == receipt.ZoneFrom && ZoneTo == receipt.ZoneTo && Position == receipt.Position && Objects.equals(transaction, receipt.transaction) && Objects.equals(Proofs, receipt.Proofs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Height, ZoneFrom, ZoneTo, Position, transaction, Proofs);
    }

    @Override
    public String toString() {
        return "Receipt{" +
                "Height=" + Height +
                ", ZoneFrom=" + ZoneFrom +
                ", ZoneTo=" + ZoneTo +
                ", Position=" + Position +
                ", transaction=" + transaction +
                ", Proofs=" + Proofs +
                '}';
    }
}
