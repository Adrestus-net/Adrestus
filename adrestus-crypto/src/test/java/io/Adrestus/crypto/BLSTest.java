package io.Adrestus.crypto;

import io.Adrestus.crypto.bls.model.*;
import io.Adrestus.crypto.bls.utils.MultiSigFastUtils;
import io.Adrestus.crypto.bls.utils.ThresholdSigUtils;
import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.*;
import org.spongycastle.util.encoders.Hex;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BLSTest {

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
        System.out.println(Hex.toHexString(sk.toBytes()));
        System.out.println(Hex.toHexString(vk.toBytes()));

        byte[] msg = "Test_Message".getBytes();
        Signature bls_sig = BLSSignature.sign(msg, sk);
        assertEquals(true, BLSSignature.verify(bls_sig, msg, vk, params));
    }

    @Test
    @Order(2)
    public void single_signature_random() {
        BLSPrivateKey sk = new BLSPrivateKey(new SecureRandom());
        BLSPublicKey vk = new BLSPublicKey(sk);
        // System.out.println(Hex.toHexString(sk.toBytes()));
        // System.out.println(Hex.toHexString(vk.toBytes()));

        byte[] msg = "Test_Message".getBytes();
        Signature bls_sig = BLSSignature.sign(msg, sk);
        assertEquals(true, BLSSignature.verify(bls_sig, msg, vk));
    }

    @Test
    @Order(3)
    public void multi_sig_fast() {
        byte[] msg = "Test_Message".getBytes();
        BLSPrivateKey sk1 = new BLSPrivateKey(new SecureRandom());
        BLSPublicKey vk1 = new BLSPublicKey(sk1);

        BLSPrivateKey sk2 = new BLSPrivateKey(new SecureRandom());
        BLSPublicKey vk2 = new BLSPublicKey(sk2);

        BLSKeyPair keypair1 = new BLSKeyPair(sk1, vk1);
        BLSKeyPair keypair2 = new BLSKeyPair(sk2, vk2);

        Signature sig1 = BLSSignature.sign(msg, keypair1.getPrivateKey());
        Signature sig2 = BLSSignature.sign(msg, keypair2.getPrivateKey());
        Signature sig = MultiSigFastUtils.mergeSignature(new Signature[]{sig1, sig2}, new BLSPublicKey[]{keypair1.getPublicKey(), keypair2.getPublicKey()});
        assertEquals(true, MultiSigFastUtils.verify(sig, msg, new BLSPublicKey[]{keypair1.getPublicKey(), keypair2.getPublicKey()}));
    }

    @Test
    @Order(4)
    public void Threshold_sig_app() {
        int threshold = 4;
        int total = 10;
        byte[] msg = "Test_Message".getBytes();
        Object[] objs = ThresholdSigUtils.trustedPartySSSKeygen(threshold, total, params);
        List<Signer> signers = (List<Signer>) objs[1];

        // Random sigkeys
        List<BLSPrivateKey> privkey = new ArrayList<>();
        List<Integer> sigIds = new ArrayList<>();

        for (int i : new int[]{8, 6, 2, 4}) {
            sigIds.add(signers.get(i).id);
            privkey.add(signers.get(i).getPrivateKey());
        }

        // Random verkeys
        List<BLSPublicKey> pubkey = new ArrayList<>();
        List<Integer> verIds = new ArrayList<>();

        for (int i : new int[]{5, 3, 1, 7}) {
            pubkey.add(signers.get(i).getPublicKey());
            verIds.add(signers.get(i).id);
        }

        BLSPublicKey thresholdVk = ThresholdSigUtils.aggregateVerkey(threshold, verIds, pubkey);
        List<Signature> sigs = new ArrayList<>();
        for (BLSPrivateKey sigkey : privkey) {
            sigs.add(BLSSignature.sign(msg, sigkey));
        }

        Signature thresholdSig = ThresholdSigUtils.aggregateSignature(threshold, sigIds, sigs);

        assertEquals(true, BLSSignature.verify(thresholdSig, msg, thresholdVk, params));
    }

    @Test
    @Order(5)
    public void fastAggregateVerify1() {
        Bytes message = Bytes.wrap("Hello, world 1!".getBytes(UTF_8));
        BLSPrivateKey sk1 = new BLSPrivateKey(new SecureRandom());
        BLSPublicKey vk1 = new BLSPublicKey(sk1);

        BLSPrivateKey sk2 = new BLSPrivateKey(new SecureRandom());
        BLSPublicKey vk2 = new BLSPublicKey(sk2);

        BLSPrivateKey sk3 = new BLSPrivateKey(new SecureRandom());
        BLSPublicKey vk3 = new BLSPublicKey(sk3);

        BLSKeyPair keyPair1 = new BLSKeyPair(sk1, vk1);
        BLSKeyPair keyPair2 = new BLSKeyPair(sk2, vk2);
        BLSKeyPair keyPair3 = new BLSKeyPair(sk3, vk3);
        List<BLSPublicKey> publicKeys = Arrays.asList(keyPair1.getPublicKey(), keyPair2.getPublicKey(), keyPair3.getPublicKey());
        List<Signature> signatures = Arrays.asList(BLSSignature.sign(message.toArray(), keyPair1.getPrivateKey()), BLSSignature.sign(message.toArray(), keyPair2.getPrivateKey()), BLSSignature.sign(message.toArray(), keyPair3.getPrivateKey()));
        Signature aggregatedSignature = BLSSignature.aggregate(signatures);
        assertEquals(true, BLSSignature.fastAggregateVerify(publicKeys, message, aggregatedSignature));
    }

    @Test
    @Order(6)
    public void succeedsWhenAggregateVerifyWithRepeatedMessagesReturnsFalse() {
        Bytes message = Bytes.wrap("Hello, world 1!".getBytes(UTF_8));
        BLSPrivateKey sk1 = new BLSPrivateKey(new SecureRandom());
        BLSPublicKey vk1 = new BLSPublicKey(sk1);

        BLSPrivateKey sk2 = new BLSPrivateKey(new SecureRandom());
        BLSPublicKey vk2 = new BLSPublicKey(sk2);

        BLSPrivateKey sk3 = new BLSPrivateKey(new SecureRandom());
        BLSPublicKey vk3 = new BLSPublicKey(sk3);

        BLSKeyPair keyPair1 = new BLSKeyPair(sk1, vk1);
        BLSKeyPair keyPair2 = new BLSKeyPair(sk2, vk2);
        BLSKeyPair keyPair3 = new BLSKeyPair(sk3, vk3);
        List<Bytes> messages = Arrays.asList(message, message, message);
        List<BLSPublicKey> publicKeys = Arrays.asList(keyPair1.getPublicKey(), keyPair2.getPublicKey(), keyPair3.getPublicKey());
        List<Signature> signatures = Arrays.asList(BLSSignature.sign(message.toArray(), keyPair1.getPrivateKey()), BLSSignature.sign(message.toArray(), keyPair2.getPrivateKey()), BLSSignature.sign(message.toArray(), keyPair3.getPrivateKey()));
        Signature aggregatedSignature = BLSSignature.aggregate(signatures);
        assertEquals(false, BLSSignature.aggregateVerify(publicKeys, messages, aggregatedSignature));
    }
}
