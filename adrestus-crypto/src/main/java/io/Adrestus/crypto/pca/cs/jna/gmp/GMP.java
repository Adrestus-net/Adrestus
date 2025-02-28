package io.Adrestus.crypto.pca.cs.jna.gmp;

import com.sun.jna.*;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Arrays;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * What we use to actually call the GMP functions. This way the low level stuff
 * is invisible to us. We can work directly with Java's BigInteger type.
 * <p>
 * Prototype taken from jna-gmp github project ((https://github.com/square/jna-gmp))
 *
 * @author Adi Pacurar
 */
public final class GMP {
    /**
     * Initial bit size of the scratch buffer.
     */
    private static final int MAX_BYTE_SIZE = 2048;
    private static final int INITIAL_BUF_BITS = 2048 * 8;
    private static final int INITIAL_BUF_SIZE = INITIAL_BUF_BITS / 8;

    /**
     * Maximum number of operands we need for any operation.
     */
    private static final int MAX_OPERANDS = 5;

    private static final int SHARED_MEM_SIZE = Long.valueOf(mpz_t.SIZE).intValue() * MAX_OPERANDS + Native.SIZE_T_SIZE;

    /**
     * Operands that can be reused over and over to avoid costly initialization and tear down
     */
    private final mpz_t[] sharedOperands = new mpz_t[MAX_OPERANDS];
    /**
     * The out Size_T pointer for export
     */
    private final Pointer countPtr;

    /**
     * A fixed, shared, reusable memory buffer.
     */

    private static volatile GMP instance;

    private final Memory sharedMem = new Memory(SHARED_MEM_SIZE) {
    };
    /**
     * Reusable scratch buffer for moving data between byte[] and mpz_t.
     */
    private Memory scratchBuf = new Memory(INITIAL_BUF_SIZE);

    private final GmpHelper gmpHelper;

    private GMP() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        int offset = 0;
        for (int i = 0; i < MAX_OPERANDS; ++i) {//create the 4 (for now) mpz_t operands we will use
            this.sharedOperands[i] = new mpz_t(sharedMem.share(offset, mpz_t.SIZE));
            LibGMPLoader.getInstance().getGmp().__gmpz_init(sharedOperands[i]);
            offset += mpz_t.SIZE;
        }
        this.countPtr = sharedMem.share(offset, Native.SIZE_T_SIZE);
        this.gmpHelper = new GmpHelper(new byte[1], 0, 1);
        offset += Native.SIZE_T_SIZE;
        assert offset == SHARED_MEM_SIZE;
    }

    public static GMP getInstance() {
        var result = instance;
        if (result == null) {
            synchronized (GMP.class) {
                result = instance;
                if (result == null) {
                    result = new GMP();
                    instance = result;
                }
            }
        }
        return result;
    }

    private static final NativeLong ZERO = new NativeLong();

    int mpzSgn(mpz_t ptr) {
        int result = LibGMPLoader.getInstance().getGmp().__gmpz_cmp_si(ptr, ZERO);
        if (result < 0) {
            return -1;
        } else if (result > 0) {
            return 1;
        }
        return 0;
    }

    private void ensureBufferSize(int size) {
        if (scratchBuf.size() < size) {
            long newSize = scratchBuf.size();
            while (newSize < size) {
                newSize <<= 1;
            }
            scratchBuf = new Memory(newSize);
        }
    }

    /**
     * If the argument is a GInteger, return its peer. Otherwise, import value into
     * sharedPeer and return sharedPeer
     */
    private mpz_t getPeer(BigInteger value, mpz_t sharedPeer) {
        if (value instanceof GInteger) {
            return this.gmpHelper.getPeer();
        }
        _mpzImport(sharedPeer, value.signum(), value.abs().toByteArray(), 0);
        return sharedPeer;
    }

    public void mpzImport(mpz_t ptr, int signum, byte[] bytes, int endian) {
        _mpzImport(ptr, signum, bytes, endian);
    }

    public BigInteger mpzImport(byte[] bytes, int endian) {
        return _mpzImport(bytes, endian);
    }

    private BigInteger _mpzImport(byte[] bytes, int endian) {
        mpzImport(sharedOperands[0], 1, bytes, endian);
        return new BigInteger(mpzSgn(sharedOperands[0]), _mpzExport(sharedOperands[0], (bytes.length + 1)));
    }

    public void mpzImport(mpz_t ptr, int signum, byte[] bytes) {
        mpzImport(ptr, signum, bytes, 0);
    }

    private void _mpzImport(mpz_t ptr, int signum, byte[] bytes, int endian) {//need to be visible in GInteger
        int expectedLength = bytes.length;
        ensureBufferSize(expectedLength);
        scratchBuf.write(0, bytes, 0, bytes.length);
        Library sizeT = LibGMPLoader.getInstance().getSize_T();
        if (sizeT instanceof SizeT4) {
            LibGMPLoader.getInstance().getSizeT4().__gmpz_import(ptr, expectedLength, 1, 1, endian, 0, scratchBuf);
        } else {
            LibGMPLoader.getInstance().getSizeT8().__gmpz_import(ptr, expectedLength, 1, 1, endian, 0, scratchBuf);
        }
        if (signum < 0) {
            LibGMPLoader.getInstance().getGmp().__gmpz_neg(ptr, ptr);
        }
    }

    public byte[] mpzExport(BigInteger obj, int endian) {
        return _mpzExport(obj, endian);
    }

    public byte[] _mpzExport(BigInteger obj, int endian) {
        mpz_t ptr = getPeer(obj, sharedOperands[0]);
        return _mpzExport(ptr, obj.bitLength() + 1, endian);
    }

    private byte[] _mpzExport(mpz_t ptr, int requiredSize, int endian) {
        ensureBufferSize(requiredSize);
        Library sizeT = LibGMPLoader.getInstance().getSize_T();
        if (sizeT instanceof SizeT4) {
            LibGMPLoader.getInstance().getSizeT4().__gmpz_export(scratchBuf, countPtr, 1, 1, endian, 0, ptr);
        } else {
            LibGMPLoader.getInstance().getSizeT8().__gmpz_export(scratchBuf, countPtr, 1, 1, endian, 0, ptr);
        }
        int count = LibGMPLoader.LibGMPHelper.readSizeT(countPtr);
        byte[] result = new byte[count];
        scratchBuf.read(0, result, 0, count);
        return result;
    }

    private byte[] _mpzExport(mpz_t ptr, int requiredSize) {
        return _mpzExport(ptr, requiredSize, 0);
    }


    private byte[] _mpzExport(mpz_t ptr) {
        return _mpzExport(ptr, MAX_BYTE_SIZE);
    }

    /**
     *
     * Below you may add any libgmp function you want to use. If it is not available
     * in LibGMP.java, add the native method there as well, import it here (see imports),
     * then add it below together with its Impl counterpart
     *
     */


    /**
     * Arithmetic functions
     */
    public BigInteger add(BigInteger a, BigInteger b) {//a + b
        return addImpl(a, b);
    }

    private BigInteger addImpl(BigInteger a, BigInteger b) {
        mpz_t peera = getPeer(a, sharedOperands[0]);
        mpz_t peerb = getPeer(b, sharedOperands[1]);
        LibGMPLoader.getInstance().getGmp().__gmpz_add(sharedOperands[2], peera, peerb);
        int requiredSize = max(a.bitLength() + 1, b.bitLength() + 1) + 1;
        return new BigInteger(mpzSgn(sharedOperands[2]), _mpzExport(sharedOperands[2], requiredSize));
    }


    public BigInteger subtract(BigInteger a, BigInteger b) {//a - b
        return subtractImpl(a, b);
    }

    private BigInteger subtractImpl(BigInteger a, BigInteger b) {
        mpz_t peera = getPeer(a, sharedOperands[0]);
        mpz_t peerb = getPeer(b, sharedOperands[1]);
        LibGMPLoader.getInstance().getGmp().__gmpz_sub(sharedOperands[2], peera, peerb);
        int requiredSize = max(a.bitLength(), b.bitLength());
        return new BigInteger(mpzSgn(sharedOperands[2]), _mpzExport(sharedOperands[2], requiredSize));
    }


    public BigInteger multiply(BigInteger a, BigInteger b) {//a * b
        return multiplyImpl(a, b);
    }

    private BigInteger multiplyImpl(BigInteger a, BigInteger b) {
        mpz_t peera = getPeer(a, sharedOperands[0]);
        mpz_t peerb = getPeer(b, sharedOperands[1]);
        LibGMPLoader.getInstance().getGmp().__gmpz_mul(sharedOperands[2], peera, peerb);
        int requiredSize = a.bitLength() + b.bitLength();
        return new BigInteger(mpzSgn(sharedOperands[2]), _mpzExport(sharedOperands[2], requiredSize));
    }


    public BigInteger divide(BigInteger a, BigInteger b) {//a / b
        return divideImpl(a, b);
    }

    private BigInteger divideImpl(BigInteger a, BigInteger b) {
        mpz_t peera = getPeer(a, sharedOperands[0]);
        mpz_t peerb = getPeer(b, sharedOperands[1]);
        LibGMPLoader.getInstance().getGmp().__gmpz_tdiv_q(sharedOperands[2], peera, peerb);
        int requiredSize = min(a.bitLength(), b.bitLength());
        return new BigInteger(mpzSgn(sharedOperands[2]), _mpzExport(sharedOperands[2], requiredSize));
    }

    public BigInteger fdiv_q(BigInteger a, BigInteger b) {//a / b
        return fdiv_qImpl(a, b);
    }

    private BigInteger fdiv_qImpl(BigInteger a, BigInteger b) {
        mpz_t peera = getPeer(a, sharedOperands[0]);
        mpz_t peerb = getPeer(b, sharedOperands[1]);
        LibGMPLoader.getInstance().getGmp().__gmpz_fdiv_q(sharedOperands[2], peera, peerb);
        int requiredSize = min(a.bitLength(), b.bitLength());
        return new BigInteger(mpzSgn(sharedOperands[2]), _mpzExport(sharedOperands[2], requiredSize));
    }

    public BigInteger fdiv(BigInteger a, BigInteger b) {
        if (a.signum() == b.signum()) {
            return divide(a, b);
        } else {
            return fdiv_q(a, b);
        }
    }

    public BigInteger fdivQUI(BigInteger a, long b) {//a / b
        return fdiv_q_uiImpl(a, b);
    }

    private BigInteger fdiv_q_uiImpl(BigInteger a, long b) {
        mpz_t peera = getPeer(a, sharedOperands[0]);
        NativeLong peerb = new NativeLong(b);
        LibGMPLoader.getInstance().getGmp().__gmpz_fdiv_q_ui(sharedOperands[2], peera, peerb);
        int requiredSize = a.bitLength();
        return new BigInteger(mpzSgn(sharedOperands[2]), _mpzExport(sharedOperands[2], requiredSize));
    }


    public BigInteger remainder(BigInteger a, BigInteger b) {//a % b
        return remainderImpl(a, b);
    }

    private BigInteger remainderImpl(BigInteger a, BigInteger b) {
        mpz_t peera = getPeer(a, sharedOperands[0]);
        mpz_t peerb = getPeer(b, sharedOperands[1]);
        LibGMPLoader.getInstance().getGmp().__gmpz_tdiv_r(sharedOperands[2], peera, peerb);
        int requiredSize = b.bitLength();
        return new BigInteger(mpzSgn(sharedOperands[2]), _mpzExport(sharedOperands[2], requiredSize));
    }


    /**
     * Divide dividend by divisor. This method only returns correct answers when the division produces
     * no remainder. Correct answers should not be expected when the divison would result in a
     * remainder.
     *
     * @return dividend / divisor
     * @throws ArithmeticException if divisor is zero
     */
    public BigInteger exactDivide(BigInteger dividend, BigInteger divisor) {
        if (divisor.signum() == 0) {
            throw new ArithmeticException("BigInteger divide by zero");
        }
        return exactDivImpl(dividend, divisor);
    }

    private BigInteger exactDivImpl(BigInteger dividend, BigInteger divisor) {
        mpz_t dividendPeer = getPeer(dividend, sharedOperands[0]);
        mpz_t divisorPeer = getPeer(divisor, sharedOperands[1]);
        LibGMPLoader.getInstance().getGmp().__gmpz_divexact(sharedOperands[2], dividendPeer, divisorPeer);
        // The result size is never larger than the bit length of the dividend minus that of the divisor
        // plus 1 (but is at least 1 bit long to hold the case that the two values are exactly equal)
        int requiredSize = max(dividend.bitLength() - divisor.bitLength() + 1, 1);
        return new BigInteger(mpzSgn(sharedOperands[2]), _mpzExport(sharedOperands[2], requiredSize));
    }

    /**
     * Return the greatest common divisor of value1 and value2. The result is always positive even if
     * one or both input operands are negative. Except if both inputs are zero; then this method
     * defines gcd(0,0) = 0.
     *
     * @return greatest common divisor of value1 and value2
     */
    public BigInteger gcd(BigInteger a, BigInteger b) {
        return gcdImpl(a, b);
    }

    private BigInteger gcdImpl(BigInteger value1, BigInteger value2) {
        mpz_t value1Peer = getPeer(value1, sharedOperands[0]);
        mpz_t value2Peer = getPeer(value2, sharedOperands[1]);
        LibGMPLoader.getInstance().getGmp().__gmpz_gcd(sharedOperands[2], value1Peer, value2Peer);
        // The result size will be no larger than the smaller of the inputs
        int requiredSize = min(value1.bitLength(), value2.bitLength());
        return new BigInteger(mpzSgn(sharedOperands[2]), _mpzExport(sharedOperands[2], requiredSize));
    }

    /**
     * Calculates g, s, and t, such that a*s + b*t = g = gcd(a,b),
     * where gcd is the greatest common divisor. Returns an array with respective elements g, s and t.
     **/

    public BigInteger[] gcdExt(BigInteger a, BigInteger b) {
        return gcdExtImpl(a, b);
    }

    private BigInteger[] gcdExtImpl(BigInteger value1, BigInteger value2) {
        mpz_t value1Peer = getPeer(value1, sharedOperands[0]);
        mpz_t value2Peer = getPeer(value2, sharedOperands[1]);
        LibGMPLoader.getInstance().getGmp().__gmpz_gcdext(sharedOperands[2], sharedOperands[3], sharedOperands[4], value1Peer, value2Peer);
        BigInteger gcd = new BigInteger(mpzSgn(sharedOperands[2]), _mpzExport(sharedOperands[2]));
        BigInteger s = new BigInteger(mpzSgn(sharedOperands[3]), _mpzExport(sharedOperands[3]));
        BigInteger t = new BigInteger(mpzSgn(sharedOperands[4]), _mpzExport(sharedOperands[4]));
        return new BigInteger[]{gcd, s, t};
    }

    /**
     * Calculate (base ^ exponent) % modulus; faster, VULNERABLE TO TIMING ATTACKS.
     */
    public BigInteger modPowInsecure(BigInteger base, BigInteger exp,
                                     BigInteger mod) {
        if (mod.signum() <= 0) {
            throw new ArithmeticException("modulus must be positive");
        }
        return modPowInsecureImpl(base, exp, mod);
    }

    private BigInteger modPowInsecureImpl(BigInteger base, BigInteger exp, BigInteger mod) {
        boolean invert = exp.signum() < 0;
        if (invert) {
            exp = exp.negate();
        }
        mpz_t basePeer = getPeer(base, sharedOperands[0]);
        mpz_t expPeer = getPeer(exp, sharedOperands[1]);
        mpz_t modPeer = getPeer(mod, sharedOperands[2]);
        if (invert) {
            int res = LibGMPLoader.getInstance().getGmp().__gmpz_invert(basePeer, basePeer, modPeer);
            if (res == 0) {
                throw new ArithmeticException("val not invertible");
            }
        }
        LibGMPLoader.getInstance().getGmp().__gmpz_powm(sharedOperands[3], basePeer, expPeer, modPeer);
        // The result size should be <= modulus size, but round up to the nearest byte.
        int requiredSize = (mod.bitLength() + 7) / 8;
        return new BigInteger(mpzSgn(sharedOperands[3]), _mpzExport(sharedOperands[3], requiredSize));
    }

    /**
     * Calculate (base ^ exponent) % modulus; slower, hardened against timing attacks.
     * <p>
     * Requires modulus to be odd.
     */
    public BigInteger modPowSecure(BigInteger base, BigInteger exp, BigInteger mod) {
        if (mod.signum() <= 0) {
            throw new ArithmeticException("modulus must be positive");
        }
        if (!mod.testBit(0)) {
            throw new IllegalArgumentException("modulus must be odd");
        }
        return modPowSecureImpl(base, exp, mod);
    }

    private BigInteger modPowSecureImpl(BigInteger base, BigInteger exp, BigInteger mod) {
        boolean invert = exp.signum() < 0;
        if (invert) {
            exp = exp.negate();
        }
        mpz_t basePeer = getPeer(base, sharedOperands[0]);
        mpz_t expPeer = getPeer(exp, sharedOperands[1]);
        mpz_t modPeer = getPeer(mod, sharedOperands[2]);
        if (invert) {
            int res = LibGMPLoader.getInstance().getGmp().__gmpz_invert(basePeer, basePeer, modPeer);
            if (res == 0) {
                throw new ArithmeticException("val not invertible");
            }
        }
        LibGMPLoader.getInstance().getGmp().__gmpz_powm_sec(sharedOperands[3], basePeer, expPeer, modPeer);
        // The result size should be <= modulus size, but round up to the nearest byte.
        int requiredSize = (mod.bitLength() + 7) / 8;
        return new BigInteger(mpzSgn(sharedOperands[3]), _mpzExport(sharedOperands[3], requiredSize));
    }

    /**
     * Calculate multiplicative inverse of "a" with respect to modulus
     */
    public BigInteger modInverse(BigInteger a, BigInteger mod) {
        if (mod.signum() <= 0) {
            throw new ArithmeticException("modulus must be positive");
        }
        return modInverseImpl(a, mod);
    }

    private BigInteger modInverseImpl(BigInteger val, BigInteger mod) {
        mpz_t valPeer = getPeer(val, sharedOperands[0]);
        mpz_t modPeer = getPeer(mod, sharedOperands[1]);
        int res = LibGMPLoader.getInstance().getGmp().__gmpz_invert(sharedOperands[2], valPeer, modPeer);
        if (res == 0) {
            throw new ArithmeticException("val not invertible");
        }
        // The result size should be <= modulus size, but round up to the nearest byte.
        int requiredSize = (mod.bitLength() + 7) / 8;
        return new BigInteger(mpzSgn(sharedOperands[2]), _mpzExport(sharedOperands[2], requiredSize));
    }


    /**
     * Calculate Legendre symbol. Note the gmp library returns an int type,
     * which we may return directly as the output of this function.
     */
    public int legendre(BigInteger a, BigInteger p) {
        return legendreImpl(a, p);
    }

    private int legendreImpl(BigInteger a, BigInteger p) {
        mpz_t aPeer = getPeer(a, sharedOperands[0]);
        mpz_t pPeer = getPeer(p, sharedOperands[1]);
        return LibGMPLoader.getInstance().getGmp().__gmpz_legendre(aPeer, pPeer);
    }

    /**
     * Calculate Jacobi symbol. Note the gmp library returns an int type,
     * which we may return directly as the output of this function.
     */
    public int jacobi(BigInteger a, BigInteger p) {
        return jacobiImpl(a, p);
    }

    private int jacobiImpl(BigInteger a, BigInteger p) {
        mpz_t aPeer = getPeer(a, sharedOperands[0]);
        mpz_t pPeer = getPeer(p, sharedOperands[1]);
        return LibGMPLoader.getInstance().getGmp().__gmpz_jacobi(aPeer, pPeer);
    }


    /**
     * Primality functions
     */
    public int isProbablePrime(BigInteger a, int certainty) {
        return isProbablePrimeImpl(a, certainty);
    }

    private int isProbablePrimeImpl(BigInteger a, int certainty) {
        mpz_t aPeer = getPeer(a, sharedOperands[0]);
        return LibGMPLoader.getInstance().getGmp().__gmpz_probab_prime_p(aPeer, certainty);
    }


    public BigInteger nextPrime(BigInteger a) {
        return nextPrimeImpl(a);
    }

    private BigInteger nextPrimeImpl(BigInteger a) {
        mpz_t peera = getPeer(a, sharedOperands[0]);
        LibGMPLoader.getInstance().getGmp().__gmpz_nextprime(sharedOperands[1], peera);
        int requiredSize = a.bitLength() + 1;//+ 10 should be safe... there are arbitrarily large gaps in primes
        return new BigInteger(mpzSgn(sharedOperands[1]), _mpzExport(sharedOperands[1], requiredSize));
    }

    public void clear() {
        Arrays.stream(this.sharedOperands).forEach(mpz_t::clear);
        LibGMPLoader.getInstance().cleanup();
        Memory.disposeAll();
        Memory.purge();
        instance = null;
    }


    @Getter
    public final class GmpHelper extends BigInteger {
        private final MPZMemory memory = new MPZMemory();

        public GmpHelper(@NotNull byte[] val, int off, int len) {
            super(val, off, len);
            mpzImport(memory.peer, super.signum(), super.abs().toByteArray());
        }

        public mpz_t getPeer() {
            return memory.peer;
        }

    }

}