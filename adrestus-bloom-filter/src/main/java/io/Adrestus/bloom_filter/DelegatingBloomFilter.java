package io.Adrestus.bloom_filter;


import io.Adrestus.bloom_filter.decompose.Decomposer;

import java.io.Serializable;
import java.util.Collection;

public abstract class DelegatingBloomFilter<T> implements BloomFilter<T>, Cloneable, Serializable {

    protected final BloomFilter<T> originalBloomFilter;

    public DelegatingBloomFilter(BloomFilter<T> original) {
        this.originalBloomFilter = original;
    }


    @Override
    public boolean add(byte[] bytes) {
        return this.originalBloomFilter.add(bytes);
    }


    @Override
    public boolean add(T value) {
        return this.originalBloomFilter.add(value);
    }


    @Override
    public boolean addAll(Collection<T> values) {
        return this.originalBloomFilter.addAll(values);
    }


    @Override
    public boolean contains(byte[] bytes) {
        return this.originalBloomFilter.contains(bytes);
    }


    @Override
    public boolean contains(T value) {
        return this.originalBloomFilter.contains(value);
    }


    @Override
    public boolean containsAll(Collection<T> values) {
        return this.originalBloomFilter.containsAll(values);
    }

    @Override
    public Decomposer<T> getObjectDecomposer() {
        return this.originalBloomFilter.getObjectDecomposer();
    }

    @Override
    public int getNumberOfBits() {
        return this.originalBloomFilter.getNumberOfBits();
    }

    @Override
    public double getFalsePositiveProbability(int numInsertedElements) {
        return this.originalBloomFilter.getFalsePositiveProbability(numInsertedElements);
    }


    @Override
    public void close() {
        this.originalBloomFilter.close();
    }
}