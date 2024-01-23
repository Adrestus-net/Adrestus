package io.Adrestus.bloom_filter.hash;


public interface HashFunction {

    public boolean isSingleValued();

    /**
     * Return the hash of the bytes as long.
     *
     * @param bytes the bytes to be hashed
     * @return the generated hash value
     */
    public long hash(byte[] bytes);

    /**
     * Return the hash of the bytes as a long array.
     *
     * @param bytes the bytes to be hashed
     * @return the generated hash value
     */
    public long[] hashMultiple(byte[] bytes);

}