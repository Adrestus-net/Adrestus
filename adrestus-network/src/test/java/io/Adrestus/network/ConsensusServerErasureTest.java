package io.Adrestus.network;

import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.BLSSignature;
import io.Adrestus.crypto.bls.model.Signature;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConsensusServerErasureTest {

    private static SerializationUtil<Signature> valueMapper;
    private static final String delimeter = "||";

    private static BLSPrivateKey sk1;
    private static BLSPublicKey vk1;

    @BeforeAll
    public static void setup() {
        List<SerializationUtil.Mapping> encodlist = new ArrayList<>();
        encodlist.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        encodlist.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        encodlist.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        valueMapper = new SerializationUtil<Signature>(Signature.class, encodlist);

        sk1 = new BLSPrivateKey(1);
        vk1 = new BLSPublicKey(sk1);
    }

    @Test
    public void test() throws DecoderException, InterruptedException {
        ConsensusServer adrestusServer = new ConsensusServer("localhost");
        final ConsensusClient[] adrestusClient1 = {null};
        (new Thread() {
            public void run() {
                List<String> list = new ArrayList<>();
                StringJoiner joiner = new StringJoiner(delimeter);
                String timeStampInString = GetTime.GetTimeStampInString();
                String pubkey = Hex.encodeHexString(vk1.toBytes());

                String toSign = joiner.add(pubkey).add(timeStampInString).toString();
                Signature bls_sig = BLSSignature.sign(toSign.getBytes(StandardCharsets.UTF_8), sk1);
                String sig = Hex.encodeHexString(valueMapper.encode(bls_sig));

                list.add(pubkey);
                list.add(timeStampInString);
                list.add(sig);

                String toSend = String.join(delimeter, list);
                adrestusClient1[0] = new ConsensusClient("localhost", toSign);
                byte[] rec = adrestusClient1[0].SendRetrieveErasureData(toSend.getBytes(StandardCharsets.UTF_8));
                System.out.println("edw :" + new String(rec, StandardCharsets.UTF_8));
                assertEquals("test", new String(rec, StandardCharsets.UTF_8));

            }
        }).start();

        String rec = new String(adrestusServer.receiveErasureData(), StandardCharsets.UTF_8);
        if (rec.equals(""))
            System.out.println("Timeout caught not receiving");
        else {
            StringJoiner joiner2 = new StringJoiner(delimeter);
            String[] splits = StringUtils.split(rec, delimeter);
            BLSPublicKey blsPublicKey = BLSPublicKey.fromByte(Hex.decodeHex(splits[0]));
            Timestamp timestamp = GetTime.GetTimestampFromString(splits[1]);
            boolean val = GetTime.CheckIfTimestampIsUnderOneMinute(timestamp);
            Signature bls_sig2 = valueMapper.decode(Hex.decodeHex(splits[2]));
            String strsgn = joiner2.add(Hex.encodeHexString(blsPublicKey.toBytes())).add(splits[1]).toString();
            Boolean signcheck = BLSSignature.verify(bls_sig2, strsgn.getBytes(StandardCharsets.UTF_8), blsPublicKey);
            if (signcheck) {
                adrestusServer.setErasureMessage("test".getBytes(StandardCharsets.UTF_8), strsgn);
            }
        }

        adrestusServer.close();
        adrestusClient1[0].close();
    }

//    @Test
//    public void test1() throws DecoderException, InterruptedException {
//        ConsensusServer adrestusServer = new ConsensusServer("localhost");
//        (new Thread() {
//            public void run() {
//                ConsensusClient adrestusClient1 = new ConsensusClient("localhost", "1");
//                adrestusClient1.SendErasureData("toSend".getBytes(StandardCharsets.UTF_8));
//                byte[] rec = adrestusClient1.receiveErasureData();
//                System.out.println("edw :" + new String(rec, StandardCharsets.UTF_8));
//            }
//        }).start();
//
//        String rec = new String(adrestusServer.receiveErasureData(), StandardCharsets.UTF_8);
//        adrestusServer.setErasureMessage("asdsa".getBytes(StandardCharsets.UTF_8), "1");
//    }
}
