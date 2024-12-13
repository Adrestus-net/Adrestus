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
import io.Adrestus.core.BlockSync;
import io.Adrestus.core.CommitteeBlock;
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
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import io.Adrestus.mapper.MemoryTreePoolSerializer;
import io.Adrestus.network.CachedEventLoop;
import io.Adrestus.network.IPFinder;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.exception.UnsupportedBoundingException;
import io.Adrestus.p2p.kademlia.node.KeyHashGenerator;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.p2p.kademlia.util.BoundedHashUtil;
import io.Adrestus.protocol.mapper.CustomFurySerializer;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import io.distributedLedger.*;
import org.apache.commons.codec.binary.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SyncConsensusNotExistedTest {

    private static ECDSASign ecdsaSign = new ECDSASign();
    private static BLSPrivateKey sk7;
    private static BLSPublicKey vk7;
    private static KademliaData kad7;
    private static KeyHashGenerator<BigInteger, String> keyHashGenerator;

    private static ECKeyPair ecKeyPair1, ecKeyPair2, ecKeyPair3, ecKeyPair4, ecKeyPair5, ecKeyPair6, ecKeyPair7, ecKeyPair8, ecKeyPair9, ecKeyPair10, ecKeyPair11, ecKeyPair12;
    private static String address1, address2, address3, address4, address5, address6, address7, address8, address9, address10, address11, address12;
    private static char[] passphrase;
    private static byte[] key1, key2, key3, key4, key5, key6, key7, key8, key9, key10, key11, key12;

    @BeforeAll
    public static void setup() throws Exception {
        if (System.out.getClass().getName().contains("maven")) {
            return;
        }
        delete_test();
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
        int version = 0x00;

        Type fluentType = new TypeToken<MemoryTreePool>() {
        }.getType();
        List<SerializationUtil.Mapping> list2 = new ArrayList<>();
        list2.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        SerializationUtil patricia_tree_wrapper = new SerializationUtil<>(fluentType, list2);


        char[] mnemonic1 = "sample sail jungle learn general promote task puppy own conduct green affair ".toCharArray();
        char[] mnemonic2 = "photo monitor cushion indicate civil witness orchard estate online favorite sustain extend".toCharArray();
        char[] mnemonic3 = "initial car bulb nature animal honey learn awful grit arrow phrase entire ".toCharArray();
        char[] mnemonic4 = "enrich pulse twin version inject horror village aunt brief magnet blush else ".toCharArray();
        char[] mnemonic5 = "struggle travel ketchup tomato satoshi caught fog process grace pupil item ahead ".toCharArray();
        char[] mnemonic6 = "abstract raise duty scare year add fluid danger include smart senior ensure".toCharArray();
        // MAKE SURE YOU CHANGE THIS MANUALLY WHEN YOUR RUN THIS TESTS ALSO CHANGE BLS KEYS TO WORK PROPERLY
        //char[] mnemonic7 = "fluid abstract raise duty scare year add danger include smart senior ensure".toCharArray();
        char[] mnemonic7 = "danger fluid abstract raise duty scare year add include smart senior ensure".toCharArray();
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
        ecKeyPair1 = Keys.create256r1KeyPair(random);
        random.setSeed(key2);
        ecKeyPair2 = Keys.create256r1KeyPair(random);
        random.setSeed(key3);
        ecKeyPair3 = Keys.create256r1KeyPair(random);
        random.setSeed(key4);
        ecKeyPair4 = Keys.create256r1KeyPair(random);
        random.setSeed(key5);
        ecKeyPair5 = Keys.create256r1KeyPair(random);
        random.setSeed(key6);
        ecKeyPair6 = Keys.create256r1KeyPair(random);
        random2.setSeed(key7);
        ecKeyPair7 = Keys.create256r1KeyPair(random2);
        random3.setSeed(key8);
        ecKeyPair8 = Keys.create256r1KeyPair(random3);
        random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(key9);
        ecKeyPair9 = Keys.create256r1KeyPair(random);
        random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(key10);
        ecKeyPair10 = Keys.create256r1KeyPair(random);
        random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(key11);
        ecKeyPair11 = Keys.create256r1KeyPair(random);
        random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(key12);
        ecKeyPair12 = Keys.create256r1KeyPair(random);


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

        ECDSASignatureData signatureData1 = ecdsaSign.signSecp256r1Message(HashUtil.sha256(StringUtils.getBytesUtf8(address1)), ecKeyPair1);
        ECDSASignatureData signatureData2 = ecdsaSign.signSecp256r1Message(HashUtil.sha256(StringUtils.getBytesUtf8(address2)), ecKeyPair2);
        ECDSASignatureData signatureData3 = ecdsaSign.signSecp256r1Message(HashUtil.sha256(StringUtils.getBytesUtf8(address3)), ecKeyPair3);
        ECDSASignatureData signatureData4 = ecdsaSign.signSecp256r1Message(HashUtil.sha256(StringUtils.getBytesUtf8(address4)), ecKeyPair4);
        ECDSASignatureData signatureData5 = ecdsaSign.signSecp256r1Message(HashUtil.sha256(StringUtils.getBytesUtf8(address5)), ecKeyPair5);
        ECDSASignatureData signatureData6 = ecdsaSign.signSecp256r1Message(HashUtil.sha256(StringUtils.getBytesUtf8(address6)), ecKeyPair6);
        ECDSASignatureData signatureData7 = ecdsaSign.signSecp256r1Message(HashUtil.sha256(StringUtils.getBytesUtf8(address7)), ecKeyPair7);

        TreeFactory.getMemoryTree(0).store(address1, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(0).store(address2, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(0).store(address3, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(0).store(address4, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(0).store(address5, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(0).store(address6, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(0).store(address7, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(0).store(address8, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(0).store(address9, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(0).store(address10, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(0).store(address11, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(0).store(address12, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(0).store("ADR-GBIV-HG2J-27P5-BNVN-MLN6-DL5V-M3YZ-PKEJ-CFFG-FK4L", new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(0).store("ADR-GBZX-XXCW-LWJC-J7RZ-Q6BJ-RFBA-J5WU-NBAG-4RL7-7G6Z", new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(0).store("ADR-GD3G-DK4I-DKM2-IQSB-KBWL-HWRV-BBQA-MUAS-MGXA-5QPP", new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(0).store("ADR-GC2I-WBAW-IKJE-BWFC-ML6T-BNOC-7XOU-IQ74-BJ5L-WP7G", new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));

        TreeFactory.getMemoryTree(1).store(address1, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(1).store(address2, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(1).store(address3, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(1).store(address4, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(1).store(address5, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(1).store(address6, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(1).store(address7, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(1).store(address8, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(1).store(address9, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(1).store(address10, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(1).store(address11, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(1).store(address12, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(1).store("ADR-GBIV-HG2J-27P5-BNVN-MLN6-DL5V-M3YZ-PKEJ-CFFG-FK4L", new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(1).store("ADR-GBZX-XXCW-LWJC-J7RZ-Q6BJ-RFBA-J5WU-NBAG-4RL7-7G6Z", new PatriciaTreeNode(BigDecimal.valueOf(2000), 0));
        TreeFactory.getMemoryTree(1).store("ADR-GD3G-DK4I-DKM2-IQSB-KBWL-HWRV-BBQA-MUAS-MGXA-5QPP", new PatriciaTreeNode(BigDecimal.valueOf(2000), 0));
        TreeFactory.getMemoryTree(1).store("ADR-GC2I-WBAW-IKJE-BWFC-ML6T-BNOC-7XOU-IQ74-BJ5L-WP7G", new PatriciaTreeNode(BigDecimal.valueOf(2000), 0));


        TreeFactory.getMemoryTree(2).store(address1, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(2).store(address2, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(2).store(address3, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(2).store(address4, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(2).store(address5, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(2).store(address6, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(2).store(address7, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(2).store(address8, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(2).store(address9, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(2).store(address10, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(2).store(address11, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(2).store(address12, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(2).store("ADR-GBIV-HG2J-27P5-BNVN-MLN6-DL5V-M3YZ-PKEJ-CFFG-FK4L", new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(2).store("ADR-GBZX-XXCW-LWJC-J7RZ-Q6BJ-RFBA-J5WU-NBAG-4RL7-7G6Z", new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(2).store("ADR-GD3G-DK4I-DKM2-IQSB-KBWL-HWRV-BBQA-MUAS-MGXA-5QPP", new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(2).store("ADR-GC2I-WBAW-IKJE-BWFC-ML6T-BNOC-7XOU-IQ74-BJ5L-WP7G", new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));


        TreeFactory.getMemoryTree(3).store(address1, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(3).store(address2, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(3).store(address3, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(3).store(address4, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(3).store(address5, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(3).store(address6, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(3).store(address7, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(3).store(address8, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(3).store(address9, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(3).store(address10, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(3).store(address11, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(3).store(address12, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(3).store("ADR-GBIV-HG2J-27P5-BNVN-MLN6-DL5V-M3YZ-PKEJ-CFFG-FK4L", new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(3).store("ADR-GBZX-XXCW-LWJC-J7RZ-Q6BJ-RFBA-J5WU-NBAG-4RL7-7G6Z", new PatriciaTreeNode(BigDecimal.valueOf(4000), 0));
        TreeFactory.getMemoryTree(3).store("ADR-GD3G-DK4I-DKM2-IQSB-KBWL-HWRV-BBQA-MUAS-MGXA-5QPP", new PatriciaTreeNode(BigDecimal.valueOf(4000), 0));
        TreeFactory.getMemoryTree(3).store("ADR-GC2I-WBAW-IKJE-BWFC-ML6T-BNOC-7XOU-IQ74-BJ5L-WP7G", new PatriciaTreeNode(BigDecimal.valueOf(4000), 0));

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


        patricia_tree0.save(String.valueOf(0), CustomFurySerializer.getInstance().getFury().serialize(TreeFactory.getMemoryTree(0)));
        patricia_tree1.save(String.valueOf(1), CustomFurySerializer.getInstance().getFury().serialize(TreeFactory.getMemoryTree(1)));
        patricia_tree2.save(String.valueOf(2), CustomFurySerializer.getInstance().getFury().serialize(TreeFactory.getMemoryTree(2)));
        patricia_tree3.save(String.valueOf(3), CustomFurySerializer.getInstance().getFury().serialize(TreeFactory.getMemoryTree(3)));


        sk7 = new BLSPrivateKey(random);
        vk7 = new BLSPublicKey(sk7);
        Thread.sleep(2000);
        kad7 = new KademliaData(new SecurityAuditProofs(address7, vk7, ecKeyPair7.getPublicKey(), signatureData7), new NettyConnectionInfo(IPFinder.getLocalIP(), KademliaConfiguration.PORT));

    }

    @Test
    public void test() throws IOException, InterruptedException {
        if (System.out.getClass().getName().contains("maven")) {
            return;
        }
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("google.com", 80));
        String IP = socket.getLocalAddress().getHostAddress();
        int hit = 0;
        if (!IP.substring(0, 3).equals("192")) {
            return;
        }


        CachedBLSKeyPair.getInstance().setPrivateKey(sk7);
        CachedBLSKeyPair.getInstance().setPublicKey(vk7);


        IAdrestusFactory factory = new AdrestusFactory();
        List<AdrestusTask> tasks = new java.util.ArrayList<>(List.of(
                factory.createBindServerKademliaTask(ecKeyPair7, vk7),
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
        ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
        tasks.stream().map(Worker::new).forEach(executor::execute);
        CachedEventLoop.getInstance().start();

        var blocksync = new BlockSync();
        blocksync.WaitPatientlyYourPosition();

        CountDownLatch latch = new CountDownLatch(50);
        ConsensusState c = new ConsensusState(latch);
        c.getTransaction_block_timer().scheduleAtFixedRate(new ConsensusState.TransactionBlockConsensusTask(), ConsensusConfiguration.CONSENSUS_TIMER, ConsensusConfiguration.CONSENSUS_TIMER);
        //c.getCommittee_block_timer().scheduleAtFixedRate(new ConsensusState.CommitteeBlockConsensusTask(), ConsensusConfiguration.CONSENSUS_COMMITTEE_TIMER, ConsensusConfiguration.CONSENSUS_COMMITTEE_TIMER);
        latch.await();
    }

    public static void delete_test() {
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
    }
}
