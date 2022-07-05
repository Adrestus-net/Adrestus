package io.Adrestus.crypto.elliptic;

public interface SignInterface {
    Sign.SignatureData signMessage(byte[] message, ECKeyPair keyPair);
}
