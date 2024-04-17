package io.Adrestus.bloom_filter.hash;

import net.jpountz.xxhash.XXHash64;
import net.jpountz.xxhash.XXHashFactory;

import java.io.Serializable;

public class XXXHashFunction implements HashFunction, Cloneable, Serializable {

    private static final long SEED = 0x12345678;

    @Override
    public boolean isSingleValued() {
        return false;
    }

    @Override
    public long hash(byte[] data) {
        XXHashFactory factory = XXHashFactory.fastestInstance();
        XXHash64 hash64 = factory.hash64();
        long hash = hash64.hash(data, 0, data.length, SEED);
        return hash;
    }

    @Override
    public long[] hashMultiple(byte[] bytes) {
        return null;
    }


}