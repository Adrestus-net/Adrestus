package io.Adrestus.Trie.optimize64_trie;


import io.Adrestus.util.bytes.Bytes;
import io.Adrestus.util.bytes.Bytes32;

/**
 * This exception is thrown when there is an issue retrieving or decoding values from {@link
 * MerkleStorage}.
 */
public class MerkleTrieException extends RuntimeException {

    private Bytes32 hash;
    private Bytes location;

    public MerkleTrieException(final String message) {
        super(message);
    }

    public MerkleTrieException(final String message, final Bytes32 hash, final Bytes location) {
        super(message);
        this.hash = hash;
        this.location = location;
    }

    public MerkleTrieException(final String message, final Exception cause) {
        super(message, cause);
    }

    public Bytes32 getHash() {
        return hash;
    }

    public Bytes getLocation() {
        return location;
    }
}
