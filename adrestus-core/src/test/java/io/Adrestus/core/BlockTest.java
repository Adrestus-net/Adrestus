package io.Adrestus.core;

import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.MemoryTreePool;
import io.Adrestus.core.RingBuffer.handler.transactions.SignatureEventHandler;
import io.Adrestus.core.RingBuffer.publisher.BlockEventPublisher;
import io.Adrestus.core.RingBuffer.publisher.TransactionEventPublisher;
import io.Adrestus.core.Trie.PatriciaTreeNode;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.WalletAddress;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import io.Adrestus.crypto.elliptic.SignatureData;
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import org.apache.commons.codec.binary.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlockTest {
    public static ArrayList<String> addreses;
    private static ArrayList<ECKeyPair> keypair;
    private static SerializationUtil<AbstractBlock> serenc = new SerializationUtil<AbstractBlock>(AbstractBlock.class);
    private static ECDSASign ecdsaSign = new ECDSASign();
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

    @BeforeAll
    public static void setup() {
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
    public void block_test() throws Exception {
        AbstractBlock t = new TransactionBlock();
        t.setHash("hash");
        t.accept(new Genesis());
    }

    @Test
    public void block_test2() {
        DefaultFactory factory = new DefaultFactory(new TransactionBlock(), new CommitteeBlock());
        var genesis = (Genesis) factory.getBlock(BlockType.GENESIS);
        var regural_block = factory.getBlock(BlockType.REGULAR);
        factory.accept(genesis);
        factory.accept(regural_block);
    }

    @Test
    public void commitee_block() {
        SerializationUtil<CommitteeBlock> encode = new SerializationUtil<CommitteeBlock>(CommitteeBlock.class);
        //byte[] buffer = new byte[200];
        // BinarySerializer<DelegateTransaction> serenc = SerializerBuilder.create().build(DelegateTransaction.class);
        CommitteeBlock block = new CommitteeBlock();
        block.setHash("hash1");
        block.setSize(1);
        byte[] buffer = encode.encode(block);

        CommitteeBlock copys = encode.decode(buffer);
        System.out.println(copys.toString());
        assertEquals(copys, block);
    }

    @Test
    public void transaction_block() {
        SerializationUtil<TransactionBlock> encode = new SerializationUtil<TransactionBlock>(TransactionBlock.class);
        //byte[] buffer = new byte[200];
        // BinarySerializer<DelegateTransaction> serenc = SerializerBuilder.create().build(DelegateTransaction.class);
        TransactionBlock block = new TransactionBlock();
        block.setHash("hash10");
        block.setSize(1);
        block.setZone(0);
        byte[] buffer = encode.encode(block);

        TransactionBlock copys = encode.decode(buffer);
        System.out.println(copys.toString());
        assertEquals(copys, block);
    }

    @Test
    public void block_test3() throws Exception {
        TransactionEventPublisher publisher = new TransactionEventPublisher(1024);

        publisher
                .withAddressSizeEventHandler()
                .withAmountEventHandler()
                .withDelegateEventHandler()
                .withDoubleSpendEventHandler()
                .withHashEventHandler()
                .withNonceEventHandler()
                .withReplayEventHandler()
                .withRewardEventHandler()
                .withStakingEventHandler()
                .withTransactionFeeEventHandler()
                .withTimestampEventHandler()
                .mergeEventsAndPassThen(new SignatureEventHandler(SignatureEventHandler.SignatureBehaviorType.SIMPLE_TRANSACTIONS));
        publisher.start();


        SecureRandom random;
        String mnemonic_code = "fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2";
        random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        random.setSeed(Hex.decode(mnemonic_code));

        ECDSASign ecdsaSign = new ECDSASign();

        SerializationUtil<Transaction> serenc = new SerializationUtil<Transaction>(Transaction.class);

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
            MemoryTreePool.getInstance().store(adddress, new PatriciaTreeNode(1000, 0));
        }


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

            SignatureData signatureData = ecdsaSign.secp256SignMessage(Hex.decode(transaction.getHash()), keypair.get(i));
            transaction.setSignature(signatureData);
            //MemoryPool.getInstance().add(transaction);
            publisher.publish(transaction);
            await().atMost(100, TimeUnit.MILLISECONDS);
        }
        publisher.getJobSyncUntilRemainingCapacityZero();
        publisher.close();


        DefaultFactory factory = new DefaultFactory();
        TransactionBlock transactionBlock = new TransactionBlock();
        var regural_block = factory.getBlock(BlockType.REGULAR);
        transactionBlock.accept(regural_block);

        if (transactionBlock.getStatustype().equals(StatusType.ABORT))
            System.out.println("true");
    }

    @Test
    public void commit_block_test() throws Exception {

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

        int version = 0x00;
        addreses = new ArrayList<>();
        keypair = new ArrayList<>();
        char[] mnemonic1 = "sample sail jungle learn general promote task puppy own conduct green affair ".toCharArray();
        char[] mnemonic2 = "photo monitor cushion indicate civil witness orchard estate online favorite sustain extend".toCharArray();
        char[] mnemonic3 = "initial car bulb nature animal honey learn awful grit arrow phrase entire ".toCharArray();
        char[] mnemonic4 = "enrich pulse twin version inject horror village aunt brief magnet blush else ".toCharArray();
        char[] mnemonic5 = "struggle travel ketchup tomato satoshi caught fog process grace pupil item ahead ".toCharArray();
        char[] mnemonic6 = "spare defense enhance settle sun educate peace steel broken praise fluid intact ".toCharArray();
        char[] mnemonic7 = "harvest school flip powder plunge bitter noise artefact actor people motion sport".toCharArray();
        char[] mnemonic8 = "crucial rule cute steak mandate source supply current remove laugh blouse dial".toCharArray();
        char[] mnemonic9 = "skate fluid door glide pause any youth jelly spatial faith chase sad ".toCharArray();
        char[] mnemonic10 = "abstract raise duty scare year add fluid danger include smart senior ensure".toCharArray();
        char[] passphrase = "p4ssphr4se".toCharArray();

        Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
        byte[] key1 = mnem.createSeed(mnemonic1, passphrase);
        byte[] key2 = mnem.createSeed(mnemonic2, passphrase);
        byte[] key3 = mnem.createSeed(mnemonic3, passphrase);
        byte[] key4 = mnem.createSeed(mnemonic4, passphrase);
        byte[] key5 = mnem.createSeed(mnemonic5, passphrase);
        byte[] key6 = mnem.createSeed(mnemonic6, passphrase);
        byte[] key7 = mnem.createSeed(mnemonic7, passphrase);
        byte[] key8 = mnem.createSeed(mnemonic8, passphrase);
        byte[] key9 = mnem.createSeed(mnemonic9, passphrase);
        byte[] key10 = mnem.createSeed(mnemonic10, passphrase);

        ECKeyPair ecKeyPair1 = Keys.createEcKeyPair(new SecureRandom(key1));
        ECKeyPair ecKeyPair2 = Keys.createEcKeyPair(new SecureRandom(key2));
        ECKeyPair ecKeyPair3 = Keys.createEcKeyPair(new SecureRandom(key3));
        ECKeyPair ecKeyPair4 = Keys.createEcKeyPair(new SecureRandom(key4));
        ECKeyPair ecKeyPair5 = Keys.createEcKeyPair(new SecureRandom(key5));
        ECKeyPair ecKeyPair6 = Keys.createEcKeyPair(new SecureRandom(key6));
        ECKeyPair ecKeyPair7 = Keys.createEcKeyPair(new SecureRandom(key7));
        ECKeyPair ecKeyPair8 = Keys.createEcKeyPair(new SecureRandom(key8));
        ECKeyPair ecKeyPair9 = Keys.createEcKeyPair(new SecureRandom(key9));
        ECKeyPair ecKeyPair10 = Keys.createEcKeyPair(new SecureRandom(key10));
        String adddress1 = WalletAddress.generate_address((byte) version, ecKeyPair1.getPublicKey());
        String adddress2 = WalletAddress.generate_address((byte) version, ecKeyPair2.getPublicKey());
        String adddress3 = WalletAddress.generate_address((byte) version, ecKeyPair3.getPublicKey());
        String adddress4 = WalletAddress.generate_address((byte) version, ecKeyPair4.getPublicKey());
        String adddress5 = WalletAddress.generate_address((byte) version, ecKeyPair5.getPublicKey());
        String adddress6 = WalletAddress.generate_address((byte) version, ecKeyPair6.getPublicKey());
        String adddress7 = WalletAddress.generate_address((byte) version, ecKeyPair7.getPublicKey());
        String adddress8 = WalletAddress.generate_address((byte) version, ecKeyPair8.getPublicKey());
        String adddress9 = WalletAddress.generate_address((byte) version, ecKeyPair9.getPublicKey());
        String adddress10 = WalletAddress.generate_address((byte) version, ecKeyPair10.getPublicKey());
        addreses.add(adddress1);
        addreses.add(adddress2);
        addreses.add(adddress3);
        addreses.add(adddress4);
        addreses.add(adddress5);
        addreses.add(adddress6);
        addreses.add(adddress7);
        addreses.add(adddress8);
        addreses.add(adddress9);
        addreses.add(adddress10);
        keypair.add(ecKeyPair1);
        keypair.add(ecKeyPair2);
        keypair.add(ecKeyPair3);
        keypair.add(ecKeyPair4);
        keypair.add(ecKeyPair5);
        keypair.add(ecKeyPair6);
        keypair.add(ecKeyPair7);
        keypair.add(ecKeyPair8);
        keypair.add(ecKeyPair9);
        keypair.add(ecKeyPair10);

        SignatureData signatureData1 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress1)), ecKeyPair1);
        SignatureData signatureData2 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress2)), ecKeyPair2);
        SignatureData signatureData3 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress3)), ecKeyPair3);
        SignatureData signatureData4 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress4)), ecKeyPair4);
        SignatureData signatureData5 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress5)), ecKeyPair5);
        SignatureData signatureData = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress5)), ecKeyPair5);
        SignatureData signatureData6 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress6)), ecKeyPair6);
        SignatureData signatureData7 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress7)), ecKeyPair7);
        SignatureData signatureData8 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress8)), ecKeyPair8);
        SignatureData signatureData9 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress9)), ecKeyPair9);
        SignatureData signatureData10 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress10)), ecKeyPair10);

        MemoryTreePool.getInstance().store(adddress1, new PatriciaTreeNode(1000, 0));
        MemoryTreePool.getInstance().store(adddress2, new PatriciaTreeNode(1000, 0));
        MemoryTreePool.getInstance().store(adddress3, new PatriciaTreeNode(1000, 0));
        MemoryTreePool.getInstance().store(adddress4, new PatriciaTreeNode(1000, 0));
        MemoryTreePool.getInstance().store(adddress5, new PatriciaTreeNode(1000, 0));
        MemoryTreePool.getInstance().store(adddress6, new PatriciaTreeNode(1000, 0));
        MemoryTreePool.getInstance().store(adddress7, new PatriciaTreeNode(1000, 0));
        MemoryTreePool.getInstance().store(adddress8, new PatriciaTreeNode(1000, 0));
        MemoryTreePool.getInstance().store(adddress9, new PatriciaTreeNode(1000, 0));
        MemoryTreePool.getInstance().store(adddress10, new PatriciaTreeNode(1000, 0));

        BlockEventPublisher publisher = new BlockEventPublisher(1024);

        publisher
                .withHashHandler()
                .withHeaderEventHandler()
                .withTimestampEventHandler()
                .withDuplicateHandler()
                .withHeightEventHandler()
                .withSortedStakingEventHandler()
                .withMinimumStakingEventHandler()
                .mergeEvents();


        CommitteeBlock prevblock = new CommitteeBlock();
        prevblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        Thread.sleep(100);
        prevblock.setHash("hash");
        prevblock.setGeneration(0);
        prevblock.setHeight(0);
        CachedLatestBlocks.getInstance().setCommitteeBlock(prevblock);
        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.getStakingMap().put(10.0, new ValidatorAddressData(vk1, adddress1, ecKeyPair1.getPublicKey(), signatureData1));
        committeeBlock.getStakingMap().put(13.0, new ValidatorAddressData(vk2, adddress2, ecKeyPair2.getPublicKey(), signatureData2));
        committeeBlock.getStakingMap().put(7.0, new ValidatorAddressData(vk3, adddress3, ecKeyPair3.getPublicKey(), signatureData3));
        committeeBlock.getStakingMap().put(22.0, new ValidatorAddressData(vk4, adddress4, ecKeyPair4.getPublicKey(), signatureData4));
        committeeBlock.getStakingMap().put(6.0, new ValidatorAddressData(vk6, adddress6, ecKeyPair6.getPublicKey(), signatureData6));
        committeeBlock.getStakingMap().put(32.0, new ValidatorAddressData(vk5, adddress5, ecKeyPair5.getPublicKey(), signatureData5));
        committeeBlock.setGeneration(1);
        committeeBlock.getHeaderData().setPreviousHash("hash");
        committeeBlock.setHeight(1);
        committeeBlock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        Thread.sleep(100);
        String hash = HashUtil.sha256_bytetoString(serenc.encode(committeeBlock));
        committeeBlock.setHash(hash);

        publisher.start();
        Thread.sleep(100);
        publisher.publish(committeeBlock);


        publisher.getJobSyncUntilRemainingCapacityZero();
        publisher.close();
        int i = 2;
    }
}
