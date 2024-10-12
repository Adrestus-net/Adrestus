package io.Adrestus.core;

import io.Adrestus.core.mapper.CustomSerializerTreeMap;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.bls.model.*;
import io.Adrestus.crypto.elliptic.mapper.BigDecimalSerializer;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;

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
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
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

    @Test
    public void test2() throws DecoderException, ParseException {
        List<SerializationUtil.Mapping> encodlist = new ArrayList<>();
        encodlist.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        encodlist.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        encodlist.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));

        String delimeter = "||";
        List<String> list = new ArrayList<>();
        StringJoiner joiner = new StringJoiner(delimeter);
        String timeStampInString = GetTime.GetTimeStampInString();
        String pubkey = Hex.encodeHexString(vk1.toBytes());

        String toSign = joiner.add(pubkey).add(timeStampInString).toString();
        Signature bls_sig = BLSSignature.sign(toSign.getBytes(StandardCharsets.UTF_8), sk1);
        SerializationUtil<Signature> valueMapper = new SerializationUtil<Signature>(Signature.class, encodlist);
        String sig = Hex.encodeHexString(valueMapper.encode(bls_sig));

        list.add(pubkey);
        list.add(timeStampInString);
        list.add(sig);


        String toSend = String.join(delimeter, list);


        StringJoiner joiner2 = new StringJoiner(delimeter);
        String[] splits = StringUtils.split(toSend, delimeter);
        BLSPublicKey blsPublicKey = BLSPublicKey.fromByte(Hex.decodeHex(splits[0]));
        Timestamp timestamp = GetTime.GetTimestampFromString(splits[1]);
        boolean val = GetTime.CheckIfTimestampIsUnderOneMinute(timestamp);
        Signature bls_sig2 = valueMapper.decode(Hex.decodeHex(splits[2]));
        String strsgn = joiner2.add(Hex.encodeHexString(blsPublicKey.toBytes())).add(splits[1]).toString();
        Boolean signcheck = BLSSignature.verify(bls_sig2, strsgn.getBytes(StandardCharsets.UTF_8), blsPublicKey);
        int g = 3;
    }
}
