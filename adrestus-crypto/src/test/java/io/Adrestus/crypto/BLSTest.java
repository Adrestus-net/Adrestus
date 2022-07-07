package io.Adrestus.crypto;

import io.Adrestus.crypto.bls.model.*;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.apache.milagro.amcl.RAND;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BLSTest {

    private static Keypair keypair;
    private static Params params;
    @BeforeAll
    public static void Setup(){
        params = new Params("testss".getBytes());
        keypair = new Keypair(43, params);
    }
    @Test
    public void single_signature_with_entropy(){
        /*Pairing pairing= PairingFactory.getPairing("C:\\Users\\User\\Documents\\GitHub\\Adrestus\\adrestus-crypto\\src\\test\\java\\io\\Adrestus\\crypto\\curve.properties");
        Element g = pairing.getG1().newRandomElement();
        Element priv = pairing.getZr().newRandomElement();*/
        SigKey sk = new SigKey(42);
        VerKey vk = new VerKey(sk, params);
        System.out.println(Hex.toHexString(sk.toBytes()));
        System.out.println(Hex.toHexString(vk.toBytes()));

        byte[] msg = "Test_Message".getBytes();
        Signature sig = new Signature(msg, sk);
        assertEquals(true,sig.verify(msg, vk, params));
    }
    @Test
    public void single_signature_random(){
        SigKey sk = new SigKey(new SecureRandom());
        VerKey vk = new VerKey(sk);
       // System.out.println(Hex.toHexString(sk.toBytes()));
       // System.out.println(Hex.toHexString(vk.toBytes()));

        byte[] msg = "Test_Message".getBytes();
        Signature sig = new Signature(msg, sk);
        assertEquals(true,sig.verify(msg, vk));
    }
    @Test
    public void multi_sig_slow(){

    }
    @Test
    public void threshold_sig(){

    }
}
