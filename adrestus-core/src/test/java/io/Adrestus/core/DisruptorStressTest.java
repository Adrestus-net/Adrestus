package io.Adrestus.core;

import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.core.Resourses.CachedTransactionBlockEventPublisher;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.Resourses.MemoryTransactionPool;
import io.Adrestus.core.RingBuffer.handler.transactions.SignatureEventHandler;
import io.Adrestus.core.RingBuffer.publisher.BlockEventPublisher;
import io.Adrestus.core.RingBuffer.publisher.TransactionEventPublisher;
import io.Adrestus.crypto.HashUtil;
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
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.MnemonicException;
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
import org.spongycastle.util.encoders.Hex;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

public class DisruptorStressTest {
    private static BLSPrivateKey sk1;
    private static BLSPublicKey vk1;

    @SneakyThrows
    @BeforeAll
    public static void setup() {
        sk1 = new BLSPrivateKey(1);
        vk1 = new BLSPublicKey(sk1);
    }

    //@Test
    public void TransactionTest() throws NoSuchAlgorithmException, NoSuchProviderException, MnemonicException, InvalidAlgorithmParameterException, InterruptedException {
        CachedZoneIndex.getInstance().setZoneIndex(0);
        TransactionEventPublisher publisher = new TransactionEventPublisher(200240);
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


        SecureRandom random;
        String mnemonic_code = "fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2";
        random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(Hex.decode(mnemonic_code));

        ECDSASign ecdsaSign = new ECDSASign();

        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        SerializationUtil<Transaction> serenc = new SerializationUtil<Transaction>(Transaction.class, list);

        ArrayList<String> addreses = new ArrayList<>();
        ArrayList<ECKeyPair> keypair = new ArrayList<>();
        int version = 0x00;
        int size = 100;
        signatureEventHandler.setLatch(new CountDownLatch(size - 1));
        for (int i = 0; i < size; i++) {
            Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
            char[] mnemonic_sequence = mnem.create();
            char[] passphrase = "p4ssphr4se".toCharArray();
            byte[] key = mnem.createSeed(mnemonic_sequence, passphrase);
            ECKeyPair ecKeyPair = Keys.createEcKeyPair(new SecureRandom(key));
            String adddress = WalletAddress.generate_address((byte) version, ecKeyPair.getPublicKey());
            addreses.add(adddress);
            keypair.add(ecKeyPair);
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(adddress, new PatriciaTreeNode(BigDecimal.valueOf(1000, 0)));
        }

        ArrayList<Transaction> list2 = new ArrayList<>();
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
            byte byf[] = serenc.encode(transaction);
            transaction.setHash(HashUtil.sha256_bytetoString(byf));
            ECDSASignatureData signatureData = ecdsaSign.secp256SignMessage(Hex.decode(transaction.getHash()), keypair.get(i));
            transaction.setSignature(signatureData);
            list2.add(transaction);
        }
        for (int i = 0; i < 2; i++) {
            publisher.publish(list2.get(i));
        }
        publisher.getJobSyncUntilRemainingCapacityZero();
        TimerTask task = new TimerTask() {
            public void run() {
                System.out.println("Timer : " + MemoryTransactionPool.getInstance().getSize());
            }
        };
        Timer timer = new Timer("Timer");

        long delay = 2000;
        timer.scheduleAtFixedRate(task, delay, delay);
        for (int i = 2; i < list2.size(); i++) {
            publisher.publish(list2.get(i));
        }
        publisher.getJobSyncUntilRemainingCapacityZero();
        signatureEventHandler.getLatch().await();
        publisher.close();
        System.out.println(MemoryTransactionPool.getInstance().getSize());
    }

    //@Test
    public void BlockTest() throws NoSuchAlgorithmException, NoSuchProviderException, MnemonicException, InvalidAlgorithmParameterException, InterruptedException {
        BlockEventPublisher publisher = new BlockEventPublisher(1024);

        TransactionBlock transactionBlock = new TransactionBlock();
        publisher
                .mergeEventsAndPassVerifySig();
        publisher.start();

        SecureRandom random;
        String mnemonic_code = "fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2";
        random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(Hex.decode(mnemonic_code));

        ECDSASign ecdsaSign = new ECDSASign();

        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        SerializationUtil<Transaction> serenc = new SerializationUtil<Transaction>(Transaction.class, list);

        ArrayList<String> addreses = new ArrayList<>();
        ArrayList<ECKeyPair> keypair = new ArrayList<>();
        int version = 0x00;
        int size = 3;
        for (int i = 0; i < size; i++) {
            Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
            char[] mnemonic_sequence = mnem.create();
            char[] passphrase = "p4ssphr4se".toCharArray();
            byte[] key = mnem.createSeed(mnemonic_sequence, passphrase);
            ECKeyPair ecKeyPair = Keys.createEcKeyPair(new SecureRandom(key));
            String adddress = WalletAddress.generate_address((byte) version, ecKeyPair.getPublicKey());
            addreses.add(adddress);
            keypair.add(ecKeyPair);
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(adddress, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        }

        ArrayList<Transaction> list1 = new ArrayList<>();
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
            byte byf[] = serenc.encode(transaction);
            transaction.setHash(HashUtil.sha256_bytetoString(byf));
            ECDSASignatureData signatureData = ecdsaSign.secp256SignMessage(Hex.decode(transaction.getHash()), keypair.get(i));
            transaction.setSignature(signatureData);
            list1.add(transaction);
        }
        while (true) {
            long start = System.currentTimeMillis();
            transactionBlock.setTransactionList(list1);
            CachedTransactionBlockEventPublisher.getInstance().publish(transactionBlock);
            CachedTransactionBlockEventPublisher.getInstance().WaitUntilRemainingCapacityZero();
            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;
            System.out.println(timeElapsed);
            Thread.sleep(350);
        }
    }

    //@Test
    public void SaveBlockTestMeasureTime() {

        int counter = 0;
        while (true) {
            long start = System.currentTimeMillis();
            IDatabase<String, TransactionBlock> transactionBlockIDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(0));
            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;
            System.out.println(timeElapsed);
            TransactionBlock transactionBlock = new TransactionBlock();
            transactionBlock.setHash(String.valueOf(counter));
            transactionBlock.setHeight(counter);
            transactionBlockIDatabase.save(String.valueOf(counter), transactionBlock);
        }
    }

}
