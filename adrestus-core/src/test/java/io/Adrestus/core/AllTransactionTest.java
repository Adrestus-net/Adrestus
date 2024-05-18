package io.Adrestus.core;

import com.google.common.collect.Iterables;
import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.Trie.PatriciaTreeTransactionType;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.Resourses.MemoryTransactionPool;
import io.Adrestus.core.RingBuffer.handler.transactions.SignatureEventHandler;
import io.Adrestus.core.RingBuffer.publisher.TransactionEventPublisher;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.WalletAddress;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.crypto.elliptic.ECDSASignatureData;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import io.distributedLedger.ZoneDatabaseFactory;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AllTransactionTest {

    private static TransactionEventPublisher publisher;
    private static SignatureEventHandler signatureEventHandler;
    private static String address1;
    private static String address2;
    private static ArrayList<Transaction> arrayList = new ArrayList<>();
    private static SerializationUtil<Transaction> serenc;
    private static ECDSASign ecdsaSign = new ECDSASign();
    private static BLSPrivateKey sk1;
    private static BLSPublicKey vk1;
    private static char[] passphrase;
    private static byte[] key1, key2, key3, key4, key5, key6;
    private static ECKeyPair ecKeyPair1, ecKeyPair2, ecKeyPair3, ecKeyPair4, ecKeyPair5, ecKeyPair6;
    private static int version = 0x00;

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {

        sk1 = new BLSPrivateKey(1);
        vk1 = new BLSPublicKey(sk1);

        char[] mnemonic1 = "sample sail jungle learn general promote task puppy own conduct green affair ".toCharArray();
        char[] mnemonic2 = "photo monitor cushion indicate civil witness orchard estate online favorite sustain extend".toCharArray();
        passphrase = "p4ssphr4se".toCharArray();

        Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
        key1 = mnem.createSeed(mnemonic1, passphrase);
        key2 = mnem.createSeed(mnemonic2, passphrase);

        SecureRandom random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(key1);
        ecKeyPair1 = Keys.createEcKeyPair(random);
        random.setSeed(key2);
        ecKeyPair2 = Keys.createEcKeyPair(random);
        address1 = WalletAddress.generate_address((byte) version, ecKeyPair1.getPublicKey());
        address2 = WalletAddress.generate_address((byte) version, ecKeyPair2.getPublicKey());

        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(address1, new PatriciaTreeNode(100,0,100,100));
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(address2, new PatriciaTreeNode(100,0,100,100));
        CachedZoneIndex.getInstance().setZoneIndex(0);
        publisher = new TransactionEventPublisher(100);
        signatureEventHandler = new SignatureEventHandler(SignatureEventHandler.SignatureBehaviorType.SIMPLE_TRANSACTIONS);
        publisher
                .withDelegateEventHandler()
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
                .mergeEventsAndPassThen(signatureEventHandler);
        publisher.start();

        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        serenc = new SerializationUtil<Transaction>(Transaction.class, list);
    }

    @Test
    public void rewards_test() throws InterruptedException {
        MemoryTransactionPool.getInstance().clear();
        ArrayList<String>mesages = new ArrayList<>();
        TransactionCallback transactionCallback = new TransactionCallback() {
            @Override
            public void call(String value) {
                mesages.add(value);
            }
        };
        signatureEventHandler.setLatch(new CountDownLatch(1));
        RewardsTransaction rewardsTransaction = new RewardsTransaction();
        rewardsTransaction.setType(TransactionType.REWARDS);
        rewardsTransaction.setRecipientAddress(address1);
        rewardsTransaction.setTo("");
        rewardsTransaction.setStatus(StatusType.PENDING);
        rewardsTransaction.setTimestamp(GetTime.GetTimeStampInString());
        rewardsTransaction.setZoneFrom(0);
        rewardsTransaction.setZoneTo(0);
        rewardsTransaction.setAmount(100);
        rewardsTransaction.setAmountWithTransactionFee((double) (100 * 10) /100);
        rewardsTransaction.setNonce(1);
        rewardsTransaction.setTransactionCallback(transactionCallback);
        byte byf[] = serenc.encode(rewardsTransaction,1024);
        rewardsTransaction.setHash(HashUtil.sha256_bytetoString(byf));
        ECDSASignatureData signatureData = ecdsaSign.secp256SignMessage(Hex.decode(rewardsTransaction.getHash()), ecKeyPair1);
        rewardsTransaction.setSignature(signatureData);
        publisher.publish(rewardsTransaction);
        signatureEventHandler.getLatch().await();
        publisher.getJobSyncUntilRemainingCapacityZero();
        assertTrue(mesages.isEmpty());
    }

    @SneakyThrows
    @Test
    public void staking_test() throws InterruptedException {
        MemoryTransactionPool.getInstance().clear();
        signatureEventHandler.setLatch(new CountDownLatch(1));
        ArrayList<String>mesages = new ArrayList<>();
        TransactionCallback transactionCallback = new TransactionCallback() {
            @Override
            public void call(String value) {
                mesages.add(value);
            }
        };
        StakingTransaction stakingTransaction  = new StakingTransaction();
        stakingTransaction.setValidatorAddress(address1);
        stakingTransaction.setType(TransactionType.STAKING);
        stakingTransaction.setStatus(StatusType.PENDING);
        stakingTransaction.setTimestamp(GetTime.GetTimeStampInString());
        stakingTransaction.setDetails("Details");
        stakingTransaction.setWebsite("Website");
        stakingTransaction.setName("Name");
        stakingTransaction.setIdentity("Identity");
        stakingTransaction.setCommissionRate(10);
        stakingTransaction.setZoneFrom(0);
        stakingTransaction.setZoneTo(0);
        stakingTransaction.setAmount(100);
        stakingTransaction.setAmountWithTransactionFee((double) (100 * 10) /100);
        stakingTransaction.setNonce(1);
        stakingTransaction.setTransactionCallback(transactionCallback);
        byte byf[] = serenc.encode(stakingTransaction,1024);
        stakingTransaction.setHash(HashUtil.sha256_bytetoString(byf));
        ECDSASignatureData signatureData = ecdsaSign.secp256SignMessage(Hex.decode(stakingTransaction.getHash()), ecKeyPair1);
        stakingTransaction.setSignature(signatureData);
        publisher.publish(stakingTransaction);
        signatureEventHandler.getLatch().await();
        publisher.getJobSyncUntilRemainingCapacityZero();
        assertTrue(mesages.isEmpty());
    }

    @SneakyThrows
    @Test
    public void un_staking_test() throws InterruptedException {
        MemoryTransactionPool.getInstance().clear();
        ArrayList<String>mesages = new ArrayList<>();
        TransactionCallback transactionCallback = new TransactionCallback() {
            @Override
            public void call(String value) {
                mesages.add(value);
            }
        };
        signatureEventHandler.setLatch(new CountDownLatch(1));
        UnstakingTransaction unstakingTransaction  = new UnstakingTransaction();
        unstakingTransaction.setValidatorAddress(address1);
        unstakingTransaction.setType(TransactionType.UNSTAKING);
        unstakingTransaction.setStatus(StatusType.PENDING);
        unstakingTransaction.setTimestamp(GetTime.GetTimeStampInString());
        unstakingTransaction.setDetails("Details");
        unstakingTransaction.setWebsite("Website");
        unstakingTransaction.setName("Name");
        unstakingTransaction.setIdentity("Identity");
        unstakingTransaction.setCommissionRate(10);
        unstakingTransaction.setZoneFrom(0);
        unstakingTransaction.setZoneTo(0);
        unstakingTransaction.setAmount(100);
        unstakingTransaction.setAmountWithTransactionFee((double) (100 * 10) /100);
        unstakingTransaction.setNonce(1);
        unstakingTransaction.setTransactionCallback(transactionCallback);
        byte byf[] = serenc.encode(unstakingTransaction,1024);
        unstakingTransaction.setHash(HashUtil.sha256_bytetoString(byf));
        ECDSASignatureData signatureData = ecdsaSign.secp256SignMessage(Hex.decode(unstakingTransaction.getHash()), ecKeyPair1);
        unstakingTransaction.setSignature(signatureData);
        publisher.publish(unstakingTransaction);
        signatureEventHandler.getLatch().await();
        publisher.getJobSyncUntilRemainingCapacityZero();
        assertTrue(mesages.isEmpty());
    }

    @Test
    public void delegate_test() throws InterruptedException {
        MemoryTransactionPool.getInstance().clear();
        signatureEventHandler.setLatch(new CountDownLatch(1));
        ArrayList<String>mesages = new ArrayList<>();
        TransactionCallback transactionCallback = new TransactionCallback() {
            @Override
            public void call(String value) {
                mesages.add(value);
            }
        };
        DelegateTransaction delegateTransaction  = new DelegateTransaction();
        delegateTransaction.setDelegatorAddress(address1);
        delegateTransaction.setValidatorAddress(address2);
        delegateTransaction.setType(TransactionType.DELEGATE);
        delegateTransaction.setStatus(StatusType.PENDING);
        delegateTransaction.setTimestamp(GetTime.GetTimeStampInString());
        delegateTransaction.setZoneFrom(0);
        delegateTransaction.setZoneTo(0);
        delegateTransaction.setAmount(100);
        delegateTransaction.setAmountWithTransactionFee((double) (100 * 10) /100);
        delegateTransaction.setNonce(1);
        delegateTransaction.setTransactionCallback(transactionCallback);
        byte byf[] = serenc.encode(delegateTransaction,1024);
        delegateTransaction.setHash(HashUtil.sha256_bytetoString(byf));
        ECDSASignatureData signatureData = ecdsaSign.secp256SignMessage(Hex.decode(delegateTransaction.getHash()), ecKeyPair1);
        delegateTransaction.setSignature(signatureData);
        publisher.publish(delegateTransaction);
        signatureEventHandler.getLatch().await();
        publisher.getJobSyncUntilRemainingCapacityZero();
        assertTrue(mesages.isEmpty());
    }

    @Test
    public void undelegate_test() throws InterruptedException {
        MemoryTransactionPool.getInstance().clear();
        IDatabase<String, TransactionBlock> transactionBlockIDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
        ArrayList<Transaction> list = new ArrayList<>();
        TransactionBlock transactionBlock1 = new TransactionBlock();
        transactionBlock1.setHeight(1);
        transactionBlock1.setHash("hash1");
        DelegateTransaction delegateTransaction = new DelegateTransaction();
        delegateTransaction.setHash("hash1");
        delegateTransaction.setDelegatorAddress(address1);
        delegateTransaction.setValidatorAddress(address2);
        delegateTransaction.setType(TransactionType.DELEGATE);
        list.add(delegateTransaction);
        transactionBlock1.setTransactionList(list);
        int position = Iterables.indexOf(transactionBlock1.getTransactionList(), u -> u.equals(delegateTransaction));
        transactionBlockIDatabase.save(String.valueOf(transactionBlock1.getHeight()), transactionBlock1);
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(address1).get().addTransactionPosition(PatriciaTreeTransactionType.DELEGATE,delegateTransaction.getHash(), 0, transactionBlock1.getHeight(), position);
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(address2).get().addTransactionPosition(PatriciaTreeTransactionType.DELEGATE,delegateTransaction.getHash(), 0, transactionBlock1.getHeight(), position);
        signatureEventHandler.setLatch(new CountDownLatch(1));
        ArrayList<String>mesages = new ArrayList<>();
        TransactionCallback transactionCallback = new TransactionCallback() {
            @Override
            public void call(String value) {
                mesages.add(value);
            }
        };
        UnDelegateTransaction unDelegateTransaction = new UnDelegateTransaction();
        unDelegateTransaction.setDelegatorAddress(address1);
        unDelegateTransaction.setValidatorAddress(address2);
        unDelegateTransaction.setType(TransactionType.UNDELEGATE);
        unDelegateTransaction.setStatus(StatusType.PENDING);
        unDelegateTransaction.setTimestamp(GetTime.GetTimeStampInString());
        unDelegateTransaction.setZoneFrom(0);
        unDelegateTransaction.setZoneTo(0);
        unDelegateTransaction.setAmount(100);
        unDelegateTransaction.setAmountWithTransactionFee((double) (100 * 10) /100);
        unDelegateTransaction.setTransactionCallback(transactionCallback);
        unDelegateTransaction.setNonce(1);
        byte byf[] = serenc.encode(unDelegateTransaction,1024);
        unDelegateTransaction.setHash(HashUtil.sha256_bytetoString(byf));
        ECDSASignatureData signatureData = ecdsaSign.secp256SignMessage(Hex.decode(unDelegateTransaction.getHash()), ecKeyPair1);
        unDelegateTransaction.setSignature(signatureData);
        publisher.publish(unDelegateTransaction);
        signatureEventHandler.getLatch().await();
        publisher.getJobSyncUntilRemainingCapacityZero();

    }

    @Test
    public void delegate2WithTimestampDelay_test() throws InterruptedException {
        IDatabase<String, TransactionBlock> transactionBlockIDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
        MemoryTransactionPool.getInstance().clear();
        TransactionBlock transactionBlock = new TransactionBlock();
        transactionBlock.setHash("Hash1");
        transactionBlock.setHeight(1);
        ArrayList<Transaction> list = new ArrayList<>();
        String time1=GetTime.GetTimeStampInString();
        Thread.sleep(500);
        String time2=GetTime.GetTimeStampInString();
        signatureEventHandler.setLatch(new CountDownLatch(1));
        ArrayList<String>mesages = new ArrayList<>();
        TransactionCallback transactionCallback = new TransactionCallback() {
            @Override
            public void call(String value) {
                mesages.add(value);
            }
        };
        DelegateTransaction delegateTransaction  = new DelegateTransaction();
        delegateTransaction.setDelegatorAddress(address1);
        delegateTransaction.setValidatorAddress(address2);
        delegateTransaction.setType(TransactionType.DELEGATE);
        delegateTransaction.setStatus(StatusType.PENDING);
        delegateTransaction.setTimestamp(time2);
        delegateTransaction.setZoneFrom(0);
        delegateTransaction.setZoneTo(0);
        delegateTransaction.setAmount(100);
        delegateTransaction.setAmountWithTransactionFee((double) (100 * 10) /100);
        delegateTransaction.setNonce(1);
        byte byf[] = serenc.encode(delegateTransaction,1024);
        delegateTransaction.setHash(HashUtil.sha256_bytetoString(byf));
        ECDSASignatureData signatureData = ecdsaSign.secp256SignMessage(Hex.decode(delegateTransaction.getHash()), ecKeyPair1);
        delegateTransaction.setSignature(signatureData);
        list.add(delegateTransaction);
        transactionBlock.setTransactionList(list);
        transactionBlockIDatabase.save(String.valueOf(transactionBlock.getHeight()),transactionBlock);
        int position = Iterables.indexOf(transactionBlock.getTransactionList(), u -> u.equals(delegateTransaction));
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(address1).get().addTransactionPosition(PatriciaTreeTransactionType.DELEGATE,delegateTransaction.getHash(), CachedZoneIndex.getInstance().getZoneIndex(), transactionBlock.getHeight(), position);

        DelegateTransaction delegateTransaction2  = new DelegateTransaction();
        delegateTransaction2.setDelegatorAddress(address1);
        delegateTransaction2.setValidatorAddress(address2);
        delegateTransaction2.setType(TransactionType.DELEGATE);
        delegateTransaction2.setStatus(StatusType.PENDING);
        delegateTransaction2.setTimestamp(time1);
        delegateTransaction2.setZoneFrom(0);
        delegateTransaction2.setZoneTo(0);
        delegateTransaction2.setAmount(10);
        delegateTransaction2.setAmountWithTransactionFee((double) (10 * 10) /100);
        delegateTransaction2.setNonce(1);
        delegateTransaction2.setTransactionCallback(transactionCallback);
        byte byf1[] = serenc.encode(delegateTransaction2,1024);
        delegateTransaction2.setHash(HashUtil.sha256_bytetoString(byf1));
        ECDSASignatureData signatureData2 = ecdsaSign.secp256SignMessage(Hex.decode(delegateTransaction2.getHash()), ecKeyPair1);
        delegateTransaction2.setSignature(signatureData2);

        publisher.publish(delegateTransaction2);
        signatureEventHandler.getLatch().await();
        publisher.getJobSyncUntilRemainingCapacityZero();
        assertEquals(2,mesages.size());

    }
    @Test
    public void jsut_test() {
        Transaction trx=new RegularTransaction();
        trx.setType(TransactionType.REGULAR);
        PatriciaTreeTransactionType patriciaTreeTransactionType = PatriciaTreeTransactionType.valueOf(trx.getType().toString());
        assertEquals(PatriciaTreeTransactionType.REGULAR,patriciaTreeTransactionType);
    }
}
