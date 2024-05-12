package io.Adrestus.Trie;

import io.Adrestus.bloom_filter.BloomFilter;
import io.Adrestus.bloom_filter.Util.UtilConstants;
import io.Adrestus.bloom_filter.impl.InMemoryBloomFilter;
import io.activej.serializer.annotations.Serialize;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public final class ReceiptPointerStorage implements Serializable {

    private HashMap<Integer, HashMap<Integer, HashMap<Integer, HashSet<Integer>>>> positions;
    private BloomFilter<String> filter;

    public ReceiptPointerStorage() {
        this.positions = new HashMap<Integer, HashMap<Integer, HashMap<Integer, HashSet<Integer>>>>();
        this.filter = new InMemoryBloomFilter<String>(10 * UtilConstants.MAX, UtilConstants.FPP);
    }

    public ReceiptPointerStorage(HashMap<Integer, HashMap<Integer, HashMap<Integer, HashSet<Integer>>>> positions, BloomFilter<String> filter) {
        this.positions = positions;
        this.filter = filter;
    }

    @Serialize
    public HashMap<Integer, HashMap<Integer, HashMap<Integer, HashSet<Integer>>>> getPositions() {
        return positions;
    }

    @Serialize
    public BloomFilter<String> getFilter() {
        return filter;
    }


    public void setPositions(HashMap<Integer, HashMap<Integer, HashMap<Integer, HashSet<Integer>>>> positions) {
        this.positions = positions;
    }

    public void setFilter(BloomFilter<String> filter) {
        this.filter = filter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReceiptPointerStorage that = (ReceiptPointerStorage) o;
        return Objects.equals(positions, that.positions) && Objects.equals(filter, that.filter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(positions, filter);
    }

    @Override
    public String toString() {
        return "ReceiptPointerStorage{" +
                "positions=" + positions +
                ", filter=" + filter +
                '}';
    }
}
