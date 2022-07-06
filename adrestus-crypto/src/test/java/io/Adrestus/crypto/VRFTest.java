package io.Adrestus.crypto;
import io.Adrestus.crypto.bls.model.Keypair;
import io.Adrestus.crypto.bls.model.Params;
import io.Adrestus.crypto.bls.model.SigKey;
import io.Adrestus.crypto.bls.model.VerKey;
import io.Adrestus.crypto.vrf.engine.VrfEngine;
import io.Adrestus.crypto.vrf.utils.VrfUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VRFTest {
    private static Keypair keypair;
    private static Params params;
    @BeforeAll
    public static void Setup(){
        params = new Params("testss".getBytes());
        keypair = new Keypair(43, params);
    }

    @Test
    public void test_vrf() throws Exception {
        SigKey sk = new SigKey(42);
        VerKey vk = new VerKey(sk, params);
        VrfEngine group = new VrfEngine("secp256k1");
        byte[] secret_key = VrfUtils.hexStringToByteArray("00000000000000000000000000000000119f962794f815e4f21ae048e66286a930f69b51bbc711367a6a4b3a57e3f5ff");
        //byte[] public_key  = group.derivePublicKey(secret_key);
        byte[] public_key  = group.derivePublicKey(sk.toBytes());
        //assertEquals(Hex.toHexString(public_key),Hex.toHexString(vk.toBytes()));
        //assertEquals("0408446c7bfd9b7b17f61f1e9efb7f08a8f2289e12970a66104c4822b0d8d7a914342a12acd82f7bad82021bd4c5577ea70fe302c37552e2dc376e2d613cd97233cee3b3218a70e8c8acfb85a600c13351d1e4176df3b056da22f7ee3eff182256",Hex.toHexString(public_key));
        byte[] msg = "this is a test".getBytes();
        byte[] pi = group.prove(sk.toBytes(), msg);
        byte[] hash = group.proofToHash(pi);
        byte[] beta = group.verify(public_key,pi, msg);
        assertEquals(Hex.toHexString(beta),Hex.toHexString(hash));
    }
}
