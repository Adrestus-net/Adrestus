package io.Adrestus.Trie;

import io.Adrestus.bloom_filter.BloomFilter;
import io.Adrestus.bloom_filter.Util.UtilConstants;
import io.Adrestus.bloom_filter.impl.InMemoryBloomFilter;
import io.activej.serializer.annotations.Serialize;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public final class TransactionPointerStorage implements Serializable {

    private HashMap<Integer, HashSet<Integer>> positions;
    private BloomFilter<String> filter;

    public TransactionPointerStorage() {
        this.positions = new HashMap<Integer, HashSet<Integer>>();
        this.filter = new InMemoryBloomFilter<String>(10 * UtilConstants.MAX, UtilConstants.FPP);
    }

    public TransactionPointerStorage(HashMap<Integer, HashSet<Integer>> positions, BloomFilter<String> filter) {
        this.positions = positions;
        this.filter = filter;
    }


    @Serialize
    public HashMap<Integer, HashSet<Integer>> getPositions() {
        return positions;
    }

    public void setPositions(HashMap<Integer, HashSet<Integer>> positions) {
        this.positions = positions;
    }

    @Serialize
    public BloomFilter<String> getFilter() {
        return filter;
    }


    public void setFilter(BloomFilter<String> filter) {
        this.filter = filter;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionPointerStorage that = (TransactionPointerStorage) o;
        return Objects.equals(positions, that.positions) && Objects.equals(filter, that.filter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(positions, filter);
    }
}
