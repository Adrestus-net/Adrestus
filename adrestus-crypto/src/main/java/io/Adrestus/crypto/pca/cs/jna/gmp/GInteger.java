package io.Adrestus.crypto.pca.cs.jna.gmp;

import java.math.BigInteger;
import java.util.Random;


public class GInteger extends BigInteger {	

    private static final long serialVersionUID = 1L;
    private final MPZMemory memory = new MPZMemory();
	
	{
		GMP.mpzImport(memory.peer, super.signum(), super.abs().toByteArray());
	}
	
	mpz_t getPeer() {
		return memory.peer;
	}

	public GInteger (BigInteger other) {
		super(other.toByteArray());
	}
	
	public GInteger(byte[] val) {
		super(val);
	}
	public GInteger(int signum, byte[] magnitude) {
		super(signum, magnitude);
	}
	public GInteger(String val, int radix) {
		super(val, radix);
	}
	public GInteger(String val) {
		super(val);
	}
	public GInteger(int numbits, Random r) {
		super(numbits, r);
	}
	public GInteger (int bitlength, int certainty, Random r) {
		super(bitlength, certainty, r);
	}
}
