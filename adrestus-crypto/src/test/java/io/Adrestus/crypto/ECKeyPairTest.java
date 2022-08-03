package io.Adrestus.crypto;

import io.Adrestus.crypto.elliptic.*;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ECKeyPairTest {
    public static interface RustLib {
        int double_input(int i);
    }

    public static String getLibraryPath(String dylib) {
        File f = new File("C:\\Users\\User\\Documents\\GitHub\\Adrestus\\adrestus-crypto\\src\\test\\java\\io\\Adrestus\\crypto\\libverification.rlib");
        return f.getParent();
    }

    @Test
    public void verifyECKeyPairTest() throws Exception {
        ECKeyPair ecKeyPair = Keys.createEcKeyPair(new SecureRandom("fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2".getBytes(StandardCharsets.UTF_8)));
        String message = "verify test";
        ECDSASignature signature = ecKeyPair.sign(message.getBytes());
        boolean verify = ecKeyPair.verify(message.getBytes(StandardCharsets.UTF_8), signature);

        assertEquals(verify, true);
    }

    @Test
    public void verifySecp256ECDSASignTest() throws Exception {
        ECKeyPair ecKeyPair = Keys.createEcKeyPair(new SecureRandom("fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2".getBytes(StandardCharsets.UTF_8)));
        ECDSASign ecdsaSign = new ECDSASign();
        String message = "message";


        SignatureData signatureData = ecdsaSign.secp256SignMessage(message.getBytes(), ecKeyPair);
        String r = Hex.toHexString(signatureData.getR());
        String s = Hex.toHexString(signatureData.getS());
        byte[] hash = HashUtil.sha256(message.getBytes());

        boolean verify = ecdsaSign.secp256Verify(hash, ecKeyPair.getPublicKey(), signatureData);

        assertEquals(verify, true);

    }


}
