package io.Adrestus.core;

import io.Adrestus.crypto.bls.model.*;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.util.SerializationUtil;
import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StressBLSTest {
    private static BLSPrivateKey sk1;
    private static BLSPublicKey vk1;

    private static BLSPrivateKey sk2;
    private static BLSPublicKey vk2;

    private static BLSPrivateKey sk3;
    private static BLSPublicKey vk3;

    private static BLSPrivateKey sk4;
    private static BLSPublicKey vk4;

    @Test
    public void Test() {
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        SerializationUtil<TransactionBlock> ser = new SerializationUtil<TransactionBlock>(TransactionBlock.class, list);
        sk1 = new BLSPrivateKey(1);
        vk1 = new BLSPublicKey(sk1);

        sk2 = new BLSPrivateKey(2);
        vk2 = new BLSPublicKey(sk2);

        sk3 = new BLSPrivateKey(3);
        vk3 = new BLSPublicKey(sk3);

        sk4 = new BLSPrivateKey(4);
        vk4 = new BLSPublicKey(sk4);

        BLSKeyPair keyPair1 = new BLSKeyPair(sk1, vk1);
        BLSKeyPair keyPair2 = new BLSKeyPair(sk2, vk2);
        BLSKeyPair keyPair3 = new BLSKeyPair(sk3, vk3);
        // BLSKeyPair keyPair4 = new BLSKeyPair(sk4, vk4);


        TransactionBlock block = new TransactionBlock();
        block.setHash("sadsadas");
        // Size is a problem during serilization/deserilization it must
        //be fixed or given statically
        Bytes message = Bytes.wrap(ser.encode(block, 1024));

        List<BLSPublicKey> publicKeys = Arrays.asList(keyPair1.getPublicKey(), keyPair2.getPublicKey(), keyPair3.getPublicKey());
        List<Signature> signatures = Arrays.asList(BLSSignature.sign(message.toArray(), keyPair1.getPrivateKey()), BLSSignature.sign(message.toArray(), keyPair2.getPrivateKey()), BLSSignature.sign(message.toArray(), keyPair3.getPrivateKey()));


        Signature aggregatedSignature = BLSSignature.aggregate(signatures);

        assertEquals(true, BLSSignature.fastAggregateVerify(publicKeys, message, aggregatedSignature));
        assertEquals(true, BLSSignature.fastAggregateVerify(publicKeys, message, aggregatedSignature));

        byte[] b = ser.encode(block, 1024);
        TransactionBlock copy = ser.decode(b);
        assertEquals(copy, block);
        Bytes message2 = Bytes.wrap(ser.encode(copy, 1024));
        assertEquals(true, BLSSignature.fastAggregateVerify(publicKeys, message2, aggregatedSignature));

        List<Signature> signatures2 = Arrays.asList(BLSSignature.sign(message.toArray(), keyPair1.getPrivateKey()), BLSSignature.sign(message.toArray(), keyPair2.getPrivateKey()), BLSSignature.sign(message.toArray(), keyPair3.getPrivateKey()));
        Signature aggregatedSignature2 = BLSSignature.aggregate(signatures2);

        Signature aggregatedSignature3 = BLSSignature.aggregate(signatures2);

        assertEquals(true, BLSSignature.fastAggregateVerify(publicKeys, message2, aggregatedSignature2));
        assertEquals(true, BLSSignature.fastAggregateVerify(publicKeys, message2, aggregatedSignature3));
    }
}
