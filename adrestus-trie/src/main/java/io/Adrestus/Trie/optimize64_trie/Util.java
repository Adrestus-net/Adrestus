package io.Adrestus.Trie.optimize64_trie;

import com.google.common.base.Suppliers;
import io.Adrestus.crypto.MessageDigestFactory;
import io.Adrestus.util.bytes.Bytes;
import io.Adrestus.util.bytes.Bytes32;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Supplier;

public class Util {
    public static final String KECCAK256_ALG = "KECCAK-256";
    private static final Supplier<MessageDigest> KECCAK256_SUPPLIER =
            Suppliers.memoize(() -> messageDigest(KECCAK256_ALG));

    private static MessageDigest messageDigest(final String algorithm) {
        try {
            return MessageDigestFactory.create(algorithm);
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static Bytes32 keccak256(final Bytes input) {
        return Bytes32.wrap(digestUsingAlgorithm(input, KECCAK256_SUPPLIER));
    }

    private static byte[] digestUsingAlgorithm(
            final Bytes input, final Supplier<MessageDigest> digestSupplier) {
        try {
            final MessageDigest digest = (MessageDigest) digestSupplier.get().clone();
            input.update(digest);
            return digest.digest();
        } catch (final CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
