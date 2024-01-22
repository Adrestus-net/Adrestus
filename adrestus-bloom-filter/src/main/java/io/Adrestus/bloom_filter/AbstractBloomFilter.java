package io.Adrestus.bloom_filter;

import io.Adrestus.bloom_filter.Util.UtilBloomFilter;
import io.Adrestus.bloom_filter.core.BitArray;
import io.Adrestus.bloom_filter.core.JavaBitSetArray;
import io.Adrestus.bloom_filter.decompose.ByteSink;
import io.Adrestus.bloom_filter.decompose.Decomposable;
import io.Adrestus.bloom_filter.decompose.Decomposer;
import io.Adrestus.bloom_filter.decompose.DefaultDecomposer;
import io.Adrestus.bloom_filter.hash.HashFunction;
import io.Adrestus.bloom_filter.hash.Murmur3HashFunction;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;

public abstract class AbstractBloomFilter<T> implements BloomFilter<T> {

    /**
     * The decomposer to use when there is none specified at construction
     */
    protected static final Decomposer<Object> DEFAULT_COMPOSER = new DefaultDecomposer();

    /**
     * The default hasher to use if one is not specified
     */
    protected static final HashFunction DEFAULT_HASHER = new Murmur3HashFunction();

    /**
     * Constant
     */
    public static final double LOG_2 = Math.log(2);

    /**
     * Constant
     */
    public static final double LOG_2_SQUARE = LOG_2 * LOG_2;

    /**
     * The default {@link Charset} is the platform encoding charset
     */
    protected transient Charset currentCharset = Charset.defaultCharset();

    /**
     * The {@link BitArray} instance that holds the entire data
     */
    protected final BitArray bitArray;

    /**
     * Number of hash functions needed
     */
    protected final int kOrNumberOfHashFunctions;

    /**
     * Holds the custom decomposer that should be used for this bloom filter
     */
    protected final Decomposer<T> customDecomposer;

    /**
     * The hashing method to be used for hashing
     */
    protected final HashFunction hasher;

    /**
     * Number of bits required for the bloom filter
     */
    protected final int numBitsRequired;

    // Various construction mechanisms

    /**
     * Create a new bloom filter.
     *
     * @param expectedInsertions       the number of max expected insertions
     * @param falsePositiveProbability the max false positive probability rate that the bloom filter
     *                                 can give
     */
    protected AbstractBloomFilter(int expectedInsertions, double falsePositiveProbability) {
        this(expectedInsertions, falsePositiveProbability, null, null);
    }

    /**
     * Create a new bloom filter.
     *
     * @param expectedInsertions       the number of max expected insertions
     * @param falsePositiveProbability the max false positive probability rate that the bloom filter
     *                                 can give
     * @param decomposer               a {@link Decomposer} that helps decompose the given object
     */
    protected AbstractBloomFilter(int expectedInsertions, double falsePositiveProbability, Decomposer<T> decomposer) {
        this(expectedInsertions, falsePositiveProbability, decomposer, null);
    }

    /**
     * Create a new bloom filter.
     *
     * @param expectedInsertions       the number of max expected insertions
     * @param falsePositiveProbability the max false positive probability rate that the bloom filter
     *                                 can give
     * @param decomposer               a {@link Decomposer} that helps decompose the given object
     * @param hasher                   the hash function to use. If <code>null</code> is specified
     *                                 the {@link AbstractBloomFilter#DEFAULT_HASHER} will be used as
     *                                 the hashing function
     */
    protected AbstractBloomFilter(int expectedInsertions, double falsePositiveProbability, Decomposer<T> decomposer, HashFunction hasher) {
        this.numBitsRequired = optimalBitSizeOrM(expectedInsertions, falsePositiveProbability);
        this.kOrNumberOfHashFunctions = optimalNumberofHashFunctionsOrK(expectedInsertions, numBitsRequired);
        this.bitArray = createBitArray(numBitsRequired);

        this.customDecomposer = decomposer;

        if (hasher != null) {
            this.hasher = hasher;
        } else {
            this.hasher = DEFAULT_HASHER;
        }
    }

    protected AbstractBloomFilter(int numBitsRequired, int kOrNumberOfHashFunctions, int[] int_array, HashFunction hasher) {
        this.numBitsRequired = numBitsRequired;
        this.kOrNumberOfHashFunctions = kOrNumberOfHashFunctions;
        this.bitArray = createAndSetBitArray(numBitsRequired, int_array);

        this.customDecomposer = null;

        if (hasher != null) {
            this.hasher = hasher;
        } else {
            this.hasher = DEFAULT_HASHER;
        }
    }

    // Default bloom filter functions follow

    /**
     * Compute the optimal size <code>m</code> of the bloom filter in bits.
     *
     * @param n the number of expected insertions, or <code>n</code>
     * @param p the maximum false positive rate expected, or <code>p</code>
     * @return the optimal size in bits for the filter, or <code>m</code>
     */
    public static int optimalBitSizeOrM(final double n, final double p) {
        return (int) (-n * Math.log(p) / (LOG_2_SQUARE));
        // return (int) Math.ceil(-1 * n * Math.log(p) / LOG_2_SQUARE);
    }

    /**
     * Compute the optimal number of hash functions, <code>k</code>
     *
     * @param n the number of expected insertions or <code>n</code>
     * @param m the number of bits in the filter
     * @return the optimal number of hash functions to be used also known as
     * <code>k</code>
     */
    public static int optimalNumberofHashFunctionsOrK(final long n, final long m) {
        return Math.max(1, (int) Math.round(m / n * Math.log(2)));
        // return Math.max(1, (int) Math.round(m / n * LOG_2));
    }

    // Abstraction layer

    /**
     * Create a new {@link BitArray} instance for the given number of bits.
     *
     * @param numBits the number of required bits in the underlying array
     * @return the {@link BitArray} implementation to be used
     */
    protected abstract BitArray createBitArray(int numBits);

    protected abstract BitArray createAndSetBitArray(int numBits, int[] array);
    // Main functions that govern the bloom filter

    /**
     * Add the given byte array to the bloom filter
     *
     * @param bytes the byte array to be added to the bloom filter, cannot be null
     * @return <code>true</code> if the value was added to the bloom filter,
     * <code>false</code> otherwise
     * @throws IllegalArgumentException if the byte array is <code>null</code>
     */
    @Override
    public final boolean add(byte[] bytes) {
        long hash64 = getLongHash64(bytes);

        // apply the less hashing technique
        int hash1 = (int) hash64;
        int hash2 = (int) (hash64 >>> 32);

        boolean bitsChanged = false;
        for (int i = 1; i <= this.kOrNumberOfHashFunctions; i++) {
            int nextHash = hash1 + i * hash2;
            if (nextHash < 0) {
                nextHash = ~nextHash;
            }
            bitsChanged |= this.bitArray.setBit(nextHash % this.bitArray.bitSize());
        }

        return bitsChanged;
    }

    /**
     * Check if the given byte array item exists in the bloom filter
     *
     * @param bytes the byte array to be tested for existence in the bloom filter,
     *              cannot be null
     * @return <code>true</code> if the value exists in the bloom filter,
     * <code>false</code> otherwise
     * @throws IllegalArgumentException if the byte array is <code>null</code>
     */
    @Override
    public final boolean contains(byte[] bytes) {
        long hash64 = getLongHash64(bytes);

        int hash1 = (int) hash64;
        int hash2 = (int) (hash64 >>> 32);
        for (int i = 1; i <= this.kOrNumberOfHashFunctions; i++) {
            int nextHash = hash1 + i * hash2;
            if (nextHash < 0) {
                nextHash = ~nextHash;
            }
            if (!this.bitArray.getBit(nextHash % this.bitArray.bitSize())) {
                return false;
            }
        }
        return true;
    }

    // Helper functions for functionality within

    /**
     * Compute one 64-bit hash from the given byte-array using the specified
     * {@link HashFunction}.
     *
     * @param bytes the byte-array to use for hash computation
     * @return the 64-bit hash
     */
    protected long getLongHash64(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("Bytes to add to bloom filter cannot be null");
        }

        if (this.hasher.isSingleValued()) {
            return this.hasher.hash(bytes);
        }

        return this.hasher.hashMultiple(bytes)[0];
    }

    /**
     * Given the value object, decompose it into a byte-array so that hashing
     * can be done over the returned bytes. If a custom {@link Decomposer} has
     * been specified, it will be used, otherwise the {@link DefaultDecomposer}
     * will be used.
     *
     * @param value the value to be decomposed
     * @return the decomposed byte array
     */
    protected byte[] decomposedValue(T value) {
        ByteSink sink = new ByteSink();

        if (value instanceof Decomposable) {
            ((Decomposable) value).decompose(sink);

            return sink.getByteArray();
        }

        if (this.customDecomposer != null) {
            this.customDecomposer.decompose(value, sink);
            return sink.getByteArray();
        }

        DEFAULT_COMPOSER.decompose(value, sink);
        return sink.getByteArray();
    }

    // Overridden helper functions follow

    /**
     * Add the given value to the bloom filter.
     *
     * @param value the value to be added
     * @return <code>true</code> if the value was added to the bloom filter,
     * <code>false</code> otherwise
     */
    @Override
    public boolean add(T value) {
        if (value == null) {
            return false;
        }

        return add(decomposedValue(value));
    }

    /**
     * Add all given value in the collection to the bloom filter
     *
     * @param values the collection of values to be inserted
     * @return <code>true</code> if all values were successfully inserted into
     * the bloom filter, <code>false</code> otherwise
     */
    @Override
    public boolean addAll(Collection<T> values) {
        if (values == null || values.isEmpty()) {
            return false;
        }

        boolean success = true;
        for (T value : values) {
            success = add(value) && success;
        }

        return success;
    }

    /**
     * Check if the given value exists in the bloom filter. Note that this
     * method may return <code>true</code>, indicating a false positive - but
     * this is the property of the bloom filter and is not a bug.
     *
     * @return <code>false</code> if the value is definitely (100% surety) not
     * contained in the bloom filter, <code>true</code> otherwise.
     */
    @Override
    public boolean contains(T value) {
        if (value == null) {
            return false;
        }

        return contains(value.toString().getBytes(this.currentCharset));
    }

    /**
     * Check if all the given values exists in the bloom filter or not.
     *
     * @return <code>true</code> only if all values are believed to be existing
     * in the bloom filter, <code>false</code> otherwise
     */
    @Override
    public boolean containsAll(Collection<T> values) {
        if (values == null || values.isEmpty()) {
            return false;
        }

        for (T value : values) {
            if (!contains(value)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean containsAddress(int current[]) {
        BitSet from = ((JavaBitSetArray) bitArray).getBitSet();
        BitSet target = UtilBloomFilter.Ints2Bits(current);
        return from.equals(target);
    }

    @Override
    public int[] toBitsetArray() {
        ArrayList<Integer> list_values = new ArrayList<>();
        BitSet bitset = ((JavaBitSetArray) bitArray).getBitSet();
        bitset.stream().forEach(value -> {
            list_values.add(value);
        });
        return list_values.stream().mapToInt(Integer::intValue).toArray();
    }


    /**
     * Override the default charset that will be used when decomposing the
     * {@link String} values into byte arrays. The default {@link Charset} used
     * in the platform's default {@link Charset}.
     *
     * @param charsetName the name of the charset that needs to be set
     * @throws IllegalArgumentException    if the charsetName is null
     * @throws IllegalCharsetNameException If the given charset name is illegal
     * @throws UnsupportedCharsetException If no support for the named charset is available in this
     *                                     instance of the Java virtual machine
     */
    @Override
    public void setCharset(String charsetName) {
        if (charsetName == null) {
            throw new IllegalArgumentException("Charset to be changed to cannot be null");
        }

        setCharset(Charset.forName(charsetName));
    }

    /**
     * Override the default charset that will be used when decomposing the
     * {@link String} values into byte arrays. The default {@link Charset} used
     * in the platform's default {@link Charset}.
     *
     * @param charset the {@link Charset} to be used
     * @throws IllegalArgumentException if the charset is null
     */
    @Override
    public void setCharset(Charset charset) {
        if (charset == null) {
            throw new IllegalArgumentException("Charset to be changed to cannot be null");
        }

        this.currentCharset = charset;
    }

    /**
     * Get the current custom decomposer that is being used. If no custom
     * decomposer is specified, <code>null</code> is returned to signify that we
     * are using the {@link AbstractBloomFilter#DEFAULT_HASHER} hash function.
     *
     * @return the current custom decomposer being used, if any
     */
    @Override
    public Decomposer<T> getObjectDecomposer() {
        if (this.customDecomposer != null) {
            return this.customDecomposer;
        }

        return null;
    }

    /**
     * @see BloomFilter#getNumberOfBits()
     */
    @Override
    public int getNumberOfBits() {
        return this.numBitsRequired;
    }

    @Override
    public int getNumberOfHashFunctions() {
        return this.kOrNumberOfHashFunctions;
    }

    /**
     * Estimate the current false positive rate (approximated) when given number
     * of elements have been inserted in to the filter.
     *
     * @param numInsertedElements the number of elements inserted into the filter
     * @return the approximated false positive rate
     */
    @Override
    public double getFalsePositiveProbability(int numInsertedElements) {
        return Math.pow((1 - Math.exp((-this.kOrNumberOfHashFunctions) * (double) numInsertedElements / (double) this.numBitsRequired)), this.kOrNumberOfHashFunctions);
    }

    @Override
    public void close() {
        if (this.bitArray instanceof Closeable) {
            try {
                ((Closeable) this.bitArray).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}