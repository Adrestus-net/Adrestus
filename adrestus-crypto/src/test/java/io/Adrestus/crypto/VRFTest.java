package io.Adrestus.crypto;

import io.Adrestus.crypto.bls.model.BLSKeyPair;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.Params;
import io.Adrestus.crypto.vrf.engine.VrfEngine;
import io.Adrestus.crypto.vrf.engine.VrfEngine2;
import io.Adrestus.crypto.vrf.utils.VrfUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VRFTest {
    private static BLSKeyPair keypair;
    private static Params params;
    private static int leftLimit;
    private static int rightLimit;
    private static int targetStringLength;
    private static Random random;


    @BeforeAll
    public static void Setup() {
        params = new Params("testss".getBytes());
        leftLimit = 97; // letter 'a'
        rightLimit = 122; // letter 'z'
        targetStringLength = 10;
        random = new Random();
    }

    @Test
    public void test_vrf() throws Exception {
        BLSPrivateKey sk = new BLSPrivateKey(42);
        BLSPublicKey vk = new BLSPublicKey(sk, params);
        VrfEngine group = new VrfEngine("secp256k1");
        byte[] secret_key = VrfUtils.hexStringToByteArray("00000000000000000000000000000000119f962794f815e4f21ae048e66286a930f69b51bbc711367a6a4b3a57e3f5ff");
        //byte[] public_key  = group.derivePublicKey(secret_key);
        byte[] public_key = group.derivePublicKey(sk.toBytes());
        //assertEquals(Hex.toHexString(public_key),Hex.toHexString(vk.toBytes()));
        //assertEquals("0408446c7bfd9b7b17f61f1e9efb7f08a8f2289e12970a66104c4822b0d8d7a914342a12acd82f7bad82021bd4c5577ea70fe302c37552e2dc376e2d613cd97233cee3b3218a70e8c8acfb85a600c13351d1e4176df3b056da22f7ee3eff182256",Hex.toHexString(public_key));
        byte[] msg = "this is a test".getBytes();
        byte[] pi = group.prove(sk.toBytes(), msg);
        //System.out.println("1a: "+Hex.toHexString(pi));
        byte[] hash = group.proofToHash(pi);
        // System.out.println("2a: "+Hex.toHexString(hash));
        byte[] beta = group.verify(public_key, pi, msg);
        assertEquals(Hex.toHexString(beta), Hex.toHexString(hash));
    }

    @Test
    public void test_vrf2() throws Exception {
        BLSPrivateKey sk = new BLSPrivateKey(42);
        BLSPublicKey vk = new BLSPublicKey(sk);


        VrfEngine2 group = new VrfEngine2();
        byte[] msg = "this is a testsdjkfhsdjklglkifsjlgfsdfjklsd".getBytes();
        byte[] ri = group.prove(sk.toBytes(), msg);
        byte[] pi = group.proofToHash(ri);
        //System.out.println("1b: "+Hex.toHexString(pi));
        // System.out.println("2b: "+Hex.toHexString(hash));
        byte[] beta = group.verify(vk.toBytes(), ri, msg);
        //System.out.println(Hex.toHexString(beta));
        assertEquals(Hex.toHexString(beta), Hex.toHexString(pi));
    }

    @Test
    public void loop_vrf2() throws Exception {
        BLSPrivateKey sk = new BLSPrivateKey(42);
        BLSPublicKey vk = new BLSPublicKey(sk);

        VrfEngine2 group = new VrfEngine2();

        for (int i = 0; i < 100; i++) {
            String generatedString = random.ints(leftLimit, rightLimit + 1)
                    .limit(targetStringLength)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
            byte[] msg = generatedString.getBytes();
            byte[] pi = group.prove(sk.toBytes(), msg);
            byte[] hash = group.proofToHash(pi);
            byte[] beta = group.verify(vk.toBytes(), pi, msg);
            assertEquals(Hex.toHexString(beta), Hex.toHexString(hash));
        }
    }
}
  /* @Test
   public void test_vrf3() throws Exception {
       SigKey sk = new SigKey(42);
       VerKey vk = new VerKey(sk);

       VrfEngine2 group = new VrfEngine2();
       byte[] msg = "this is a test".getBytes();
       while (true) {
           byte[] pi = group.prove(sk.toBytes(), msg);
           byte[] hash = group.proofToHash(pi);
           byte[] beta = group.verify(vk.toBytes(), pi, msg);
       }
   }*/

