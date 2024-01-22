package io.Adrestus.bloom_filter.impl;


import io.Adrestus.bloom_filter.AbstractBloomFilter;
import io.Adrestus.bloom_filter.core.BitArray;
import io.Adrestus.bloom_filter.core.JavaBitSetArray;
import io.Adrestus.bloom_filter.hash.HashFunction;

/**
 * An in-memory implementation of the bloom filter. Not suitable for
 * persistence.
 *
 * 
 * @param <T> the type of object to be stored in the filter
 */
public class InMemoryBloomFilter<T> extends AbstractBloomFilter<T> {
	
	/**
	 * Constructor
	 * 
	 * @param n
	 *            the number of elements expected to be inserted in the bloom
	 *            filter
	 * 
	 * @param fpp
	 *            the expected max false positivity rate
	 */
	public InMemoryBloomFilter(int n, double fpp) {
		super(n, fpp);
	}
	public InMemoryBloomFilter(int numBitsRequired, int kOrNumberOfHashFunctions, int[] int_array, HashFunction hasher) {
		super(numBitsRequired, kOrNumberOfHashFunctions,int_array, hasher);
	}

	/**
	 * Used a normal {@link JavaBitSetArray}.
	 * 
	 */
	@Override
	protected BitArray createBitArray(int numBits) {
		return new JavaBitSetArray(numBits);
	}

	@Override
	protected BitArray createAndSetBitArray(int numBits, int[] array){
		return new JavaBitSetArray(numBits,array);
	}

	@Override
	public BitArray getBitArray(){
		return bitArray;
	}
	
}