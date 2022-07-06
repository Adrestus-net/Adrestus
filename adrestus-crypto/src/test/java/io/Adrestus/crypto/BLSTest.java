package io.Adrestus.crypto;

import io.Adrestus.crypto.bls.model.*;
import org.apache.milagro.amcl.RAND;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BLSTest {

    private static Keypair keypair;
    private static Params params;
    @BeforeAll
    public static void Setup(){
        RAND r = new RAND();
        r.sirand(4);
        params = new Params("test".getBytes());
        keypair = new Keypair(r, params);
    }
    @Test
    public void single_signature(){
        SigKey sk = keypair.sigKey;
        VerKey vk = keypair.verKey;
        System.out.println(Hex.toHexString(sk.toBytes()));
        System.out.println(Hex.toHexString(vk.toBytes()));

        byte[] msg = "Test_Message".getBytes();
        Signature sig = new Signature(msg, sk);
        assertEquals(true,sig.verify(msg, vk, params));
    }
    @Test
    public void multi_sig_slow(){

    }
    @Test
    public void threshold_sig(){

    }
}
