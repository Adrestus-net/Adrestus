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
        int size = 5;
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
    }

    @Test
    public void test() throws InterruptedException {
        IAdrestusFactory factory = new AdrestusFactory();
        List<AdrestusTask> tasks = new java.util.ArrayList<>(List.of(
                factory.createTransactionTask(),
                factory.createBindServerReceiptTask(),
                factory.createSendReceiptTask(),
                factory.createRepositoryTransactionTask(),
                factory.createRepositoryCommitteeTask()));
        ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
        tasks.stream().map(Worker::new).forEach(executor::execute);
        // All tasks were executed, now shutdown
        Thread.sleep(2000);
        tasks.forEach(val -> {
            val.close();
        });
        tasks.clear();
        executor.shutdown();
        while (!executor.isTerminated()) {
            Thread.yield();
        }

    }

    @Test
    public void test2() throws Exception {
        Thread.sleep(2000);
        IAdrestusFactory factory = new AdrestusFactory();
        List<AdrestusTask> tasks = new java.util.ArrayList<>(List.of(
                factory.createTransactionTask(),
                factory.createBindServerReceiptTask(),
                factory.createSendReceiptTask(),
                factory.createRepositoryTransactionTask(),
                factory.createRepositoryCommitteeTask()));
        ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
        tasks.stream().map(Worker::new).forEach(executor::execute);
        // All tasks were executed, now shutdown
        int size = 0;
        int count = 0;
        while (count < 100) {
            Thread.sleep(200);
            int left = MemoryTransactionPool.getInstance().getAll().size() - size;
            System.out.println(left);
            size = size + MemoryTransactionPool.getInstance().getAll().size();
            count++;
        }

    }
}
