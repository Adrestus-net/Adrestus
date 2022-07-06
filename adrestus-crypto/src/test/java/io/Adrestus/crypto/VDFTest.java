package io.Adrestus.crypto;

import io.Adrestus.crypto.vdf.engine.VdfEngine;
import io.Adrestus.crypto.vdf.engine.VdfEnginePietrzak;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.*;

public class VDFTest {
    private static byte[] challenge;
    private static long difficulty;
    private static SecureRandom random;
    @BeforeAll
    public static void Setup(){
        challenge = new byte[20];
        difficulty = 100;
        random = new SecureRandom();
        random.setSeed(200);
    }
    @Test
    public void test_vdf(){

        random.nextBytes(challenge);
        System.out.println(Hex.toHexString(challenge));
        VdfEngine vdf = new VdfEnginePietrzak(2048);
        byte[] solution = vdf.solve(challenge, difficulty);
        System.out.println(Hex.toHexString(solution));
        assertEquals(true,vdf.verify(challenge, difficulty, solution));
    }
}
