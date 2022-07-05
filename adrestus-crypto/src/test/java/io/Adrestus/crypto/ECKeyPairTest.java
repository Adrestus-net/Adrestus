package io.Adrestus.crypto;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.Adrestus.crypto.elliptic.ECDSASignature;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class ECKeyPairTest {

    @Test
    public void verifyECKeyPairTest() throws Exception {
        ECKeyPair ecKeyPair = Keys.createEcKeyPair(new SecureRandom("fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2".getBytes(StandardCharsets.UTF_8)));
        String message = "verify test";
        ECDSASignature signature = ecKeyPair.sign(message.getBytes());
        boolean verify = ecKeyPair.verify(message.getBytes(), signature);

        assertEquals(verify, true);
    }
}
