package io.Adrestus.protocol;

import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.config.ConsensusConfiguration;
import io.Adrestus.config.KademliaConfiguration;
import io.Adrestus.config.NodeSettings;
import io.Adrestus.consensus.ConsensusState;
import io.Adrestus.core.BlockSync;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.Resourses.CachedKademliaNodes;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.SecurityAuditProofs;
import io.Adrestus.crypto.WalletAddress;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.bls.model.Params;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.crypto.elliptic.ECDSASignatureData;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.MnemonicException;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import io.Adrestus.network.CachedEventLoop;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.exception.UnsupportedBoundingException;
import io.Adrestus.p2p.kademlia.node.DHTBootstrapNode;
import io.Adrestus.p2p.kademlia.node.DHTRegularNode;
import io.Adrestus.p2p.kademlia.node.KeyHashGenerator;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.p2p.kademlia.util.BoundedHashUtil;
import io.distributedLedger.*;
import org.apache.commons.codec.binary.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SyncConsensusNotExistedTest {

    private static ECDSASign ecdsaSign = new ECDSASign();
    private static BLSPrivateKey sk2;
    private static BLSPublicKey vk2;
    private static ECKeyPair ecKeyPair2;
    private static KademliaData kad2;
    private static KeyHashGenerator<BigInteger, String> keyHashGenerator;

    private static byte[] key2;
    private static char[] passphrase;

    @BeforeAll
    public static void setup() throws Exception {
        delete_test();
        KademliaConfiguration.IDENTIFIER_SIZE = 3;
        ConsensusConfiguration.EPOCH_TRANSITION=1;
        NodeSettings.getInstance();
        keyHashGenerator = key -> {
            try {
                return new BoundedHashUtil(NodeSettings.getInstance().getIdentifierSize()).hash(key.hashCode(), BigInteger.class);
            } catch (UnsupportedBoundingException e) {
                throw new IllegalArgumentException("Key hash generator not valid");
            }
        };
        int version = 0x00;
        char[] mnemonic7 = "fluid abstract raise duty scare year add danger include smart senior ensure".toCharArray();
        passphrase = "p4ssphr4se".toCharArray();

        Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
        byte[] key1 = mnem.createSeed(mnemonic7, passphrase);
        SecureRandom random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(key1);
        ecKeyPair2 = Keys.createEcKeyPair(random);
        sk2 = new BLSPrivateKey(random);
        vk2 = new BLSPublicKey(sk2, new Params(new String(passphrase).getBytes(StandardCharsets.UTF_8)));
        String address2 = WalletAddress.generate_address((byte) version, ecKeyPair2.getPublicKey());
        ECDSASignatureData signatureData2 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address2)), ecKeyPair2);
        kad2 = new KademliaData(new SecurityAuditProofs(address2, vk2, ecKeyPair2.getPublicKey(), signatureData2), new NettyConnectionInfo("192.168.1.113", KademliaConfiguration.PORT));

        TreeFactory.getMemoryTree(0).store(address2, new PatriciaTreeNode(3000, 0));
    }

    @Test
    public void test() throws IOException, InterruptedException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("google.com", 80));
        String IP = socket.getLocalAddress().getHostAddress();

        if (!IP.substring(0, 9).equals("192.168.1"))
            return;


        CachedBLSKeyPair.getInstance().setPrivateKey(sk2);
        CachedBLSKeyPair.getInstance().setPublicKey(vk2);



        IAdrestusFactory factory = new AdrestusFactory();
        List<AdrestusTask> tasks = new java.util.ArrayList<>(List.of(
                factory.createBindServerKademliaTask(ecKeyPair2,vk2),
                factory.createBindServerCachedTask(),
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
        CachedEventLoop.getInstance().start();

        var blocksync = new BlockSync();
        blocksync.WaitPatientlyYourPosition();

        CountDownLatch latch = new CountDownLatch(20);
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
