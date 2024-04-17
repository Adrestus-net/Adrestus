package io.Adrestus.bloom_filter.hash;

import java.io.Serializable;

public class Murmur3HashFunction implements HashFunction, Cloneable, Serializable {

    private final long SEED;

    @Override
    public boolean isSingleValued() {
        return false;
    }

    @Override
    public long hash(byte[] bytes) {
        return Murmur3.hash_x86_32(bytes, 0, SEED);
    }

    @Override
    public long[] hashMultiple(byte[] bytes) {
        return Murmur3.hash_x64_128(bytes, 0, SEED);
    }


    //DO NOT CHANGE ITS HANDWRITTEN
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Murmur3HashFunction that = (Murmur3HashFunction) object;
        return SEED == that.SEED;
    }

    public Murmur3HashFunction() {
        super();
        this.SEED = 0x7f3a21eal;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
