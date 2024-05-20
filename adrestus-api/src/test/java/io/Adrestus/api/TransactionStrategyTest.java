package io.Adrestus.api;


import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.config.KademliaConfiguration;
import io.Adrestus.core.*;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedLeaderIndex;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.Resourses.MemoryTransactionPool;
import io.Adrestus.core.RingBuffer.handler.transactions.SignatureEventHandler;
import io.Adrestus.core.RingBuffer.publisher.TransactionEventPublisher;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.SecurityAuditProofs;
import io.Adrestus.crypto.WalletAddress;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.crypto.elliptic.ECDSASignatureData;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.StakingData;
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import org.apache.commons.codec.binary.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for simple App.
 */
public class TransactionStrategyTest {
    private static BLSPrivateKey sk1;
    private static BLSPublicKey vk1;

    private static BLSPrivateKey sk2;
    private static BLSPublicKey vk2;

    private static ECKeyPair ecKeyPair1, ecKeyPair2;

    private static int NONCE = 1;
    private static int start = 0;
    private static int end = 105;
    private static ArrayList<String> addreses = new ArrayList<>();
    private static ArrayList<ECKeyPair> keypair = new ArrayList<>();

    private static ECDSASign ecdsaSign = new ECDSASign();
    private static SerializationUtil<Transaction> serenc;

    @BeforeAll
    public static void setup() throws Exception {
        List<SerializationUtil.Mapping> lists = new ArrayList<>();
        lists.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        serenc = new SerializationUtil<Transaction>(Transaction.class, lists);
        int version = 0x00;

        sk1 = new BLSPrivateKey(1);
        vk1 = new BLSPublicKey(sk1);

        sk2 = new BLSPrivateKey(2);
        vk2 = new BLSPublicKey(sk2);

        char[] mnemonic1 = "sample sail jungle learn general promote task puppy own conduct green affair ".toCharArray();
        char[] mnemonic2 = "photo monitor cushion indicate civil witness orchard estate online favorite sustain extend".toCharArray();
        char[] passphrases = "p4ssphr4se".toCharArray();

        Mnemonic mnems = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
        byte[] key1 = mnems.createSeed(mnemonic1, passphrases);
        byte[] key2 = mnems.createSeed(mnemonic2, passphrases);
        SecureRandom random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(key1);
        ecKeyPair1 = Keys.createEcKeyPair(random);
        random.setSeed(key2);
        ecKeyPair2 = Keys.createEcKeyPair(random);
        String address1 = WalletAddress.generate_address((byte) version, ecKeyPair1.getPublicKey());
        String address2 = WalletAddress.generate_address((byte) version, ecKeyPair2.getPublicKey());
        ECDSASignatureData signatureData1 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address1)), ecKeyPair1);
        ECDSASignatureData signatureData2 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address2)), ecKeyPair2);

        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));


        CachedZoneIndex.getInstance().setZoneIndex(0);
        ArrayList<String> mesages = new ArrayList<>();
        TransactionCallback transactionCallback = new TransactionCallback() {
            @Override
            public void call(String value) {
                mesages.add(value);
            }
        };
        SignatureEventHandler eventHandler = new SignatureEventHandler(SignatureEventHandler.SignatureBehaviorType.SIMPLE_TRANSACTIONS, new CountDownLatch(end - 1));
        TransactionEventPublisher publisher = new TransactionEventPublisher(4096);
        publisher
                .withAddressSizeEventHandler()
                .withTypeEventHandler()
                .withAmountEventHandler()
                .withTypeEventHandler()
                .withDoubleSpendEventHandler()
                .withHashEventHandler()
                .withDelegateEventHandler()
                .withNonceEventHandler()
                .withReplayEventHandler()
                .withStakingEventHandler()
                .withTransactionFeeEventHandler()
                .withTimestampEventHandler()
                .withSameOriginEventHandler()
                .withZoneEventHandler()
                .withDuplicateEventHandler()
                .mergeEventsAndPassThen(eventHandler);
        publisher.start();

        for (int i = start; i < end; i++) {
            Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
            char[] mnemonic_sequence = "sample sail jungle learn general promote task puppy own conduct green affair ".toCharArray();
            char[] passphrase = ("p4ssphr4se" + String.valueOf(i)).toCharArray();
            byte[] key = mnem.createSeed(mnemonic_sequence, passphrase);
            SecureRandom randoms = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
            randoms.setSeed(key);
            ECKeyPair ecKeyPair = Keys.createEcKeyPair(randoms);
            String adddress = WalletAddress.generate_address((byte) version, ecKeyPair.getPublicKey());
            addreses.add(adddress);
            keypair.add(ecKeyPair);
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(adddress, new PatriciaTreeNode(1000, 0));
        }
        for (int i = start; i < end - 1; i++) {
            Transaction transaction = new RegularTransaction();
            transaction.setFrom(addreses.get(i));
            transaction.setTo(addreses.get(i + 1));
            transaction.setStatus(StatusType.PENDING);
            transaction.setTimestamp(GetTime.GetTimeStampInString());
            transaction.setZoneFrom(0);
            transaction.setZoneTo(0);
            transaction.setAmount(100);
            transaction.setAmountWithTransactionFee(transaction.getAmount() * (10.0 / 100.0));
            transaction.setTransactionCallback(transactionCallback);
            transaction.setNonce(1);

            byte byf[] = serenc.encode(transaction, 1024);
            transaction.setHash(HashUtil.sha256_bytetoString(byf));

            ECDSASignatureData signatureData = ecdsaSign.secp256SignMessage(Hex.decode(transaction.getHash()), keypair.get(i));
            transaction.setSignature(signatureData);

            publisher.publish(transaction);
            //publisher.publish(transaction);
        }
        publisher.getJobSyncUntilRemainingCapacityZero();
        eventHandler.getLatch().await();
        assertTrue(mesages.isEmpty());
        publisher.close();
        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.getHeaderData().setTimestamp("2022-11-18 15:01:29.304");
        committeeBlock.getStructureMap().get(0).put(vk1, "192.168.1.106");
        committeeBlock.getStructureMap().get(0).put(vk2, "192.168.1.113");

        committeeBlock.getStakingMap().put(new StakingData(1, 10.0), new KademliaData(new SecurityAuditProofs(addreses.get(0), vk1, ecKeyPair1.getPublicKey(), signatureData1), new NettyConnectionInfo("192.168.1.106", KademliaConfiguration.PORT)));
        committeeBlock.getStakingMap().put(new StakingData(2, 13.0), new KademliaData(new SecurityAuditProofs(addreses.get(1), vk2, ecKeyPair2.getPublicKey(), signatureData2), new NettyConnectionInfo("192.168.1.113", KademliaConfiguration.PORT)));

        CachedLatestBlocks.getInstance().setCommitteeBlock(committeeBlock);
        CachedLeaderIndex.getInstance().setCommitteePositionLeader(0);
        CachedZoneIndex.getInstance().setZoneIndex(0);
    }


    //I am not sure but this maybe need WorkerTest from protocols tests
    @Test
    public void transaction_list() throws Exception {
        ArrayList<Transaction> list = new ArrayList<>(MemoryTransactionPool.getInstance().getAll());
        list.remove(0);
        Strategy transactionStrategy = new Strategy(new TransactionStrategy(list));
        System.out.println(MemoryTransactionPool.getInstance().getAll().size());
        transactionStrategy.SendTransactionSync();
    }


    //In order to run this test with sucess change line 42 at BindServerTransactionTask
    //Worker Test runs on 106
    @Test
    public void transaction_list2() throws Exception {
        int count = 0;
        for (int j = 1; j <= NONCE; j++) {
            ArrayList<Transaction> list = new ArrayList<>();
            for (int i = start; i < end - 1; i++) {
                Transaction transaction = new RegularTransaction();
                transaction.setFrom(addreses.get(i));
                transaction.setTo(addreses.get(i + 1));
                transaction.setStatus(StatusType.PENDING);
                transaction.setTimestamp(GetTime.GetTimeStampInString());
                transaction.setZoneFrom(0);
                transaction.setZoneTo(0);
                transaction.setAmount(100);
                transaction.setAmountWithTransactionFee(transaction.getAmount() * (10.0 / 100.0));
                transaction.setNonce(1);

                byte byf[] = serenc.encode(transaction, 1024);
                transaction.setHash(HashUtil.sha256_bytetoString(byf));

                count++;
                ECDSASignatureData signatureData = ecdsaSign.secp256SignMessage(Hex.decode(transaction.getHash()), keypair.get(i));
                transaction.setSignature(signatureData);
                list.add(transaction);
                if (i == end - 2) {
                    Strategy transactionStrategy = new Strategy(new TransactionStrategy(list));
                    transactionStrategy.SendTransactionSync();
                }
            }
            Thread.sleep(500);
        }
        System.out.println(count);
    }

    @Test
    public void single_transaction() throws Exception {
        MessageListener messageListener = new MessageListener();
        Strategy transactionStrategy = new Strategy(new TransactionStrategy((Transaction) MemoryTransactionPool.getInstance().getAll().get(0), messageListener));
        // Strategy transactionStrategy1 = new Strategy(new TransactionStrategy((Transaction) MemoryTransactionPool.getInstance().getAll().get(0)));
        System.out.println(MemoryTransactionPool.getInstance().getAll().size());
        transactionStrategy.SendTransactionSync();

    }
}
