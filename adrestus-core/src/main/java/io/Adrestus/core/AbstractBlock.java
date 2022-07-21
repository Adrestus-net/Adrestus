package io.Adrestus.core;

import java.sql.Timestamp;
import java.util.Objects;

public abstract class AbstractBlock {
    private String Hash;
    private String PreviousHash;
    private int Size;
    private int Height;
    private Timestamp timestamp;
    private int Generation;
    private int ViewID;

    public AbstractBlock(String hash, String previousHash, int size, int height, Timestamp timestamp) {
        this.Hash = hash;
        this.PreviousHash = previousHash;
        this.Size = size;
        this.Height = height;
        this.timestamp = timestamp;
    }

    public AbstractBlock(String previousHash, int height, int Generation) {
        this.PreviousHash = previousHash;
        this.Height = height;
        this.Generation = Generation;
    }

    public String getHash() {
        return Hash;
    }

    public void setHash(String hash) {
        Hash = hash;
    }

    public int getSize() {
        return Size;
    }

    public void setSize(int size) {
        Size = size;
    }

    public String getPreviousHash() {
        return PreviousHash;
    }

    public void setPreviousHash(String previousHash) {
        PreviousHash = previousHash;
    }

    public int getHeight() {
        return Height;
    }

    public void setHeight(int height) {
        Height = height;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public int getGeneration() {
        return Generation;
    }

    public void setGeneration(int generation) {
        Generation = generation;
    }

    public int getViewID() {
        return ViewID;
    }

    public void setViewID(int viewID) {
        ViewID = viewID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractBlock that = (AbstractBlock) o;
        return Size == that.Size && Height == that.Height && Generation == that.Generation && ViewID == that.ViewID && Objects.equals(Hash, that.Hash) && Objects.equals(PreviousHash, that.PreviousHash) && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Hash, PreviousHash, Size, Height, timestamp, Generation, ViewID);
    }

    @Override
    public String toString() {
        return "AbstractBlock{" +
                "Hash='" + Hash + '\'' +
                ", PreviousHash='" + PreviousHash + '\'' +
                ", Size=" + Size +
                ", Height=" + Height +
                ", timestamp=" + timestamp +
                ", Generation=" + Generation +
                ", ViewID=" + ViewID +
                '}';
    }
}
