package io.Adrestus.crypto.elliptic;

import java.math.BigInteger;
import java.security.PublicKey;

public interface SignInterface {
    ECDSASignatureData signMessage(byte[] message, ECKeyPair keyPair);

    ECDSASignatureData signSecp256k1Message(byte[] message, ECKeyPair keyPair);

    ECDSASignatureData signSecp256r1Message(byte[] message, ECKeyPair keyPair);

    boolean secp256k1Verify(byte[] hash, BigInteger publicKey, ECDSASignatureData signatureData);

    boolean secp256k1Verify(byte[] hash, String publicKey, ECDSASignatureData signatureData);

    boolean secp256k1Verify(byte[] hash, String address, BigInteger recovered_key, ECDSASignatureData signatureData);

    boolean secp256r1Verify(byte[] hash, BigInteger x_axis, BigInteger y_axis, ECDSASignatureData signatureData);

    boolean secp256r1Verify(byte[] hash,PublicKey publicKey, ECDSASignatureData signatureData);

    boolean verify(byte[] hash, BigInteger publicKey, ECDSASignatureData signatureData);

    boolean verify(byte[] hash, String given_address, ECDSASignatureData signatureData);

    boolean verify(byte[] hash, String given_address, BigInteger recovered_key, ECDSASignatureData signatureData);

    BigInteger recoverSecp256k1PublicKeyValue(BigInteger x, BigInteger y);

    PublicKey recoversecp256r1PublicKeyValue(BigInteger x, BigInteger y);
}
