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
        VdfEngine vdf = new VdfEnginePietrzak(2048);
        byte[] solution = vdf.solve(challenge, difficulty);
        assertEquals("003c27c656bd20796997ff0715e5e2dce3850f5bda38b2461c3a14026814b008c957dbabca624b54d51f5496b0acefc84749ed35a07b1cc26dead96289766740a0e1a9ac95d47eb402b5247a9b9225b0feda9c216ff59b1949f26aadaf8b38196a0fee56247337bf313446c2090ad6db356c47c5970a57f27246c799282733b6c7ffe5010b2b8c3486d5fe95c912918affb0edec2dff4f362f1cdd4981019c177d2bf62d1ff1b48309f43e242717e716e5c482d43d78fc30eb5f0e09ec0b0e8d88f9db71f37b1cbfb4e7de39b5dae7598f31c2f630e7bc6cb786ecf7e7dbaba2dbc8b686d0873fa5ac015fb8724e873279898de3164dc7ebc773a47d5d06f75fe03f",Hex.toHexString(solution));
        assertEquals(true,vdf.verify(challenge, difficulty, solution));
    }
}
