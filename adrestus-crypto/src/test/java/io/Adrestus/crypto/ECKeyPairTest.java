package io.Adrestus.crypto;

import io.Adrestus.crypto.elliptic.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.DrbgParameters;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import static java.security.DrbgParameters.Capability.RESEED_ONLY;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ECKeyPairTest {
    static SecureRandom random;
    @BeforeAll
    public static void Setup() throws NoSuchAlgorithmException, NoSuchProviderException {
        String mnemonic_code="fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2";
        random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        random.setSeed(Hex.decode(mnemonic_code));
    }
    @Test
    public void verifyECKeyPairTest() throws Exception {
        ECKeyPair ecKeyPair = Keys.createEcKeyPair(random);
        String message = "verify test";
        ECDSASignature signature = ecKeyPair.sign(message.getBytes());
        boolean verify = ecKeyPair.verify(message.getBytes(StandardCharsets.UTF_8), signature);

        assertEquals(verify, true);
    }

    @Test
    public void verifySecp256ECDSASignTest() throws Exception {
        ECKeyPair ecKeyPair = Keys.createEcKeyPair(random);
        ECDSASign ecdsaSign = new ECDSASign();
        String message = "message";


        SignatureData signatureData = ecdsaSign.secp256SignMessage(message.getBytes(), ecKeyPair);
        String r = Hex.toHexString(signatureData.getR());
        String s = Hex.toHexString(signatureData.getS());

        boolean verify = ecdsaSign.secp256Verify(message.getBytes(StandardCharsets.UTF_8), ecKeyPair.getPublicKey(), signatureData);

        assertEquals(verify, true);

    }


}
