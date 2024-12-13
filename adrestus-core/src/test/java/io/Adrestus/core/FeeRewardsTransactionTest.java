package io.Adrestus.core;

import com.google.common.reflect.TypeToken;
import io.Adrestus.MemoryTreePool;
import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.Trie.PatriciaTreeTransactionType;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.config.ConsensusConfiguration;
import io.Adrestus.config.KademliaConfiguration;
import io.Adrestus.config.NodeSettings;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedLeaderIndex;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.RingBuffer.handler.transactions.SignatureEventHandler;
import io.Adrestus.core.RingBuffer.publisher.BlockEventPublisher;
import io.Adrestus.core.RingBuffer.publisher.TransactionEventPublisher;
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
import io.Adrestus.crypto.elliptic.mapper.BigDecimalSerializer;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.StakingData;
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import io.Adrestus.crypto.vdf.engine.VdfEngine;
import io.Adrestus.mapper.MemoryTreePoolSerializer;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.node.KeyHashGenerator;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FeeRewardsTransactionTest {

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

    private static ECKeyPair ecKeyPair1, ecKeyPair2, ecKeyPair3, ecKeyPair4, ecKeyPair5, ecKeyPair6;
    private static String address1, address2, address3, address4, address5, address6;
    private static ECDSASign ecdsaSign = new ECDSASign();
    private static VdfEngine vdf;
    private static KademliaData kad1, kad2, kad3, kad4, kad5, kad6;
    private static KeyHashGenerator<BigInteger, String> keyHashGenerator;
    private static char[] passphrase;
    private static byte[] key1, key2, key3, key4, key5, key6;

    private static int version = 0x00;
    private static int size = 3;
    private static ArrayList<Transaction> arrayList = new ArrayList<>();
    private static SerializationUtil<Transaction> serenc;

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        Type fluentType = new TypeToken<MemoryTreePool>() {
        }.getType();
        List<SerializationUtil.Mapping> list2 = new ArrayList<>();
        list2.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        SerializationUtil patricia_tree_wrapper = new SerializationUtil<>(fluentType, list2);

        int version = 0x00;
        int port = 1080;
        KademliaConfiguration.IDENTIFIER_SIZE = 4;
        ConsensusConfiguration.EPOCH_TRANSITION = 3;
        NodeSettings.getInstance();

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

        CachedBLSKeyPair.getInstance().setPrivateKey(sk3);
        CachedBLSKeyPair.getInstance().setPublicKey(vk3);

        char[] mnemonic1 = "sample sail jungle learn general promote task puppy own conduct green affair ".toCharArray();
        char[] mnemonic2 = "photo monitor cushion indicate civil witness orchard estate online favorite sustain extend".toCharArray();
        char[] mnemonic3 = "initial car bulb nature animal honey learn awful grit arrow phrase entire ".toCharArray();
        char[] mnemonic4 = "enrich pulse twin version inject horror village aunt brief magnet blush else ".toCharArray();
        char[] mnemonic5 = "struggle travel ketchup tomato satoshi caught fog process grace pupil item ahead ".toCharArray();
        char[] mnemonic6 = "abstract raise duty scare year add fluid danger include smart senior ensure".toCharArray();
        passphrase = "p4ssphr4se".toCharArray();

        Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
        key1 = mnem.createSeed(mnemonic1, passphrase);
        key2 = mnem.createSeed(mnemonic2, passphrase);
        key3 = mnem.createSeed(mnemonic3, passphrase);
        key4 = mnem.createSeed(mnemonic4, passphrase);
        key5 = mnem.createSeed(mnemonic5, passphrase);
        key6 = mnem.createSeed(mnemonic6, passphrase);

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


        address1 = WalletAddress.generate_address((byte) version, ecKeyPair1.getPublicKey());
        address2 = WalletAddress.generate_address((byte) version, ecKeyPair2.getPublicKey());
        address3 = WalletAddress.generate_address((byte) version, ecKeyPair3.getPublicKey());
        address4 = WalletAddress.generate_address((byte) version, ecKeyPair4.getPublicKey());
        address5 = WalletAddress.generate_address((byte) version, ecKeyPair5.getPublicKey());
        address6 = WalletAddress.generate_address((byte) version, ecKeyPair6.getPublicKey());

        ECDSASignatureData signatureData1 = ecdsaSign.signSecp256r1Message(HashUtil.sha256(StringUtils.getBytesUtf8(address1)), ecKeyPair1);
        ECDSASignatureData signatureData2 = ecdsaSign.signSecp256r1Message(HashUtil.sha256(StringUtils.getBytesUtf8(address2)), ecKeyPair2);
        ECDSASignatureData signatureData3 = ecdsaSign.signSecp256r1Message(HashUtil.sha256(StringUtils.getBytesUtf8(address3)), ecKeyPair3);
        ECDSASignatureData signatureData4 = ecdsaSign.signSecp256r1Message(HashUtil.sha256(StringUtils.getBytesUtf8(address4)), ecKeyPair4);
        ECDSASignatureData signatureData5 = ecdsaSign.signSecp256r1Message(HashUtil.sha256(StringUtils.getBytesUtf8(address5)), ecKeyPair5);
        ECDSASignatureData signatureData6 = ecdsaSign.signSecp256r1Message(HashUtil.sha256(StringUtils.getBytesUtf8(address6)), ecKeyPair6);

        TreeFactory.getMemoryTree(0).store(address1, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(0).store(address2, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(0).store(address3, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(0).store(address4, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(0).store(address5, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(0).store(address6, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));

        TreeFactory.getMemoryTree(1).store(address1, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(1).store(address2, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(1).store(address3, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(1).store(address4, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(1).store(address5, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(1).store(address6, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));

        TreeFactory.getMemoryTree(2).store(address1, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(2).store(address2, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(2).store(address3, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(2).store(address4, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(2).store(address5, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(2).store(address6, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));

        TreeFactory.getMemoryTree(3).store(address1, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(3).store(address2, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(3).store(address3, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(3).store(address4, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(3).store(address5, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(3).store(address6, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));

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


        committeeBlock.getStakingMap().put(new StakingData(1, BigDecimal.valueOf(10.0)), kad1);
        committeeBlock.getStakingMap().put(new StakingData(2, BigDecimal.valueOf(11.0)), kad2);
        committeeBlock.getStakingMap().put(new StakingData(3, BigDecimal.valueOf(151.0)), kad3);
        committeeBlock.getStakingMap().put(new StakingData(4, BigDecimal.valueOf(16.0)), kad4);
        committeeBlock.getStakingMap().put(new StakingData(5, BigDecimal.valueOf(271.0)), kad5);
        committeeBlock.getStakingMap().put(new StakingData(6, BigDecimal.valueOf(281.0)), kad6);


        CachedLatestBlocks.getInstance().setCommitteeBlock(committeeBlock);

        CachedLatestBlocks.getInstance().getCommitteeBlock().setDifficulty(112);
        CachedLatestBlocks.getInstance().getCommitteeBlock().setHash("hash");
        CachedLatestBlocks.getInstance().getCommitteeBlock().setGeneration(0);
        CachedLatestBlocks.getInstance().getCommitteeBlock().setHeight(0);

        CachedZoneIndex.getInstance().setZoneIndex(0);
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(address3, new PatriciaTreeNode(BigDecimal.ZERO, 0));


        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        serenc = new SerializationUtil<Transaction>(Transaction.class, list);


        ArrayList<String> addreses = new ArrayList<>();
        ArrayList<ECKeyPair> keypair = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Mnemonic mnem1 = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
            char[] mnemonic_sequence = mnem1.create();
            char[] passphrase = "p4ssphr4se".toCharArray();
            byte[] key = mnem1.createSeed(mnemonic_sequence, passphrase);
            ECKeyPair ecKeyPair = Keys.create256r1KeyPair(new SecureRandom(key));
            String adddress = WalletAddress.generate_address((byte) version, ecKeyPair.getPublicKey());
            addreses.add(adddress);
            keypair.add(ecKeyPair);
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(adddress, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        }

        for (int i = 0; i < size - 1; i++) {
            Transaction transaction = new RegularTransaction();
            transaction.setFrom(addreses.get(i));
            transaction.setTo(addreses.get(i + 1));
            transaction.setStatus(StatusType.PENDING);
            transaction.setTimestamp(GetTime.GetTimeStampInString());
            transaction.setZoneFrom(0);
            transaction.setZoneTo(0);
            transaction.setAmount(BigDecimal.valueOf(100));
            transaction.setAmountWithTransactionFee(transaction.getAmount().multiply(BigDecimal.valueOf(10.0 / 100.0)));
            transaction.setNonce(1);
            transaction.setXAxis(keypair.get(i).getXpubAxis());
            transaction.setYAxis(keypair.get(i).getYpubAxis());
            byte byf[] = serenc.encode(transaction, 1204);
            transaction.setHash(HashUtil.sha256_bytetoString(byf));
            ECDSASignatureData signatureData = ecdsaSign.signSecp256r1Message(transaction.getHash().getBytes(StandardCharsets.UTF_8), keypair.get(i));
            transaction.setSignature(signatureData);
            arrayList.add(transaction);
        }
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(address2, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0, BigDecimal.ZERO, BigDecimal.valueOf(100)));
        RewardsTransaction rewardsTransaction = new RewardsTransaction();
        rewardsTransaction.setType(TransactionType.REWARDS);
        rewardsTransaction.setRecipientAddress(address2);
        rewardsTransaction.setTo("");
        rewardsTransaction.setStatus(StatusType.PENDING);
        rewardsTransaction.setTimestamp(GetTime.GetTimeStampInString());
        rewardsTransaction.setZoneFrom(0);
        rewardsTransaction.setZoneTo(0);
        rewardsTransaction.setAmount(BigDecimal.valueOf(100));
        rewardsTransaction.setAmountWithTransactionFee(rewardsTransaction.getAmount().multiply(BigDecimal.valueOf(10.0 / 100.0)));
        rewardsTransaction.setNonce(1);
        rewardsTransaction.setXAxis(ecKeyPair2.getXpubAxis());
        rewardsTransaction.setYAxis(ecKeyPair2.getYpubAxis());
        byte byf[] = serenc.encode(rewardsTransaction, 1204);
        rewardsTransaction.setHash(HashUtil.sha256_bytetoString(byf));
        ECDSASignatureData signatureData = ecdsaSign.signSecp256r1Message(rewardsTransaction.getHash().getBytes(StandardCharsets.UTF_8), ecKeyPair2);
        rewardsTransaction.setSignature(signatureData);
        arrayList.add(rewardsTransaction);
        BigDecimal sum = arrayList.parallelStream().filter(val -> !val.getType().equals(TransactionType.UNCLAIMED_FEE_REWARD)).map(Transaction::getAmountWithTransactionFee).reduce(BigDecimal.ZERO, BigDecimal::add);
        arrayList.add(0, new UnclaimedFeeRewardTransaction(TransactionType.UNCLAIMED_FEE_REWARD, address3, sum));

    }

    @Test
    public void single_test() {
        BlockIndex blockIndex = new BlockIndex();
        CachedLeaderIndex.getInstance().setTransactionPositionLeader(2);
        TransactionBlock transactionBlock = new TransactionBlock();
        transactionBlock.setHash("Hash1");
        transactionBlock.setHeight(1);
        transactionBlock.setTransactionList(arrayList);

        BLSPublicKey key = blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), CachedLeaderIndex.getInstance().getTransactionPositionLeader());
        String address = blockIndex.getAddressByPublicKey(key);
        long count = transactionBlock.getTransactionList().stream().filter(val -> val.getType().equals(TransactionType.UNCLAIMED_FEE_REWARD)).count();
        BigDecimal sum = arrayList.parallelStream().skip(1).filter(val -> val.getType().equals(TransactionType.REGULAR)).map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(1, count);
        assertEquals(200, sum.doubleValue());
        assertEquals(vk3, key);
        assertEquals(address3, address);

    }

    @SneakyThrows
    @Test
    public void block_publish_fee() throws InterruptedException {
        CachedLeaderIndex.getInstance().setTransactionPositionLeader(0);
        BlockEventPublisher publisher = new BlockEventPublisher(1024);

        TransactionBlock transactionBlock = new TransactionBlock();
        transactionBlock.setHash("Hash1");
        transactionBlock.setHeight(1);
        transactionBlock.setTransactionList(arrayList);
//        MemoryTreePool replica = new MemoryTreePool(((MemoryTreePool) TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex())));
//        if (!transactionBlock.getTransactionList().isEmpty()) {
//            UnclaimedFeeRewardTransaction unclaimedFeeRewardTransaction = (UnclaimedFeeRewardTransaction) transactionBlock.getTransactionList().get(0);
//            replica.depositUnclaimedReward(unclaimedFeeRewardTransaction.getRecipientAddress(), unclaimedFeeRewardTransaction.getAmount());
//            for (int i = 1; i < transactionBlock.getTransactionList().size(); i++) {
//                Transaction transaction = transactionBlock.getTransactionList().get(i);
//                if (transaction instanceof RewardsTransaction) {
//                    if ((transaction.getZoneFrom() == CachedZoneIndex.getInstance().getZoneIndex()) && (transaction.getZoneTo() == CachedZoneIndex.getInstance().getZoneIndex()) && CachedZoneIndex.getInstance().getZoneIndex() == 0) {
//                        replica.withdrawUnclaimedReward(((RewardsTransaction) transaction).getRecipientAddress(), transaction.getAmount());
//                        replica.deposit(((RewardsTransaction) transaction).getRecipientAddress(), transaction.getAmount());
//                    } else
//                        throw new IllegalArgumentException("Unclaimed fee reward problem");
//                    continue;
//                }
//                if ((transaction.getZoneFrom() == CachedZoneIndex.getInstance().getZoneIndex()) && (transaction.getZoneTo() == CachedZoneIndex.getInstance().getZoneIndex())) {
//                    replica.withdraw(transaction.getFrom(), transaction.getAmount());
//                    replica.deposit(transaction.getTo(), transaction.getAmount());
//                } else {
//                    replica.withdraw(transaction.getFrom(), transaction.getAmount());
//                }
//            }
//        }
        MemoryTreePool replica = new MemoryTreePool(((MemoryTreePool) TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex())));
        TreePoolConstructBlock.getInstance().visitForgeTreePool(transactionBlock, replica);
        transactionBlock.setPatriciaMerkleRoot(replica.getRootHash());
        publisher.withReplayFeeEventHandler().withPatriciaTreeEventHandler().withSumFeeRewardEventHandler().withLeaderFeeRewardEventHandler().mergeEvents();
        publisher.start();

        publisher.publish(transactionBlock);
        publisher.getJobSyncUntilRemainingCapacityZero();
        publisher.close();
    }

    @Test
    public void reward_transaction() throws InterruptedException {
        CachedZoneIndex.getInstance().setZoneIndex(0);
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(address1, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).deposit(PatriciaTreeTransactionType.UNCLAIMED_FEE_REWARD, address1, BigDecimal.valueOf(100), BigDecimal.valueOf(1));
        TransactionEventPublisher publisher = new TransactionEventPublisher(100);
        SignatureEventHandler signatureEventHandler = new SignatureEventHandler(SignatureEventHandler.SignatureBehaviorType.SIMPLE_TRANSACTIONS);
        CachedBLSKeyPair.getInstance().setPublicKey(vk1);
        publisher
                .withAddressSizeEventHandler()
                .withTypeEventHandler()
                .withAmountEventHandler()
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
                .withSecp256k1EventHandler()
                .withDuplicateEventHandler()
                .withMinimumStakingEventHandler()
                .mergeEventsAndPassThen(signatureEventHandler);
        publisher.start();


        Callback transactionCallback = new TransactionCallback();
        signatureEventHandler.setLatch(new CountDownLatch(1));
        RewardsTransaction rewardsTransaction = new RewardsTransaction();
        rewardsTransaction.setRecipientAddress(address1);
        rewardsTransaction.setTo("");
        rewardsTransaction.setType(TransactionType.REWARDS);
        rewardsTransaction.setStatus(StatusType.PENDING);
        rewardsTransaction.setTimestamp(GetTime.GetTimeStampInString());
        rewardsTransaction.setZoneFrom(0);
        rewardsTransaction.setZoneTo(0);
        rewardsTransaction.setAmount(BigDecimal.valueOf(100));
        rewardsTransaction.setAmountWithTransactionFee(BigDecimal.ZERO);
        rewardsTransaction.setNonce(1);
        rewardsTransaction.setTransactionCallback(transactionCallback);
        rewardsTransaction.setXAxis(ecKeyPair1.getXpubAxis());
        rewardsTransaction.setYAxis(ecKeyPair1.getYpubAxis());
        byte[] byf = serenc.encode(rewardsTransaction, 1024);
        rewardsTransaction.setHash(HashUtil.sha256_bytetoString(byf));
        ECDSASignatureData signatureData = ecdsaSign.signSecp256r1Message(rewardsTransaction.getHash().getBytes(StandardCharsets.UTF_8), ecKeyPair1);
        rewardsTransaction.setSignature(signatureData);
        publisher.publish(rewardsTransaction);
        signatureEventHandler.getLatch().await();
        assertTrue(((TransactionCallback) transactionCallback).getMessages().isEmpty());
        publisher.getJobSyncUntilRemainingCapacityZero();
        publisher.close();
    }
}
