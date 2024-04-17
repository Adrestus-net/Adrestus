package io.Adrestus.Trie;

import io.activej.serializer.annotations.Serialize;

import java.util.HashSet;
import java.util.Objects;

public class StorageInfo {

    private int origin_zone;
    private int blockHeight;

    private int zone;
    private int receiptBlockHeight;
    private int position;

    private HashSet<Integer> positions;


    public StorageInfo() {
    }


    public StorageInfo(int origin_zone, int blockHeight, int position) {
        this.origin_zone = origin_zone;
        this.blockHeight = blockHeight;
        this.position = position;
    }

    public StorageInfo(int blockHeight, HashSet<Integer> positions) {
        this.blockHeight = blockHeight;
        this.positions = positions;
    }

    public StorageInfo(int origin_zone, int blockHeight, HashSet<Integer> positions) {
        this.origin_zone = origin_zone;
        this.blockHeight = blockHeight;
        this.positions = positions;
    }

    public StorageInfo(int origin_zone, int blockHeight, int zone, int receiptBlockHeight, int position) {
        this.origin_zone = origin_zone;
        this.blockHeight = blockHeight;
        this.zone = zone;
        this.receiptBlockHeight = receiptBlockHeight;
        this.position = position;
    }

    @Serialize
    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Serialize
    public int getZone() {
        return zone;
    }

    public void setZone(int zone) {
        this.zone = zone;
    }

    @Serialize
    public int getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(int blockHeight) {
        this.blockHeight = blockHeight;
    }

    @Serialize
    public int getReceiptBlockHeight() {
        return receiptBlockHeight;
    }

    public void setReceiptBlockHeight(int receiptBlockHeight) {
        this.receiptBlockHeight = receiptBlockHeight;
    }

    @Serialize
    public int getOrigin_zone() {
        return origin_zone;
    }

    public void setOrigin_zone(int origin_zone) {
        this.origin_zone = origin_zone;
    }

    @Serialize
    public HashSet<Integer> getPositions() {
        return positions;
    }

    public void setPositions(HashSet<Integer> positions) {
        this.positions = positions;
    }


    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        StorageInfo that = (StorageInfo) object;
        return origin_zone == that.origin_zone && blockHeight == that.blockHeight && zone == that.zone && receiptBlockHeight == that.receiptBlockHeight && position == that.position && Objects.equals(positions, that.positions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin_zone, blockHeight, zone, receiptBlockHeight, position, positions);
    }

    @Override
    public String toString() {
        return "StorageInfo{" +
                "origin_zone=" + origin_zone +
                ", blockHeight=" + blockHeight +
                ", zone=" + zone +
                ", receiptBlockHeight=" + receiptBlockHeight +
                ", position=" + position +
                ", positions=" + positions +
                '}';
    }
}
