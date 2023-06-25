package io.Adrestus.protocol;

import com.google.common.reflect.TypeToken;
import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.config.ConsensusConfiguration;
import io.Adrestus.config.TestingConfiguration;
import io.Adrestus.consensus.ConsensusState;
import io.Adrestus.core.*;
import io.Adrestus.core.Resourses.*;
import io.Adrestus.crypto.WalletAddress;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import io.Adrestus.network.CachedEventLoop;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import io.distributedLedger.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConsensusTransactionTimer4Test {

    private static int version = 0x00;
    private static SerializationUtil<Transaction> serenc;
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
    private static IBlockIndex blockIndex;




    //YOU NEED TO RUN COLLECTION STRATEGY FROM API
    @BeforeAll
    public static void construct() throws Exception {
        MemoryRingBuffer.getInstance();
        MemoryTransactionPool.getInstance();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        serenc = new SerializationUtil<Transaction>(Transaction.class, list);
        IDatabase<String, TransactionBlock> block_database = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
        IDatabase<String, byte[]> tree_datasbase = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getPatriciaTreeZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
        IDatabase<String, LevelDBTransactionWrapper<Transaction>> transaction_database = new DatabaseFactory(String.class, Transaction.class, new TypeToken<LevelDBTransactionWrapper<Transaction>>() {
        }.getType()).getDatabase(DatabaseType.LEVEL_DB);

        tree_datasbase.delete_db();
        transaction_database.delete_db();
        block_database.delete_db();

        CachedZoneIndex.getInstance().setZoneIndex(1);
        int start = 0;
        int end = 2000;
        for (int i = start; i < end; i++) {
            Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
            char[] mnemonic_sequence = "sample sail jungle learn general promote task puppy own conduct green affair ".toCharArray();
            char[] passphrase = ("p4ssphr4se" + String.valueOf(i)).toCharArray();
            byte[] key = mnem.createSeed(mnemonic_sequence, passphrase);
            SecureRandom randoms = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
            randoms.setSeed(key);
            ECKeyPair ecKeyPair = Keys.createEcKeyPair(randoms);
            String adddress = WalletAddress.generate_address((byte) version, ecKeyPair.getPublicKey());
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(adddress, new PatriciaTreeNode(1000, 0));
        }

        blockIndex = new BlockIndex();
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

        TransactionBlock prevblock = new TransactionBlock();
        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.setGeneration(1);
        committeeBlock.setViewID(1);
        prevblock.setHeight(1);
        prevblock.setHash("hash");
        prevblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        CachedLatestBlocks.getInstance().setCommitteeBlock(committeeBlock);

        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).put(vk1, "192.168.1.106");
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).put(vk3, "192.168.1.116");
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).put(vk4, "192.168.1.110");
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).put(vk5, "192.168.1.112");
        //CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).put(vk4, "192.168.1.110");
        //CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).put(vk5, "192.168.1.112");
        //CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).put(vk6, "192.168.1.115");


        try {
            prevblock.setTransactionProposer(CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).keySet().stream().findFirst().get().toRaw());
            prevblock.setLeaderPublicKey(CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).keySet().stream().findFirst().get());

            CachedLatestBlocks.getInstance().setTransactionBlock(prevblock);
            CachedEpochGeneration.getInstance().setEpoch_counter(0);
            CachedLeaderIndex.getInstance().setTransactionPositionLeader(0);
        } catch (NoSuchElementException e) {
            System.out.println(e.toString());
        }
    }

    @Test
    public void consensus_timer_test() throws Exception {

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
                factory.createRepositoryTransactionTask(DatabaseInstance.ZONE_0_TRANSACTION_BLOCK),
                factory.createRepositoryTransactionTask(DatabaseInstance.ZONE_1_TRANSACTION_BLOCK),
                factory.createRepositoryTransactionTask(DatabaseInstance.ZONE_2_TRANSACTION_BLOCK),
                factory.createRepositoryTransactionTask(DatabaseInstance.ZONE_3_TRANSACTION_BLOCK),
                factory.createRepositoryPatriciaTreeTask(PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_0),
                factory.createRepositoryPatriciaTreeTask(PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_1),
                factory.createRepositoryPatriciaTreeTask(PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_2),
                factory.createRepositoryPatriciaTreeTask(PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_3)));
        int activezones = blockIndex.getZone(IP);
        for (Map.Entry<BLSPublicKey, String> entry : CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(activezones).entrySet()) {
            if (IP.equals(entry.getValue())) {
                if (vk1.equals(entry.getKey())) {
                    CachedBLSKeyPair.getInstance().setPrivateKey(sk1);
                    CachedBLSKeyPair.getInstance().setPublicKey(vk1);
                } else if (vk2.equals(entry.getKey())) {
                    CachedBLSKeyPair.getInstance().setPrivateKey(sk2);
                    CachedBLSKeyPair.getInstance().setPublicKey(vk2);
                } else if (vk3.equals(entry.getKey())) {
                    CachedBLSKeyPair.getInstance().setPrivateKey(sk3);
                    CachedBLSKeyPair.getInstance().setPublicKey(vk3);
                } else if (vk4.equals(entry.getKey())) {
                    CachedBLSKeyPair.getInstance().setPrivateKey(sk4);
                    CachedBLSKeyPair.getInstance().setPublicKey(vk4);
                } else if (vk5.equals(entry.getKey())) {
                    CachedBLSKeyPair.getInstance().setPrivateKey(sk5);
                    CachedBLSKeyPair.getInstance().setPublicKey(vk5);
                } else if (vk6.equals(entry.getKey())) {
                    CachedBLSKeyPair.getInstance().setPrivateKey(sk6);
                    CachedBLSKeyPair.getInstance().setPublicKey(vk6);
                }
                hit = 1;
                break;
            }
        }
        if (hit == 0) {
            return;
        }

        ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
        tasks.stream().map(Worker::new).forEach(executor::execute);

        CachedEventLoop.getInstance().start();


        CountDownLatch latch = new CountDownLatch(15);
        ConsensusState c = new ConsensusState(latch, true);
        c.getTransaction_block_timer().scheduleAtFixedRate(new ConsensusState.TransactionBlockConsensusTask(), ConsensusConfiguration.CONSENSUS_TIMER, ConsensusConfiguration.CONSENSUS_TIMER);
        //c.getCommittee_block_timer().scheduleAtFixedRate(new ConsensusState.CommitteeBlockConsensusTask(), ConsensusConfiguration.CONSENSUS_COMMITTEE_TIMER, ConsensusConfiguration.CONSENSUS_COMMITTEE_TIMER);
        latch.await();
        IDatabase<String, TransactionBlock> block_database = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
        Map<String, TransactionBlock> map_returned = block_database.findBetweenRange("1");

        int count = 0;
        for (Map.Entry<String, TransactionBlock> entry : map_returned.entrySet()) {
            count += entry.getValue().getTransactionList().size();
        }

        String address = "ADR-ACPY-HRDJ-SSL3-7X2N-OT3Y-7VRW-PFRZ-S46W-M5AF-JO4O";
        Map<String, List<Transaction>> map = CacheTemporalTransactionPool.getInstance().getAsMap();
        List<Transaction> list = MemoryTransactionPool.getInstance().getAll();
        List<Transaction> gf = list.stream().filter(tr -> tr.getFrom().equals(address)).collect(Collectors.toList());
        List<Transaction> tra = map.get(address);
        IDatabase<String, LevelDBTransactionWrapper<Transaction>> transaction_database = new DatabaseFactory(String.class, Transaction.class, new TypeToken<LevelDBTransactionWrapper<Transaction>>() {
        }.getType()).getDatabase(DatabaseType.LEVEL_DB);
        LevelDBTransactionWrapper<Transaction> tosearch;
        try {
            tosearch = transaction_database.findByKey(address).get();
        } catch (NoSuchElementException e) {
            return;
        }
        assertEquals(TestingConfiguration.NONCE * (TestingConfiguration.END - 1), count);

    }
}
