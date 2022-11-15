package io.Adrestus.crypto;

import io.Adrestus.crypto.vdf.engine.VdfEngine;
import io.Adrestus.crypto.vdf.engine.VdfEnginePietrzak;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.security.NoSuchAlgorithmException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VDFTest {
    private static byte[] challenge;
    private static long difficulty;
    private static Random random;

    @BeforeAll
    public static void Setup() {
        challenge = new byte[20];
        difficulty = 100;
        random = new Random();
        random.setSeed(200);
    }

    @Test
    public void test_vdf() throws NoSuchAlgorithmException {
        random.nextBytes(challenge);
        VdfEngine vdf = new VdfEnginePietrzak(2048);
        byte[] solution = vdf.solve(challenge, difficulty);
        assertEquals("0035e80b6a54563d78c34e365fd17f83fdb8946793b8d3c9fcda2bc257f57e806f72a8faee112a8b07c8cb4afa0d83a7f4e3bc87c4b2a63718a096f4bea1dc9fbb419c58131dc828ea9485ba24e8ef2ca5e68541545b859a106fc3d5da04ed6ede2614d7a7722334531cb4a8b9d6c2d50c68674c5e47b5eea2d89a720250fdf1d400134de5d604874b3c524a9ed4dfa4b892d8065d76f11d1d902561c56bc0960ed2f698caae3ab4c53e14b0cef2018313c6f4325944c44bebb56eb546871979b3242a938c9f3038e2ac2d9026b2c84867df2b2034aea6b2eec10c36b98975473a847cb004fb29a80e66777ed1e0feef4739b25a28defb6e1f7fc3e1e0985b2c6ecd", Hex.toHexString(solution));
        assertEquals(true, vdf.verify(challenge, difficulty, solution));
    }

    @Test
    public void test_vdf2() throws NoSuchAlgorithmException {
        VdfEngine vdf = new VdfEnginePietrzak(2048);
        byte[] solution = vdf.solve(Hex.decode("c1f72aa5bd1e1d53c723b149259b63f759f40d5ab003b547d5c13d45db9a5da8"), difficulty);
        assertEquals(true, vdf.verify(Hex.decode("c1f72aa5bd1e1d53c723b149259b63f759f40d5ab003b547d5c13d45db9a5da8"), difficulty, solution));
    }

    @Test
    public void streess_test_vdf() {
        int count = 10;
        while (count > 0) {
            random.nextBytes(challenge);
            VdfEngine vdf = new VdfEnginePietrzak(2048);
            byte[] solution = vdf.solve(challenge, difficulty);
            assertEquals(true, vdf.verify(challenge, difficulty, solution));
            count--;
            System.out.print(count);
        }
    }

}
