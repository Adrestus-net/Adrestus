package io.Adrestus.bloom_filter.core;

import java.io.Closeable;

public interface BitArray extends Closeable {


	public boolean getBit(int index);
	public boolean setBit(int index);
	public void clear();
	public void clearBit(int index);
	public boolean setBitIfUnset(int index);
	public void or(BitArray bitArray);
	public void and(BitArray bitArray);
	public int bitSize();

}