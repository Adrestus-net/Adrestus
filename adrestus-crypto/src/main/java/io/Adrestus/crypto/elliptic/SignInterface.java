package io.Adrestus.crypto.elliptic;

public interface SignInterface {
    SignatureData signMessage(byte[] message, ECKeyPair keyPair);
}
