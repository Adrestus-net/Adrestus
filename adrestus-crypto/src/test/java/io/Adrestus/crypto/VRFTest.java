package io.Adrestus.crypto;
import io.Adrestus.crypto.vrf.engine.VrfEngine;
import io.Adrestus.crypto.vrf.utils.VrfUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VRFTest {
    @BeforeAll
    public static void Setup(){

    }

    @Test
    public void test_vrf() throws Exception {
        VrfEngine group = new VrfEngine("secp256k1");
        byte[] secret_key = VrfUtils.hexStringToByteArray("c9afa9d845ba75166b5c215767b1d6934e50c3db36eb127b8a622b120f6721");
        byte[] public_key  = group.derivePublicKey(secret_key);
        byte[] msg = "this is a test".getBytes();
        byte[] pi = group.prove(secret_key, msg);
        byte[] hash = group.proofToHash(pi);
        byte[] beta = group.verify(public_key,pi, msg);
        assertEquals(Hex.toHexString(beta),Hex.toHexString(hash));
    }
}
