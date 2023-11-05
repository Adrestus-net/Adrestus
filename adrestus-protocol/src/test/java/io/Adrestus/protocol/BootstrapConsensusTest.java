package io.Adrestus.protocol;

import com.google.common.reflect.TypeToken;
import io.Adrestus.MemoryTreePool;
import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.config.ConsensusConfiguration;
import io.Adrestus.config.KademliaConfiguration;
import io.Adrestus.config.NodeSettings;
import io.Adrestus.consensus.ConsensusState;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.Receipt;
import io.Adrestus.core.Resourses.*;
import io.Adrestus.core.Transaction;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.SecurityAuditProofs;
import io.Adrestus.crypto.WalletAddress;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.crypto.elliptic.ECDSASignatureData;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import io.Adrestus.crypto.elliptic.mapper.StakingData;
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import io.Adrestus.crypto.vdf.engine.VdfEngine;
import io.Adrestus.crypto.vdf.engine.VdfEnginePietrzak;
import io.Adrestus.mapper.MemoryTreePoolSerializer;
import io.Adrestus.network.CachedEventLoop;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.exception.UnsupportedBoundingException;
import io.Adrestus.p2p.kademlia.node.KeyHashGenerator;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.p2p.kademlia.util.BoundedHashUtil;
import io.Adrestus.p2p.kademlia.util.LoggerKademlia;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import io.distributedLedger.*;
import org.apache.commons.codec.binary.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BootstrapConsensusTest {

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

    private static ECKeyPair ecKeyPair1, ecKeyPair2, ecKeyPair3, ecKeyPair4, ecKeyPair5, ecKeyPair6, ecKeyPair7, ecKeyPair8, ecKeyPair9, ecKeyPair10, ecKeyPair11, ecKeyPair12;
    private static String address1, address2, address3, address4, address5, address6, address7, address8, address9, address10, address11, address12;
    private static ECDSASign ecdsaSign = new ECDSASign();
    private static VdfEngine vdf;
    private static KademliaData kad1, kad2, kad3, kad4, kad5, kad6;
    private static KeyHashGenerator<BigInteger, String> keyHashGenerator;
    private static char[] passphrase;
    private static byte[] key1, key2, key3, key4, key5, key6, key7, key8, key9, key10, key11, key12;

    @BeforeAll
    public static void setup() throws Exception {
        delete_test();
        MemoryRingBuffer.getInstance();
        MemoryTransactionPool.getInstance();
        IDatabase<String, CommitteeBlock> database = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);

        Type fluentType = new TypeToken<MemoryTreePool>() {
        }.getType();
        List<SerializationUtil.Mapping> list2 = new ArrayList<>();
        list2.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        SerializationUtil patricia_tree_wrapper = new SerializationUtil<>(fluentType, list2);

        int version = 0x00;
        LoggerKademlia.setLevelOFF();
        int port = 1080;
        KademliaConfiguration.IDENTIFIER_SIZE = 4;
        ConsensusConfiguration.EPOCH_TRANSITION = 3;
        NodeSettings.getInstance();
        keyHashGenerator = key -> {
            try {
                return new BoundedHashUtil(NodeSettings.getInstance().getIdentifierSize()).hash(key.hashCode(), BigInteger.class);
            } catch (UnsupportedBoundingException e) {
                throw new IllegalArgumentException("Key hash generator not valid");
            }
        };

        vdf = new VdfEnginePietrzak(2048);

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


        char[] mnemonic1 = "sample sail jungle learn general promote task puppy own conduct green affair ".toCharArray();
        char[] mnemonic2 = "photo monitor cushion indicate civil witness orchard estate online favorite sustain extend".toCharArray();
        char[] mnemonic3 = "initial car bulb nature animal honey learn awful grit arrow phrase entire ".toCharArray();
        char[] mnemonic4 = "enrich pulse twin version inject horror village aunt brief magnet blush else ".toCharArray();
        char[] mnemonic5 = "struggle travel ketchup tomato satoshi caught fog process grace pupil item ahead ".toCharArray();
        char[] mnemonic6 = "abstract raise duty scare year add fluid danger include smart senior ensure".toCharArray();
        char[] mnemonic7 = "fluid abstract raise duty scare year add danger include smart senior ensure".toCharArray();
        char[] mnemonic8 = "danger fluid abstract raise duty scare year add include smart senior ensure".toCharArray();
        char[] mnemonic9 = "abstract fluid danger raise duty scare year add include smart senior ensure".toCharArray();
        char[] mnemonic10 = "raise fluid abstract danger duty scare year add include smart senior ensure".toCharArray();
        char[] mnemonic11 = "duty fluid abstract raise danger scare year add include smart senior ensure".toCharArray();
        char[] mnemonic12 = "scare fluid abstract raise duty danger year add include smart senior ensure".toCharArray();
        passphrase = "p4ssphr4se".toCharArray();

        Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
        key1 = mnem.createSeed(mnemonic1, passphrase);
        key2 = mnem.createSeed(mnemonic2, passphrase);
        key3 = mnem.createSeed(mnemonic3, passphrase);
        key4 = mnem.createSeed(mnemonic4, passphrase);
        key5 = mnem.createSeed(mnemonic5, passphrase);
        key6 = mnem.createSeed(mnemonic6, passphrase);
        key7 = mnem.createSeed(mnemonic7, passphrase);
        key8 = mnem.createSeed(mnemonic8, passphrase);
        key9 = mnem.createSeed(mnemonic9, passphrase);
        key10 = mnem.createSeed(mnemonic10, passphrase);
        key11 = mnem.createSeed(mnemonic11, passphrase);
        key12 = mnem.createSeed(mnemonic12, passphrase);

        SecureRandom random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        SecureRandom random2 = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        SecureRandom random3 = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(key1);
        ecKeyPair1 = Keys.createEcKeyPair(random);
        random.setSeed(key2);
        ecKeyPair2 = Keys.createEcKeyPair(random);
        random.setSeed(key3);
        ecKeyPair3 = Keys.createEcKeyPair(random);
        random.setSeed(key4);
        ecKeyPair4 = Keys.createEcKeyPair(random);
        random.setSeed(key5);
        ecKeyPair5 = Keys.createEcKeyPair(random);
        random.setSeed(key6);
        ecKeyPair6 = Keys.createEcKeyPair(random);
        random2.setSeed(key7);
        ecKeyPair7 = Keys.createEcKeyPair(random2);
        random3.setSeed(key8);
        ecKeyPair8 = Keys.createEcKeyPair(random3);
        random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(key9);
        ecKeyPair9 = Keys.createEcKeyPair(random);
        random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(key10);
        ecKeyPair10 = Keys.createEcKeyPair(random);
        random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(key11);
        ecKeyPair11 = Keys.createEcKeyPair(random);
        random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(key12);
        ecKeyPair12 = Keys.createEcKeyPair(random);


        address1 = WalletAddress.generate_address((byte) version, ecKeyPair1.getPublicKey());
        address2 = WalletAddress.generate_address((byte) version, ecKeyPair2.getPublicKey());
        address3 = WalletAddress.generate_address((byte) version, ecKeyPair3.getPublicKey());
        address4 = WalletAddress.generate_address((byte) version, ecKeyPair4.getPublicKey());
        address5 = WalletAddress.generate_address((byte) version, ecKeyPair5.getPublicKey());
        address6 = WalletAddress.generate_address((byte) version, ecKeyPair6.getPublicKey());
        address7 = WalletAddress.generate_address((byte) version, ecKeyPair7.getPublicKey());
        address8 = WalletAddress.generate_address((byte) version, ecKeyPair8.getPublicKey());
        address9 = WalletAddress.generate_address((byte) version, ecKeyPair9.getPublicKey());
        address10 = WalletAddress.generate_address((byte) version, ecKeyPair10.getPublicKey());
        address11 = WalletAddress.generate_address((byte) version, ecKeyPair11.getPublicKey());
        address12 = WalletAddress.generate_address((byte) version, ecKeyPair12.getPublicKey());

        ECDSASignatureData signatureData1 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address1)), ecKeyPair1);
        ECDSASignatureData signatureData2 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address2)), ecKeyPair2);
        ECDSASignatureData signatureData3 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address3)), ecKeyPair3);
        ECDSASignatureData signatureData4 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address4)), ecKeyPair4);
        ECDSASignatureData signatureData5 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address5)), ecKeyPair5);
        ECDSASignatureData signatureData6 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address6)), ecKeyPair6);
        ECDSASignatureData signatureData7 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address7)), ecKeyPair7);

        TreeFactory.getMemoryTree(0).store(address1, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(0).store(address2, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(0).store(address3, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(0).store(address4, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(0).store(address5, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(0).store(address6, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(0).store(address7, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(0).store(address8, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(0).store(address9, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(0).store(address10, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(0).store(address11, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(0).store(address12, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(0).store("ADR-GBIV-HG2J-27P5-BNVN-MLN6-DL5V-M3YZ-PKEJ-CFFG-FK4L", new PatriciaTreeNode(1000, 0));
        TreeFactory.getMemoryTree(0).store("ADR-GBZX-XXCW-LWJC-J7RZ-Q6BJ-RFBA-J5WU-NBAG-4RL7-7G6Z", new PatriciaTreeNode(1000, 0));
        TreeFactory.getMemoryTree(0).store("ADR-GD3G-DK4I-DKM2-IQSB-KBWL-HWRV-BBQA-MUAS-MGXA-5QPP", new PatriciaTreeNode(1000, 0));

        TreeFactory.getMemoryTree(1).store(address1, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(1).store(address2, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(1).store(address3, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(1).store(address4, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(1).store(address5, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(1).store(address6, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(1).store(address7, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(1).store(address8, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(1).store(address9, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(1).store(address10, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(1).store(address11, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(1).store(address12, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(1).store("ADR-GBIV-HG2J-27P5-BNVN-MLN6-DL5V-M3YZ-PKEJ-CFFG-FK4L", new PatriciaTreeNode(1000, 0));
        TreeFactory.getMemoryTree(1).store("ADR-GBZX-XXCW-LWJC-J7RZ-Q6BJ-RFBA-J5WU-NBAG-4RL7-7G6Z", new PatriciaTreeNode(2000, 0));
        TreeFactory.getMemoryTree(1).store("ADR-GD3G-DK4I-DKM2-IQSB-KBWL-HWRV-BBQA-MUAS-MGXA-5QPP", new PatriciaTreeNode(2000, 0));

        TreeFactory.getMemoryTree(2).store(address1, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(2).store(address2, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(2).store(address3, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(2).store(address4, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(2).store(address5, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(2).store(address6, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(2).store(address7, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(2).store(address8, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(2).store(address9, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(2).store(address10, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(2).store(address11, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(2).store(address12, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(2).store("ADR-GBIV-HG2J-27P5-BNVN-MLN6-DL5V-M3YZ-PKEJ-CFFG-FK4L", new PatriciaTreeNode(1000, 0));
        TreeFactory.getMemoryTree(2).store("ADR-GBZX-XXCW-LWJC-J7RZ-Q6BJ-RFBA-J5WU-NBAG-4RL7-7G6Z", new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(2).store("ADR-GD3G-DK4I-DKM2-IQSB-KBWL-HWRV-BBQA-MUAS-MGXA-5QPP", new PatriciaTreeNode(3000, 0));

        TreeFactory.getMemoryTree(3).store(address1, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(3).store(address2, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(3).store(address3, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(3).store(address4, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(3).store(address5, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(3).store(address6, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(3).store(address7, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(3).store(address8, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(3).store(address9, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(3).store(address10, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(3).store(address11, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(3).store(address12, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(3).store("ADR-GBIV-HG2J-27P5-BNVN-MLN6-DL5V-M3YZ-PKEJ-CFFG-FK4L", new PatriciaTreeNode(1000, 0));
        TreeFactory.getMemoryTree(3).store("ADR-GBZX-XXCW-LWJC-J7RZ-Q6BJ-RFBA-J5WU-NBAG-4RL7-7G6Z", new PatriciaTreeNode(4000, 0));
        TreeFactory.getMemoryTree(3).store("ADR-GD3G-DK4I-DKM2-IQSB-KBWL-HWRV-BBQA-MUAS-MGXA-5QPP", new PatriciaTreeNode(4000, 0));

        kad1 = new KademliaData(new SecurityAuditProofs(address1, vk1, ecKeyPair1.getPublicKey(), signatureData1), new NettyConnectionInfo("192.168.1.106", KademliaConfiguration.PORT));
        kad2 = new KademliaData(new SecurityAuditProofs(address2, vk2, ecKeyPair2.getPublicKey(), signatureData2), new NettyConnectionInfo("192.168.1.113", KademliaConfiguration.PORT));
        kad3 = new KademliaData(new SecurityAuditProofs(address3, vk3, ecKeyPair3.getPublicKey(), signatureData3), new NettyConnectionInfo("192.168.1.116", KademliaConfiguration.PORT));
        kad4 = new KademliaData(new SecurityAuditProofs(address4, vk4, ecKeyPair4.getPublicKey(), signatureData4), new NettyConnectionInfo("192.168.1.110", KademliaConfiguration.PORT));
        kad5 = new KademliaData(new SecurityAuditProofs(address5, vk5, ecKeyPair5.getPublicKey(), signatureData5), new NettyConnectionInfo("192.168.1.112", KademliaConfiguration.PORT));
        kad6 = new KademliaData(new SecurityAuditProofs(address6, vk6, ecKeyPair6.getPublicKey(), signatureData6), new NettyConnectionInfo("192.168.1.115", KademliaConfiguration.PORT));
        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.getHeaderData().setTimestamp("2022-11-18 15:01:29.304");
        committeeBlock.getStructureMap().get(0).put(vk1, "192.168.1.106");
        committeeBlock.getStructureMap().get(0).put(vk2, "192.168.1.113");
        committeeBlock.getStructureMap().get(0).put(vk3, "192.168.1.116");
        committeeBlock.getStructureMap().get(1).put(vk4, "192.168.1.110");
        committeeBlock.getStructureMap().get(1).put(vk5, "192.168.1.112");
        committeeBlock.getStructureMap().get(1).put(vk6, "192.168.1.115");


        committeeBlock.getStakingMap().put(new StakingData(1, 10.0), kad1);
        committeeBlock.getStakingMap().put(new StakingData(2, 11.0), kad2);
        committeeBlock.getStakingMap().put(new StakingData(3, 151.0), kad3);
        committeeBlock.getStakingMap().put(new StakingData(4, 16.0), kad4);
        committeeBlock.getStakingMap().put(new StakingData(5, 271.0), kad5);
        committeeBlock.getStakingMap().put(new StakingData(6, 281.0), kad6);

        CachedLatestBlocks.getInstance().setCommitteeBlock(committeeBlock);

        CachedLatestBlocks.getInstance().getCommitteeBlock().setDifficulty(112);
        CachedLatestBlocks.getInstance().getCommitteeBlock().setHash("hash");
        CachedLatestBlocks.getInstance().getCommitteeBlock().setGeneration(0);
        CachedLatestBlocks.getInstance().getCommitteeBlock().setHeight(0);

        TransactionBlock prevblock = new TransactionBlock();
        prevblock.setHeight(1);
        prevblock.setHash("hash");
        prevblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        prevblock.setTransactionProposer(vk1.toRaw());
        prevblock.setLeaderPublicKey(vk1);
        CachedLatestBlocks.getInstance().setTransactionBlock(prevblock);

        database.save(String.valueOf(CachedLatestBlocks.getInstance().getCommitteeBlock().getHeight()), CachedLatestBlocks.getInstance().getCommitteeBlock());

        CachedSecurityHeaders.getInstance().getSecurityHeader().setPRnd(Hex.decode("c1f72aa5bd1e1d53c723b149259b63f759f40d5ab003b547d5c13d45db9a5da8"));
        CachedSecurityHeaders.getInstance().getSecurityHeader().setRnd(vdf.solve(CachedSecurityHeaders.getInstance().getSecurityHeader().getPRnd(), CachedLatestBlocks.getInstance().getCommitteeBlock().getDifficulty()));

        CachedZoneIndex.getInstance().setZoneIndexInternalIP();
        IDatabase<String, TransactionBlock> block_database = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
        block_database.save(String.valueOf(CachedLatestBlocks.getInstance().getTransactionBlock().getHeight()), CachedLatestBlocks.getInstance().getTransactionBlock());

        TransactionBlock TransactionBlockZone2 = new TransactionBlock();
        TransactionBlockZone2.setHeight(1);
        TransactionBlockZone2.setHash("TransactionBlockZone0");
        TransactionBlockZone2.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        Thread.sleep(200);
        TransactionBlock TransactionBlockZone3 = new TransactionBlock();
        TransactionBlockZone3.setHeight(1);
        TransactionBlockZone3.setHash("TransactionBlockZone0");
        TransactionBlockZone3.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());

        IDatabase<String, TransactionBlock> Zone2TransactionDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(2));
        IDatabase<String, TransactionBlock> Zone3TransactionDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(3));
        Zone2TransactionDatabase.save("1", TransactionBlockZone2);
        Zone3TransactionDatabase.save("1", TransactionBlockZone3);

        IDatabase<String, byte[]> patricia_tree0 = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_0);
        IDatabase<String, byte[]> patricia_tree1 = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_1);
        IDatabase<String, byte[]> patricia_tree2 = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_2);
        IDatabase<String, byte[]> patricia_tree3 = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_3);

        TreeFactory.getMemoryTree(0).setHeight("1");
        TreeFactory.getMemoryTree(1).setHeight("1");
        TreeFactory.getMemoryTree(2).setHeight("1");
        TreeFactory.getMemoryTree(3).setHeight("1");
        patricia_tree0.save(TreeFactory.getMemoryTree(0).getHeight(), patricia_tree_wrapper.encode(TreeFactory.getMemoryTree(0)));
        patricia_tree1.save(TreeFactory.getMemoryTree(1).getHeight(), patricia_tree_wrapper.encode(TreeFactory.getMemoryTree(1)));
        patricia_tree2.save(TreeFactory.getMemoryTree(2).getHeight(), patricia_tree_wrapper.encode(TreeFactory.getMemoryTree(2)));
        patricia_tree3.save(TreeFactory.getMemoryTree(3).getHeight(), patricia_tree_wrapper.encode(TreeFactory.getMemoryTree(3)));
    }


    @Test
    public void test() throws IOException, InterruptedException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("google.com", 80));
        String IP = socket.getLocalAddress().getHostAddress();
        int hit = 0;
        if (!IP.substring(0, 3).equals("192")) {
            return;
        }

        IAdrestusFactory factory = new AdrestusFactory();
        List<AdrestusTask> tasks = new java.util.ArrayList<>(List.of(
                //factory.createBindServerKademliaTask(),
                factory.createBindServerCachedTask(),
                factory.createBindServerTransactionTask(),
                factory.createBindServerReceiptTask(),
                factory.createRepositoryTransactionTask(DatabaseInstance.ZONE_0_TRANSACTION_BLOCK),
                factory.createRepositoryTransactionTask(DatabaseInstance.ZONE_1_TRANSACTION_BLOCK),
                factory.createRepositoryTransactionTask(DatabaseInstance.ZONE_2_TRANSACTION_BLOCK),
                factory.createRepositoryTransactionTask(DatabaseInstance.ZONE_3_TRANSACTION_BLOCK),
                factory.createRepositoryPatriciaTreeTask(PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_0),
                factory.createRepositoryPatriciaTreeTask(PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_1),
                factory.createRepositoryPatriciaTreeTask(PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_2),
                factory.createRepositoryPatriciaTreeTask(PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_3),
                factory.createRepositoryCommitteeTask()));


        for (Map.Entry<BLSPublicKey, String> entry : CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).entrySet()) {
            if (IP.equals(entry.getValue())) {
                if (vk1.equals(entry.getKey())) {
                    CachedBLSKeyPair.getInstance().setPrivateKey(sk1);
                    CachedBLSKeyPair.getInstance().setPublicKey(vk1);
                    tasks.add(factory.createBindServerKademliaTask(ecKeyPair1, vk1));
                } else if (vk2.equals(entry.getKey())) {
                    CachedBLSKeyPair.getInstance().setPrivateKey(sk2);
                    CachedBLSKeyPair.getInstance().setPublicKey(vk2);
                    tasks.add(factory.createBindServerKademliaTask(ecKeyPair2, vk2));
                } else if (vk3.equals(entry.getKey())) {
                    CachedBLSKeyPair.getInstance().setPrivateKey(sk3);
                    CachedBLSKeyPair.getInstance().setPublicKey(vk3);
                    tasks.add(factory.createBindServerKademliaTask(ecKeyPair3, vk3));
                } else if (vk4.equals(entry.getKey())) {
                    CachedBLSKeyPair.getInstance().setPrivateKey(sk4);
                    CachedBLSKeyPair.getInstance().setPublicKey(vk4);
                    tasks.add(factory.createBindServerKademliaTask(ecKeyPair4, vk4));
                } else if (vk5.equals(entry.getKey())) {
                    CachedBLSKeyPair.getInstance().setPrivateKey(sk5);
                    CachedBLSKeyPair.getInstance().setPublicKey(vk5);
                    tasks.add(factory.createBindServerKademliaTask(ecKeyPair5, vk5));

                } else if (vk6.equals(entry.getKey())) {
                    CachedBLSKeyPair.getInstance().setPrivateKey(sk6);
                    CachedBLSKeyPair.getInstance().setPublicKey(vk6);
                    tasks.add(factory.createBindServerKademliaTask(ecKeyPair6, vk6));
                }

                hit = 1;
                break;
            }
        }
        for (Map.Entry<BLSPublicKey, String> entry2 : CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).entrySet()) {
            if (IP.equals(entry2.getValue())) {
                if (vk1.equals(entry2.getKey())) {
                    CachedBLSKeyPair.getInstance().setPrivateKey(sk1);
                    CachedBLSKeyPair.getInstance().setPublicKey(vk1);
                    tasks.add(factory.createBindServerKademliaTask(ecKeyPair1, vk1));
                } else if (vk2.equals(entry2.getKey())) {
                    CachedBLSKeyPair.getInstance().setPrivateKey(sk2);
                    CachedBLSKeyPair.getInstance().setPublicKey(vk2);
                    tasks.add(factory.createBindServerKademliaTask(ecKeyPair2, vk2));
                } else if (vk3.equals(entry2.getKey())) {
                    CachedBLSKeyPair.getInstance().setPrivateKey(sk3);
                    CachedBLSKeyPair.getInstance().setPublicKey(vk3);
                    tasks.add(factory.createBindServerKademliaTask(ecKeyPair3, vk3));
                } else if (vk4.equals(entry2.getKey())) {
                    CachedBLSKeyPair.getInstance().setPrivateKey(sk4);
                    CachedBLSKeyPair.getInstance().setPublicKey(vk4);
                    tasks.add(factory.createBindServerKademliaTask(ecKeyPair4, vk4));
                } else if (vk5.equals(entry2.getKey())) {
                    CachedBLSKeyPair.getInstance().setPrivateKey(sk5);
                    CachedBLSKeyPair.getInstance().setPublicKey(vk5);
                    tasks.add(factory.createBindServerKademliaTask(ecKeyPair5, vk5));

                } else if (vk6.equals(entry2.getKey())) {
                    CachedBLSKeyPair.getInstance().setPrivateKey(sk6);
                    CachedBLSKeyPair.getInstance().setPublicKey(vk6);
                    tasks.add(factory.createBindServerKademliaTask(ecKeyPair6, vk6));
                }

                hit = 1;
                break;
            }
        }
        if (hit == 0)
            return;


        ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
        tasks.stream().map(Worker::new).forEach(executor::execute);

        CachedEventLoop.getInstance().start();

        if (CachedBLSKeyPair.getInstance().getPublicKey().equals(vk1)) {
            System.out.println("Start and wait");
            Thread.sleep(17000);
        }


        CountDownLatch latch = new CountDownLatch(50);
        ConsensusState c = new ConsensusState(latch);
        c.getTransaction_block_timer().scheduleAtFixedRate(new ConsensusState.TransactionBlockConsensusTask(), ConsensusConfiguration.CONSENSUS_TIMER, ConsensusConfiguration.CONSENSUS_TIMER);
        //c.getCommittee_block_timer().scheduleAtFixedRate(new ConsensusState.CommitteeBlockConsensusTask(), ConsensusConfiguration.CONSENSUS_COMMITTEE_TIMER, ConsensusConfiguration.CONSENSUS_COMMITTEE_TIMER);
        latch.await();
    }


    public static void delete_test() {
        IDatabase<String, LevelDBTransactionWrapper<Transaction>> transaction_database = new DatabaseFactory(String.class, Transaction.class, new TypeToken<LevelDBTransactionWrapper<Transaction>>() {
        }.getType()).getDatabase(DatabaseType.LEVEL_DB);
        IDatabase<String, LevelDBTransactionWrapper<Receipt>> receiptdatabase = new DatabaseFactory(String.class, Receipt.class, new TypeToken<LevelDBTransactionWrapper<Receipt>>() {
        }.getType()).getDatabase(DatabaseType.LEVEL_DB);
        IDatabase<String, TransactionBlock> transaction_block1 = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.ZONE_0_TRANSACTION_BLOCK);
        IDatabase<String, TransactionBlock> transaction_block2 = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.ZONE_1_TRANSACTION_BLOCK);
        IDatabase<String, TransactionBlock> transaction_block3 = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.ZONE_2_TRANSACTION_BLOCK);
        IDatabase<String, TransactionBlock> transaction_block4 = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.ZONE_3_TRANSACTION_BLOCK);

        IDatabase<String, byte[]> patricia_tree0 = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_0);
        IDatabase<String, byte[]> patricia_tree1 = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_1);
        IDatabase<String, byte[]> patricia_tree2 = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_2);
        IDatabase<String, byte[]> patricia_tree3 = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_3);

        IDatabase<String, CommitteeBlock> commit = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);


        //ITS IMPORTANT FIRST DELETE PATRICIA TREE AND AFTER TRASNACTION BLOCKS
        patricia_tree0.delete_db();
        patricia_tree1.delete_db();
        patricia_tree2.delete_db();
        patricia_tree3.delete_db();

        transaction_block1.delete_db();
        transaction_block2.delete_db();
        transaction_block3.delete_db();
        transaction_block4.delete_db();


        commit.delete_db();
        transaction_database.delete_db();
        receiptdatabase.delete_db();
    }
}
