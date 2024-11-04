package io.Adrestus.core;

import io.Adrestus.core.Util.BlockSizeCalculator;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.BLSSignatureData;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.BLSSignature;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.crypto.elliptic.ECDSASignatureData;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.mapper.BigDecimalSerializer;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomSerializerTreeMap;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.util.SerializationUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SerializableBlockTest {
    private static ArrayList<String> addreses = new ArrayList<>();
    private static ArrayList<ECKeyPair> keypair = new ArrayList<>();
    private static ArrayList<Transaction> transactions = new ArrayList<>();
    private static ArrayList<Transaction> outer_transactions = new ArrayList<>();
    private static SerializationUtil<AbstractBlock> serenc;
    private static SerializationUtil<Transaction> trx_serence;
    private static ECDSASign ecdsaSign = new ECDSASign();
    private static BLSPrivateKey sk1;
    private static BLSPublicKey vk1;

    private static BLSPrivateKey sk2;
    private static BLSPublicKey vk2;

    private static BLSPrivateKey sk3;
    private static BLSPublicKey vk3;

    private static BLSPrivateKey sk4;
    private static BLSPublicKey vk4;


    private static BLSPrivateKey sk5;
    private static BLSPublicKey vk5;

    private static BLSPrivateKey sk6;
    private static BLSPublicKey vk6;

    private static BLSPrivateKey sk7;
    private static BLSPublicKey vk7;

    private static BLSPrivateKey sk8;
    private static BLSPublicKey vk8;

    private static BLSPrivateKey sk9;
    private static BLSPublicKey vk9;

    private static BlockSizeCalculator sizeCalculator;
    private static KademliaData kad1, kad2, kad3, kad4, kad5, kad6;
    private static ECDSASignatureData signatureData1, signatureData2, signatureData3;
    private static Callback transactionCallback;
    private static int version = 0x00;
    private static int size = 5;
    private static ECKeyPair ecKeyPair1, ecKeyPair2, ecKeyPair3;
    private static String address1, address2, address3;

    @SneakyThrows
    @BeforeAll
    public static void setup() {
        sizeCalculator = new BlockSizeCalculator();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        serenc = new SerializationUtil<AbstractBlock>(AbstractBlock.class, list);

        sk1 = new BLSPrivateKey(1);
        vk1 = new BLSPublicKey(sk1);

        sk2 = new BLSPrivateKey(2);
        vk2 = new BLSPublicKey(sk2);

        sk3 = new BLSPrivateKey(3);
        vk3 = new BLSPublicKey(sk3);

        sk4 = new BLSPrivateKey(4);
        vk4 = new BLSPublicKey(sk4);


        sk5 = new BLSPrivateKey(5);
        vk5 = new BLSPublicKey(sk5);

        sk6 = new BLSPrivateKey(6);
        vk6 = new BLSPublicKey(sk6);

        sk7 = new BLSPrivateKey(7);
        vk7 = new BLSPublicKey(sk7);

        sk8 = new BLSPrivateKey(8);
        vk8 = new BLSPublicKey(sk8);
    }

//    @Test
//    public void TransactionBlocktest1() {
//        TransactionBlock prevblock = new TransactionBlock();
//        CommitteeBlock committeeBlock = new CommitteeBlock();
//        committeeBlock.setGeneration(1);
//        committeeBlock.setViewID(1);
//        prevblock.setHeight(1);
//        prevblock.setHash("hash");
//        prevblock.getSignatureData().put(vk1, new BLSSignatureData());
//        prevblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
//        BlockSizeCalculator blockSizeCalculator = new BlockSizeCalculator();
//        blockSizeCalculator.setTransactionBlock(prevblock);
//        byte[] tohash = serenc.encode(prevblock, blockSizeCalculator.TransactionBlockSizeCalculator());
//        TransactionBlock newblock = (TransactionBlock) serenc.decode(tohash);
//        assertEquals(prevblock, newblock);
//    }
//
//    @SneakyThrows
//    @Test
//    public void TransactionBlocktest2() {
//        TransactionBlock prevblock = new TransactionBlock();
//        CommitteeBlock committeeBlock = new CommitteeBlock();
//        committeeBlock.setGeneration(1);
//        committeeBlock.setViewID(1);
//        prevblock.setHeight(1);
//        prevblock.setHash("hash");
//        prevblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
//        BlockSizeCalculator blockSizeCalculator = new BlockSizeCalculator();
//        blockSizeCalculator.setTransactionBlock(prevblock);
//        byte[] data = serenc.encode(prevblock, blockSizeCalculator.TransactionBlockSizeCalculator());
//        TransactionBlock newblock = (TransactionBlock) serenc.decode(data);
//        TransactionBlock cloned_newblock = (TransactionBlock) newblock.clone();
//        assertEquals(prevblock, newblock);
//        assertEquals(prevblock, cloned_newblock);
//    }
//
//    @Test
//    public void CommitteeBlocktest2() throws MnemonicException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
//        char[] mnemonic1 = "sample sail jungle learn general promote task puppy own conduct green affair ".toCharArray();
//        char[] mnemonic2 = "photo monitor cushion indicate civil witness orchard estate online favorite sustain extend".toCharArray();
//        char[] mnemonic3 = "struggle travel ketchup tomato satoshi caught fog process grace pupil item ahead ".toCharArray();
//        char[] passphrase = "p4ssphr4se".toCharArray();
//        Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
//        byte[] key1 = mnem.createSeed(mnemonic1, passphrase);
//        byte[] key2 = mnem.createSeed(mnemonic2, passphrase);
//        byte[] key3 = mnem.createSeed(mnemonic3, passphrase);
//        SecureRandom random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
//        random.setSeed(key1);
//        ecKeyPair1 = Keys.createEcKeyPair(random);
//        random.setSeed(key2);
//        ecKeyPair2 = Keys.createEcKeyPair(random);
//        random.setSeed(key3);
//        ecKeyPair3 = Keys.createEcKeyPair(random);
//
//        address1 = WalletAddress.generate_address((byte) version, ecKeyPair1.getPublicKey());
//        address2 = WalletAddress.generate_address((byte) version, ecKeyPair2.getPublicKey());
//        address3 = WalletAddress.generate_address((byte) version, ecKeyPair3.getPublicKey());
//
//        ECDSASignatureData signatureData1 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address1)), ecKeyPair1);
//        ECDSASignatureData signatureData2 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address2)), ecKeyPair2);
//        ECDSASignatureData signatureData3 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address3)), ecKeyPair3);
//
//        TreeFactory.getMemoryTree(0).store(address1, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
//        TreeFactory.getMemoryTree(0).store(address2, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
//        TreeFactory.getMemoryTree(0).store(address3, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
//
//        CommitteeBlock committeeBlock = new CommitteeBlock();
//        committeeBlock.getHeaderData().setTimestamp("2022-11-18 15:01:29.304");
//        committeeBlock.getStructureMap().get(0).put(vk1, "192.168.1.106");
//        committeeBlock.getStructureMap().get(0).put(vk2, "192.168.1.116");
//        committeeBlock.getStructureMap().get(0).put(vk3, "192.168.1.115");
//
//        committeeBlock.getSignatureData().put(vk1, new BLSSignatureData());
//        committeeBlock.getStakingMap().put(new StakingData(1, BigDecimal.valueOf(10.0)), new KademliaData(new SecurityAuditProofs(address1, vk1, ecKeyPair1.getPublicKey(), signatureData1), new NettyConnectionInfo("192.168.1.106", KademliaConfiguration.PORT)));
//        committeeBlock.getStakingMap().put(new StakingData(2, BigDecimal.valueOf(13.0)), new KademliaData(new SecurityAuditProofs(address2, vk2, ecKeyPair2.getPublicKey(), signatureData2), new NettyConnectionInfo("192.168.1.116", KademliaConfiguration.PORT)));
//        committeeBlock.getStakingMap().put(new StakingData(2, BigDecimal.valueOf(17.0)), new KademliaData(new SecurityAuditProofs(address3, vk3, ecKeyPair3.getPublicKey(), signatureData3), new NettyConnectionInfo("192.168.1.115", KademliaConfiguration.PORT)));
//
//        BlockSizeCalculator blockSizeCalculator = new BlockSizeCalculator();
//        blockSizeCalculator.setCommitteeBlock(committeeBlock);
//        byte[] data = serenc.encode(committeeBlock, blockSizeCalculator.CommitteeBlockSizeCalculator());
//        CommitteeBlock newblock = (CommitteeBlock) serenc.decode(data);
//        assertEquals(committeeBlock, newblock);
//    }
//
//    @Test
//    public void HashmapTest() {
//        HashMap<BLSPublicKey, BLSSignatureData> hashMap = new HashMap<BLSPublicKey, BLSSignatureData>();
//        BLSSignatureData blsSignatureData1 = new BLSSignatureData();
//        BLSSignatureData blsSignatureData2 = new BLSSignatureData();
//        BLSSignatureData blsSignatureData3 = new BLSSignatureData();
//        BLSSignatureData blsSignatureData4 = new BLSSignatureData();
//        hashMap.put(vk3, blsSignatureData1);
//        hashMap.put(vk4, blsSignatureData2);
//        hashMap.put(vk1, blsSignatureData3);
//        hashMap.put(vk2, blsSignatureData4);
//
//        AbstractBlock block = new TransactionBlock();
//        block.AddAllSignatureData(hashMap);
//        blsSignatureData1.getMessageHash()[0] = "test";
//        hashMap.put(vk3, blsSignatureData1);
//        block.AddAllSignatureData(hashMap);
//        TreeMap<BLSPublicKey, BLSSignatureData> final_block = block.getSignatureData();
//        System.out.println(vk1.getPoint().toString());
//        System.out.println(vk2.getPoint().toString());
//        System.out.println(vk3.getPoint().toString());
//        System.out.println(vk4.getPoint().toString());
//        assertEquals(vk3, final_block.keySet().toArray()[0]);
//        assertEquals(vk1, final_block.keySet().toArray()[1]);
//        assertEquals(vk4, final_block.keySet().toArray()[2]);
//        assertEquals(vk2, final_block.keySet().toArray()[3]);
//        assertEquals("test", final_block.get(vk3).getMessageHash()[0]);
//    }

    @SneakyThrows
    @Test
    public void CloneTest() {
        HashMap<BLSPublicKey, BLSSignatureData> hashMap = new HashMap<BLSPublicKey, BLSSignatureData>();
        BLSSignatureData blsSignatureData1 = new BLSSignatureData();
        blsSignatureData1.getSignature()[0] = BLSSignature.sign("0".getBytes(StandardCharsets.UTF_8), sk1);
        blsSignatureData1.getSignature()[1] = BLSSignature.sign("1".getBytes(StandardCharsets.UTF_8), sk1);
        blsSignatureData1.getMessageHash()[0] = "0";
        blsSignatureData1.getMessageHash()[1] = "1";
        BLSSignatureData blsSignatureData2 = new BLSSignatureData();
        blsSignatureData2.getSignature()[0] = BLSSignature.sign("0".getBytes(StandardCharsets.UTF_8), sk2);
        blsSignatureData2.getSignature()[1] = BLSSignature.sign("1".getBytes(StandardCharsets.UTF_8), sk2);
        blsSignatureData2.getMessageHash()[0] = "0";
        blsSignatureData2.getMessageHash()[1] = "1";
        BLSSignatureData blsSignatureData3 = new BLSSignatureData();
        blsSignatureData3.getSignature()[0] = BLSSignature.sign("0".getBytes(StandardCharsets.UTF_8), sk3);
        blsSignatureData3.getSignature()[1] = BLSSignature.sign("1".getBytes(StandardCharsets.UTF_8), sk3);
        blsSignatureData3.getMessageHash()[0] = "0";
        blsSignatureData3.getMessageHash()[1] = "1";
        BLSSignatureData blsSignatureData4 = new BLSSignatureData();
        blsSignatureData4.getSignature()[0] = BLSSignature.sign("0".getBytes(StandardCharsets.UTF_8), sk4);
        blsSignatureData4.getSignature()[1] = BLSSignature.sign("1".getBytes(StandardCharsets.UTF_8), sk4);
        blsSignatureData4.getMessageHash()[0] = "0";
        blsSignatureData4.getMessageHash()[1] = "1";
        hashMap.put(vk1, blsSignatureData1);
        hashMap.put(vk2, blsSignatureData2);
        hashMap.put(vk3, blsSignatureData3);
        hashMap.put(vk4, blsSignatureData4);

        TransactionBlock block = new TransactionBlock();
        block.setHash("1");
        block.setHeight(1);
        block.AddAllSignatureData(hashMap);

        TransactionBlock clone1 = (TransactionBlock) block.clone();
        for (int i = 0; i < 1000; i++) {
            long start = System.currentTimeMillis();
            TransactionBlock clone = (TransactionBlock) block.clone();
            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;
            if (System.out.getClass().getName().contains("maven")) {
                assertTrue(timeElapsed < 10, "Assert true");
            }
        }
    }

//    @Test
//    public void HashSignatureSerializationTest() {
//        HashMap<BLSPublicKey, BLSSignatureData> hashMap = new HashMap<BLSPublicKey, BLSSignatureData>();
//        BLSSignatureData blsSignatureData1 = new BLSSignatureData();
//        blsSignatureData1.getSignature()[0] = BLSSignature.sign("0".getBytes(StandardCharsets.UTF_8), sk1);
//        blsSignatureData1.getSignature()[1] = BLSSignature.sign("1".getBytes(StandardCharsets.UTF_8), sk1);
//        blsSignatureData1.getMessageHash()[0] = "0";
//        blsSignatureData1.getMessageHash()[1] = "1";
//        BLSSignatureData blsSignatureData2 = new BLSSignatureData();
//        blsSignatureData2.getSignature()[0] = BLSSignature.sign("0".getBytes(StandardCharsets.UTF_8), sk2);
//        blsSignatureData2.getSignature()[1] = BLSSignature.sign("1".getBytes(StandardCharsets.UTF_8), sk2);
//        blsSignatureData2.getMessageHash()[0] = "0";
//        blsSignatureData2.getMessageHash()[1] = "1";
//        BLSSignatureData blsSignatureData3 = new BLSSignatureData();
//        blsSignatureData3.getSignature()[0] = BLSSignature.sign("0".getBytes(StandardCharsets.UTF_8), sk3);
//        blsSignatureData3.getSignature()[1] = BLSSignature.sign("1".getBytes(StandardCharsets.UTF_8), sk3);
//        blsSignatureData3.getMessageHash()[0] = "0";
//        blsSignatureData3.getMessageHash()[1] = "1";
//        BLSSignatureData blsSignatureData4 = new BLSSignatureData();
//        blsSignatureData4.getSignature()[0] = BLSSignature.sign("0".getBytes(StandardCharsets.UTF_8), sk4);
//        blsSignatureData4.getSignature()[1] = BLSSignature.sign("1".getBytes(StandardCharsets.UTF_8), sk4);
//        blsSignatureData4.getMessageHash()[0] = "0";
//        blsSignatureData4.getMessageHash()[1] = "1";
//        hashMap.put(vk1, blsSignatureData1);
//        hashMap.put(vk2, blsSignatureData2);
//        hashMap.put(vk3, blsSignatureData3);
//        hashMap.put(vk4, blsSignatureData4);
//
//        TransactionBlock block = new TransactionBlock();
//        block.setHash("1");
//        block.setHeight(1);
//        block.AddAllSignatureData(hashMap);
//
//        BlockSizeCalculator blockSizeCalculator = new BlockSizeCalculator();
//        blockSizeCalculator.setTransactionBlock(block);
//        byte[] data = serenc.encode(block, blockSizeCalculator.TransactionBlockSizeCalculator());
//        String hash1 = HashUtil.sha256_bytetoString(data);
//
//        HashMap<BLSPublicKey, BLSSignatureData> hashMap1 = new HashMap<BLSPublicKey, BLSSignatureData>();
//        BLSSignatureData blsSignatureData1a = new BLSSignatureData(2);
//        blsSignatureData1a.getSignature()[0] = new Signature(blsSignatureData1.getSignature()[0].getPoint());
//        blsSignatureData1a.getSignature()[1] = BLSSignature.sign("1".getBytes(StandardCharsets.UTF_8), sk1);
//        blsSignatureData1a.getMessageHash()[0] = "0";
//        blsSignatureData1a.getMessageHash()[1] = "1";
//        BLSSignatureData blsSignatureData2a = new BLSSignatureData();
//        blsSignatureData2a.getSignature()[0] = BLSSignature.sign("0".getBytes(StandardCharsets.UTF_8), sk2);
//        blsSignatureData2a.getSignature()[1] = BLSSignature.sign("1".getBytes(StandardCharsets.UTF_8), sk2);
//        blsSignatureData2a.getMessageHash()[0] = "0";
//        blsSignatureData2a.getMessageHash()[1] = "1";
//        BLSSignatureData blsSignatureData3a = new BLSSignatureData();
//        blsSignatureData3a.getSignature()[0] = BLSSignature.sign("0".getBytes(StandardCharsets.UTF_8), sk3);
//        blsSignatureData3a.getSignature()[1] = BLSSignature.sign("1".getBytes(StandardCharsets.UTF_8), sk3);
//        blsSignatureData3a.getMessageHash()[0] = "0";
//        blsSignatureData3a.getMessageHash()[1] = "1";
//        BLSSignatureData blsSignatureData4a = new BLSSignatureData();
//        blsSignatureData4a.getSignature()[0] = BLSSignature.sign("0".getBytes(StandardCharsets.UTF_8), sk4);
//        blsSignatureData4a.getSignature()[1] = BLSSignature.sign("1".getBytes(StandardCharsets.UTF_8), sk4);
//        blsSignatureData4a.getMessageHash()[0] = "0";
//        blsSignatureData4a.getMessageHash()[1] = "1";
//        hashMap1.put(vk1, blsSignatureData1a);
//        hashMap1.put(vk2, blsSignatureData2a);
//        hashMap1.put(vk3, blsSignatureData3a);
//        hashMap1.put(vk4, blsSignatureData4a);
//
//        TransactionBlock blocka = new TransactionBlock();
//        blocka.setHash("1");
//        blocka.setHeight(1);
//        blocka.getSignatureData().put(vk1, blsSignatureData1a);
//        blocka.getSignatureData().put(vk2, blsSignatureData2a);
//        blocka.getSignatureData().put(vk3, blsSignatureData3a);
//        blocka.getSignatureData().put(vk4, blsSignatureData4a);
//
//        assertEquals(block, blocka);
//        BlockSizeCalculator blockSizeCalculatora = new BlockSizeCalculator();
//        blockSizeCalculatora.setTransactionBlock(blocka);
//        byte[] dataa = serenc.encode(blocka, blockSizeCalculatora.TransactionBlockSizeCalculator());
//        TransactionBlock cloned = (TransactionBlock) serenc.decode(dataa);
//        assertEquals(block, cloned);
//        assertEquals(blocka, cloned);
//        String hash2 = HashUtil.sha256_bytetoString(dataa);
//        assertEquals(hash1, hash2);
//        blockSizeCalculatora.setTransactionBlock(cloned);
//        byte[] dataa2 = serenc.encode(cloned, blockSizeCalculatora.TransactionBlockSizeCalculator());
//        String hash3 = HashUtil.sha256_bytetoString(dataa2);
//        assertEquals(hash2, hash3);
//    }
}
