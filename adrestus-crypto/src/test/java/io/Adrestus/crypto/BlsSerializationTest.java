package io.Adrestus.crypto;

import io.Adrestus.crypto.bls.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlsSerializationTest {

    private static BLSKeyPair keypair;
    private static Params params;

    @BeforeAll
    public static void Setup() {
        params = new Params("testss".getBytes());
    }

    @Test
    @Order(1)
    public void single_signature_with_entropy() {
        BLSPrivateKey sk = new BLSPrivateKey(42);
        BLSPublicKey vk = new BLSPublicKey(sk, params);
        byte[] msg = "Test_Message".getBytes();
        Signature bls_sig = BLSSignature.sign(msg, sk);

        for(int i=0;i<200;i++) {
            BLSPublicKey clonevk = BLSPublicKey.fromByte(vk.toBytes());
            Signature clone_sig = Signature.fromByte(bls_sig.toBytes());

            assertEquals(bls_sig, clone_sig);
            assertEquals(vk, clonevk);
        }
    }
}
