package io.Adrestus.core;

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
        rewardsTransaction.setAmountWithTransactionFee(0);
        rewardsTransaction.setNonce(1);
        byte byf[] = serenc.encode(rewardsTransaction,1024);
        rewardsTransaction.setHash(HashUtil.sha256_bytetoString(byf));
        ECDSASignatureData signatureData = ecdsaSign.secp256SignMessage(Hex.decode(rewardsTransaction.getHash()), ecKeyPair1);
        rewardsTransaction.setSignature(signatureData);
        publisher.publish(rewardsTransaction);
        signatureEventHandler.getLatch().await();
        publisher.getJobSyncUntilRemainingCapacityZero();
    }

    @SneakyThrows
    @Test
    public void staking_test() throws InterruptedException {
        MemoryTransactionPool.getInstance().clear();
        signatureEventHandler.setLatch(new CountDownLatch(1));
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
        stakingTransaction.setNonce(1);
        byte byf[] = serenc.encode(stakingTransaction,1024);
        stakingTransaction.setHash(HashUtil.sha256_bytetoString(byf));
        ECDSASignatureData signatureData = ecdsaSign.secp256SignMessage(Hex.decode(stakingTransaction.getHash()), ecKeyPair1);
        stakingTransaction.setSignature(signatureData);
        publisher.publish(stakingTransaction);
        signatureEventHandler.getLatch().await();
        publisher.getJobSyncUntilRemainingCapacityZero();
    }

    @Test
    public void jsut_test() {
        Transaction trx=new RegularTransaction();
        trx.setType(TransactionType.REGULAR);
        PatriciaTreeTransactionType patriciaTreeTransactionType = PatriciaTreeTransactionType.valueOf(trx.getType().toString());
        assertEquals(PatriciaTreeTransactionType.REGULAR,patriciaTreeTransactionType);
    }
}
