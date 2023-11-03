package io.Adrestus.protocol;

import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.Resourses.MemoryTransactionPool;
import io.Adrestus.crypto.WalletAddress;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import io.Adrestus.network.CachedEventLoop;
import io.distributedLedger.DatabaseInstance;
import io.distributedLedger.PatriciaTreeInstance;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkerTest {


    @BeforeAll
    public static void setup() throws Exception {
        int version = 0x00;
        int size = 105;
        for (int i = 0; i < size; i++) {
            Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
            char[] mnemonic_sequence = "sample sail jungle learn general promote task puppy own conduct green affair ".toCharArray();
            char[] passphrase = ("p4ssphr4se" + String.valueOf(i)).toCharArray();
            byte[] key = mnem.createSeed(mnemonic_sequence, passphrase);
            SecureRandom random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
            random.setSeed(key);
            ECKeyPair ecKeyPair = Keys.createEcKeyPair(random);
            String adddress = WalletAddress.generate_address((byte) version, ecKeyPair.getPublicKey());
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(adddress, new PatriciaTreeNode(1000, 0));
        }
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store("ADR-GBIV-HG2J-27P5-BNVN-MLN6-DL5V-M3YZ-PKEJ-CFFG-FK4L", new PatriciaTreeNode(1000, 0));
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store("ADR-GB5Y-BF5F-JUS3-5HHG-WWQR-MCM3-LIP6-EMWY-UVAK-PXYV", new PatriciaTreeNode(1000, 0));
    }

    //IT'S IMPORTANT to have cachedEventLoop started to play
    @Test
    public void test() throws InterruptedException {
        CachedEventLoop.getInstance().start();
        IAdrestusFactory factory = new AdrestusFactory();
        List<AdrestusTask> tasks = new java.util.ArrayList<>(List.of(
                //factory.createBindServerKademliaTask(),
                factory.createBindServerTransactionTask(),
                factory.createBindServerReceiptTask(),
                factory.createSendReceiptTask(),
                factory.createRepositoryTransactionTask(DatabaseInstance.ZONE_0_TRANSACTION_BLOCK),
                factory.createRepositoryTransactionTask(DatabaseInstance.ZONE_1_TRANSACTION_BLOCK),
                factory.createRepositoryTransactionTask(DatabaseInstance.ZONE_2_TRANSACTION_BLOCK),
                factory.createRepositoryTransactionTask(DatabaseInstance.ZONE_3_TRANSACTION_BLOCK),
                factory.createRepositoryCommitteeTask()));
        ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
        tasks.stream().map(Worker::new).forEach(executor::execute);
        // All tasks were executed, now shutdown
        Thread.sleep(2000);
        tasks.forEach(val -> {
            try {
                if (val != null)
                    val.close();
            } catch (NullPointerException e) {
            }
        });
        tasks.clear();
        executor.shutdown();
        executor.shutdownNow();
        while (!executor.isTerminated()) {
            Thread.yield();
        }
    }

    @Test
    public void test2() throws Exception {
        Thread.sleep(2000);
        IAdrestusFactory factory = new AdrestusFactory();
        List<AdrestusTask> tasks = new java.util.ArrayList<>(List.of(
                //factory.createBindServerKademliaTask(),
                factory.createBindServerTransactionTask(),
                factory.createBindServerReceiptTask(),
                factory.createSendReceiptTask(),
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

        Thread.sleep(5000);
        /*ArrayList<InetSocketAddress> list = new ArrayList<>();
        InetSocketAddress address1 = new InetSocketAddress(InetAddress.getByName(IPFinder.getLocalIP()), NetworkConfiguration.RPC_PORT);
        list.add(address1);

        RpcAdrestusClient client = new RpcAdrestusClient(new CommitteeBlock(), list, CachedEventLoop.getInstance().getEventloop());
        client.connect();
        RpcAdrestusClient client2 = new RpcAdrestusClient(new TransactionBlock(), list, CachedEventLoop.getInstance().getEventloop());
        client2.connect();*/
        int size = 0;
        int count = 0;
        while (count < 20) {
            Thread.sleep(2000);
            System.out.println(MemoryTransactionPool.getInstance().getSize());
            count++;
        }
        Thread.sleep(2000);
        tasks.forEach(val -> {
            try {
                if (val != null)
                    val.close();
            } catch (NullPointerException e) {
            }
        });
        tasks.clear();
        executor.shutdown();
        executor.shutdownNow();
        while (!executor.isTerminated()) {
            Thread.yield();
        }

    }
}
