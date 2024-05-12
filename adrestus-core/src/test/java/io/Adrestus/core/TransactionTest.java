package io.Adrestus.core;

import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.Resourses.MemoryTransactionPool;
import io.Adrestus.core.RingBuffer.handler.transactions.SignatureEventHandler;
import io.Adrestus.core.RingBuffer.publisher.TransactionEventPublisher;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.WalletAddress;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.crypto.elliptic.ECDSASignatureData;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.MnemonicException;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransactionTest {

    @BeforeAll
    public static void setup() throws Exception {
        MemoryTransactionPool.getInstance().clear();
        TransactionBlock prevblock = new TransactionBlock();
        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.setGeneration(1);
        committeeBlock.setViewID(1);
        prevblock.setHeight(1);
        prevblock.setHash("hash");
        prevblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        CachedLatestBlocks.getInstance().setCommitteeBlock(committeeBlock);
        CachedLatestBlocks.getInstance().setTransactionBlock(prevblock);
        await().atMost(100, TimeUnit.MILLISECONDS);
    }

    @Test
    public void Transaction_test() {

        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        SerializationUtil<Transaction> ser = new SerializationUtil<Transaction>(Transaction.class, list);
        Transaction transaction = new RegularTransaction();
        transaction.setAmount(100);
        transaction.setHash("Hash");
        transaction.setType(TransactionType.REGULAR);

        byte[] buffer = ser.encode(transaction);
        Transaction copy = ser.decode(buffer);
        System.out.println(copy.toString());
        assertEquals(copy, transaction);

    }

    @Test
    public void MinimalTransactionSerialization() throws MnemonicException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        ECDSASign ecdsaSign = new ECDSASign();
        Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
        char[] mnemonic_sequence = mnem.create();
        char[] passphrase = "p4ssphr4se".toCharArray();
        byte[] key = mnem.createSeed(mnemonic_sequence, passphrase);
        ECKeyPair ecKeyPair = Keys.createEcKeyPair(new SecureRandom(key));

        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        SerializationUtil<Transaction> ser = new SerializationUtil<Transaction>(Transaction.class, list);
        Transaction transaction = new RegularTransaction();
        transaction.setAmount(1000000);
        transaction.setHash("Hash");
        transaction.setType(TransactionType.REGULAR);
        transaction.setTimestamp(GetTime.GetTimeStampInString());
        transaction.setStatus(StatusType.PENDING);
        transaction.setAmountWithTransactionFee(100);
        transaction.setNonce(10000);
        transaction.setBlockNumber(1000);
        transaction.setXAxis(new BigInteger("73885651435926854515264701221164520142160681037984229233067136520784684869519"));
        transaction.setYAxis(new BigInteger("73885651435926854515264701221164520142160681037984229233067136520784684869519"));
        transaction.setFrom("ADR-GC2I-WBAW-IKJE-BWFC-ML6T-BNOC-7XOU-IQ74-BJ5L-WP7G");
        transaction.setTo("ADR-GC2I-WBAW-IKJE-BWFC-ML6T-BNOC-7XOU-IQ74-BJ5L-WP7G");
        transaction.setZoneFrom(0);
        transaction.setZoneTo(0);
        transaction.setType(TransactionType.REGULAR);
        byte byf[] = ser.encode(transaction);
        transaction.setHash(HashUtil.sha256_bytetoString(byf));

        ECDSASignatureData signatureData = ecdsaSign.secp256SignMessage(Hex.decode(transaction.getHash()), ecKeyPair);
        transaction.setSignature(signatureData);

        byte[] buffer = ser.encode(transaction, 400);
        Transaction copy = ser.decode(buffer);
        System.out.println(copy.toString());
        assertEquals(copy, transaction);

    }

    @Test
    public void rewards_test() {

        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        SerializationUtil<Transaction> ser = new SerializationUtil<Transaction>(Transaction.class, list);
        UnclaimedFeeRewardTransaction transaction = new UnclaimedFeeRewardTransaction();
        transaction.setRecipientAddress("From1");
        transaction.setType(TransactionType.UNCLAIMED_FEE_REWARD);
        transaction.setHash("Hash");
        //transaction.setType(TransactionType.REGULAR);

        byte[] buffer = ser.encode(transaction);
        Transaction copy = ser.decode(buffer);
        System.out.println(copy.toString());
        assertEquals(copy, transaction);

    }

    @Test
    public void StakingTransaction_test() {
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        SerializationUtil<Transaction> serenc = new SerializationUtil<Transaction>(Transaction.class, list);
        Transaction stakingTransaction = new StakingTransaction();
        stakingTransaction.setAmount(100);
        stakingTransaction.setType(TransactionType.STAKING);
        byte[] buffer = serenc.encode(stakingTransaction);

        Transaction copys = serenc.decode(buffer);
        System.out.println(copys.toString());
        assertEquals(copys, stakingTransaction);
    }

    @Test
    public void RewardTransaction_test() {
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        SerializationUtil<Transaction> serenc = new SerializationUtil<Transaction>(Transaction.class, list);
        RewardsTransaction rewardsTransaction = new RewardsTransaction("Del");
        rewardsTransaction.setAmount(100);
        rewardsTransaction.setType(TransactionType.REWARDS);
        byte[] buffers = serenc.encode(rewardsTransaction);

        Transaction copys = serenc.decode(buffers);
        System.out.println(copys.toString());
        assertEquals(copys, rewardsTransaction);
    }

    @Test
    public void DelegateTransaction_test() {
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        SerializationUtil<Transaction> serenc = new SerializationUtil<Transaction>(Transaction.class, list);
        //byte[] buffer = new byte[200];
        // BinarySerializer<DelegateTransaction> serenc = SerializerBuilder.create().build(DelegateTransaction.class);
        Transaction delegateTransaction = new DelegateTransaction();
        delegateTransaction.setAmount(100);
        delegateTransaction.setType(TransactionType.DELEGATING);
        byte[] buffer = serenc.encode(delegateTransaction);

        Transaction copys = serenc.decode(buffer);
        System.out.println(copys.toString());

    }


    @Test
    public void StressTesting() throws Exception {
        CachedZoneIndex.getInstance().setZoneIndex(0);
        TransactionEventPublisher publisher = new TransactionEventPublisher(1024);
        SignatureEventHandler signatureEventHandler = new SignatureEventHandler(SignatureEventHandler.SignatureBehaviorType.SIMPLE_TRANSACTIONS);
        publisher
                .withAddressSizeEventHandler()
                .withTypeEventHandler()
                .withAmountEventHandler()
                .withDoubleSpendEventHandler()
                .withHashEventHandler()
                .withNonceEventHandler()
                .withReplayEventHandler()
                .withStakingEventHandler()
                .withTransactionFeeEventHandler()
                .withTimestampEventHandler()
                .withSameOriginEventHandler()
                .withZoneEventHandler()
                .withDuplicateEventHandler()
                .mergeEventsAndPassThen(signatureEventHandler);
        publisher.start();


        SecureRandom random;
        String mnemonic_code = "fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2";
        random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(Hex.decode(mnemonic_code));

        ECDSASign ecdsaSign = new ECDSASign();

        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        SerializationUtil<Transaction> serenc = new SerializationUtil<Transaction>(Transaction.class, list);

        ArrayList<String> addreses = new ArrayList<>();
        ArrayList<ECKeyPair> keypair = new ArrayList<>();
        int version = 0x00;
        int size = 5;
        for (int i = 0; i < size; i++) {
            Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
            char[] mnemonic_sequence = mnem.create();
            char[] passphrase = "p4ssphr4se".toCharArray();
            byte[] key = mnem.createSeed(mnemonic_sequence, passphrase);
            ECKeyPair ecKeyPair = Keys.createEcKeyPair(new SecureRandom(key));
            String adddress = WalletAddress.generate_address((byte) version, ecKeyPair.getPublicKey());
            addreses.add(adddress);
            keypair.add(ecKeyPair);
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(adddress, new PatriciaTreeNode(1000, 0));
        }


        signatureEventHandler.setLatch(new CountDownLatch(size - 1));
        for (int i = 0; i < size - 1; i++) {
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
            byte byf[] = serenc.encode(transaction);
            transaction.setHash(HashUtil.sha256_bytetoString(byf));

            ECDSASignatureData signatureData = ecdsaSign.secp256SignMessage(Hex.decode(transaction.getHash()), keypair.get(i));
            transaction.setSignature(signatureData);

            publisher.publish(transaction);
            await().atMost(100, TimeUnit.MILLISECONDS);
            //publisher.publish(transaction);
        }
        publisher.getJobSyncUntilRemainingCapacityZero();
        signatureEventHandler.getLatch().await();
        publisher.close();
    }

}
