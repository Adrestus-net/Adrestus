package io.Adrestus.consensus;

import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.core.*;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.RingBuffer.handler.transactions.SignatureEventHandler;
import io.Adrestus.core.RingBuffer.publisher.TransactionEventPublisher;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.WalletAddress;
import io.Adrestus.crypto.bls.BLSSignatureData;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public class ConsensusTransactionBlockTest {
    private static SecureRandom random;
    private static byte[] pRnd;


    public static void delete() {
        IDatabase<String, TransactionBlock> block_database = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
        IDatabase<String, byte[]> tree_datasbase = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getPatriciaTreeZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));

        tree_datasbase.delete_db();
        block_database.delete_db();

        CachedZoneIndex.getInstance().setZoneIndex(1);
    }

    @BeforeAll
    public static void pre_setup() {
        delete();
        CachedZoneIndex.getInstance().setZoneIndex(1);
        TransactionBlock prevblock = new TransactionBlock();
        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.setGeneration(1);
        committeeBlock.setViewID(1);
        prevblock.setHeight(1);
        prevblock.setHash("hash");
        prevblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        CachedLatestBlocks.getInstance().setCommitteeBlock(committeeBlock);
        CachedLatestBlocks.getInstance().setTransactionBlock(prevblock);
        await().atMost(1, SECONDS);
    }

    public static void setup() throws Exception {
        pRnd = new byte[20];
        random = new SecureRandom();
        random.nextBytes(pRnd);

        TransactionEventPublisher publisher = new TransactionEventPublisher(1024);
        SignatureEventHandler signatureEventHandler = new SignatureEventHandler(SignatureEventHandler.SignatureBehaviorType.SIMPLE_TRANSACTIONS);
        publisher
                .withAddressSizeEventHandler()
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
                .withZoneEventHandler()
                .withDuplicateEventHandler()
                .withMinimumStakingEventHandler()
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
            TreeFactory.getMemoryTree(1).store(adddress, new PatriciaTreeNode(1000, 0));
        }


        signatureEventHandler.setLatch(new CountDownLatch(size - 1));
        for (int i = 0; i < size - 1; i++) {
            Transaction transaction = new RegularTransaction();
            transaction.setFrom(addreses.get(i));
            transaction.setTo(addreses.get(i + 1));
            transaction.setStatus(StatusType.PENDING);
            transaction.setTimestamp(GetTime.GetTimeStampInString());
            transaction.setZoneFrom(1);
            transaction.setZoneTo(0);
            transaction.setAmount(100);
            transaction.setAmountWithTransactionFee(transaction.getAmount() * (10.0 / 100.0));
            transaction.setNonce(1);
            byte byf[] = serenc.encode(transaction);
            transaction.setHash(HashUtil.sha256_bytetoString(byf));

            ECDSASignatureData signatureData = ecdsaSign.secp256SignMessage(Hex.decode(transaction.getHash()), keypair.get(i));
            transaction.setSignature(signatureData);
            //MemoryPool.getInstance().add(transaction);
            System.out.println(transaction.toString());
            publisher.publish(transaction);
            await().atMost(100, TimeUnit.MILLISECONDS);
        }
        publisher.getJobSyncUntilRemainingCapacityZero();
        signatureEventHandler.getLatch().await();
        publisher.close();


    }

    @Test
    public void ConsensusTransactionTest() throws Exception {

        setup();

        ConsensusManager consensusManager = new ConsensusManager(true);
        consensusManager.changeStateTo(ConsensusRoleType.ORGANIZER);

        BLSPrivateKey leadersk = new BLSPrivateKey(new SecureRandom());
        BLSPublicKey leadervk = new BLSPublicKey(leadersk);

        BLSPrivateKey validator1sk = new BLSPrivateKey(new SecureRandom());
        BLSPublicKey validator1vk = new BLSPublicKey(validator1sk);

        BLSPrivateKey validator2sk = new BLSPrivateKey(new SecureRandom());
        BLSPublicKey validator2vk = new BLSPublicKey(validator2sk);


        var organizerphase = consensusManager.getRole().manufacturePhases(ConsensusType.TRANSACTION_BLOCK);
        TransactionBlock transactionBlock = new TransactionBlock();
        ConsensusMessage<TransactionBlock> consensusMessage = new ConsensusMessage<>(transactionBlock);

        CachedBLSKeyPair.getInstance().setPrivateKey(leadersk);
        CachedBLSKeyPair.getInstance().setPublicKey(leadervk);

        organizerphase.DispersePhase(consensusMessage);

        consensusManager.changeStateTo(ConsensusRoleType.VALIDATOR);
        BFTConsensusPhase validatorphase = (BFTConsensusPhase) consensusManager.getRole().manufacturePhases(ConsensusType.TRANSACTION_BLOCK);
        CachedBLSKeyPair.getInstance().setPrivateKey(validator1sk);
        CachedBLSKeyPair.getInstance().setPublicKey(validator1vk);

        validatorphase.DispersePhase(consensusMessage);


        CachedBLSKeyPair.getInstance().setPrivateKey(leadersk);
        CachedBLSKeyPair.getInstance().setPublicKey(leadervk);

        organizerphase.AnnouncePhase(consensusMessage);

        HashMap<BLSPublicKey, BLSSignatureData> list = new HashMap<>();

        CachedBLSKeyPair.getInstance().setPrivateKey(validator1sk);
        CachedBLSKeyPair.getInstance().setPublicKey(validator1vk);

        validatorphase.AnnouncePhase(consensusMessage);
        if (consensusMessage.getStatusType().equals(ConsensusStatusType.SUCCESS)) {
            BLSSignatureData blsSignatureData = new BLSSignatureData();
            blsSignatureData.getSignature()[0] = consensusMessage.getChecksumData().getSignature();
            list.put(consensusMessage.getChecksumData().getBlsPublicKey(), blsSignatureData);
        }

        CachedBLSKeyPair.getInstance().setPrivateKey(validator2sk);
        CachedBLSKeyPair.getInstance().setPublicKey(validator2vk);

        validatorphase.AnnouncePhase(consensusMessage);
        if (consensusMessage.getStatusType().equals(ConsensusStatusType.SUCCESS)) {
            BLSSignatureData blsSignatureData = new BLSSignatureData();
            blsSignatureData.getSignature()[0] = consensusMessage.getChecksumData().getSignature();
            list.put(consensusMessage.getChecksumData().getBlsPublicKey(), blsSignatureData);
        }


        consensusMessage.clear();
        consensusMessage.setSignatures(list);
        CachedBLSKeyPair.getInstance().setPrivateKey(leadersk);
        CachedBLSKeyPair.getInstance().setPublicKey(leadervk);

        organizerphase.PreparePhase(consensusMessage);

        HashMap<BLSPublicKey, BLSSignatureData> list1 = new HashMap<>();

        CachedBLSKeyPair.getInstance().setPrivateKey(validator1sk);
        CachedBLSKeyPair.getInstance().setPublicKey(validator1vk);

        validatorphase.PreparePhase(consensusMessage);
        if (consensusMessage.getStatusType().equals(ConsensusStatusType.SUCCESS)) {
            BLSSignatureData blsSignatureData = new BLSSignatureData();
            blsSignatureData.getSignature()[1] = consensusMessage.getChecksumData().getSignature();
            list1.put(consensusMessage.getChecksumData().getBlsPublicKey(), blsSignatureData);
        }

        CachedBLSKeyPair.getInstance().setPrivateKey(validator2sk);
        CachedBLSKeyPair.getInstance().setPublicKey(validator2vk);

        validatorphase.PreparePhase(consensusMessage);
        if (consensusMessage.getStatusType().equals(ConsensusStatusType.SUCCESS)) {
            BLSSignatureData blsSignatureData = new BLSSignatureData();
            blsSignatureData.getSignature()[1] = consensusMessage.getChecksumData().getSignature();
            list1.put(consensusMessage.getChecksumData().getBlsPublicKey(), blsSignatureData);
        }


        CachedBLSKeyPair.getInstance().setPrivateKey(leadersk);
        CachedBLSKeyPair.getInstance().setPublicKey(leadervk);

        consensusMessage.clear();
        consensusMessage.setSignatures(list1);

        organizerphase.CommitPhase(consensusMessage);

        CachedBLSKeyPair.getInstance().setPrivateKey(validator1sk);
        CachedBLSKeyPair.getInstance().setPublicKey(validator1vk);

        validatorphase.CommitPhase(consensusMessage);

        CachedBLSKeyPair.getInstance().setPrivateKey(validator2sk);
        CachedBLSKeyPair.getInstance().setPublicKey(validator2vk);

        validatorphase.CommitPhase(consensusMessage);


    }
}
