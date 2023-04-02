package io.Adrestus.crypto.elliptic;

import java.math.BigInteger;

public interface SignInterface {
    ECDSASignatureData signMessage(byte[] message, ECKeyPair keyPair);
    ECDSASignatureData secp256SignMessage(byte[] message, ECKeyPair keyPair);

    boolean secp256Verify(byte[] hash, BigInteger publicKey, ECDSASignatureData signatureData);

    boolean secp256Verify(byte[] hash, String publicKey, ECDSASignatureData signatureData);

    boolean secp256Verify(byte[] hash, String address, ECDSASignatureData signatureData,BigInteger recovered_PubQ,String OriginalPubKey);

    boolean verify(byte[] hash, BigInteger publicKey, ECDSASignatureData signatureData);

    boolean verify(byte[] hash, String given_address, ECDSASignatureData signatureData);

    boolean verify(byte[] hash, String given_address, ECDSASignatureData signatureData, BigInteger recovered_pubQ, String OriginalPubKey);
    BigInteger recoverPublicKeyValue(BigInteger x,BigInteger y);
}
