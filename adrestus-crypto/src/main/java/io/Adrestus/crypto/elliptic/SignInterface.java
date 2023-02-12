package io.Adrestus.crypto.elliptic;

public interface SignInterface {
    ECDSASignatureData signMessage(byte[] message, ECKeyPair keyPair);
}
