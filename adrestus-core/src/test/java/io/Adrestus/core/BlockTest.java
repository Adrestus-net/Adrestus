package io.Adrestus.core;

import com.google.common.primitives.Ints;
import io.Adrestus.MemoryTreePool;
import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.MerkleNode;
import io.Adrestus.Trie.MerkleTreeImp;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.config.KademliaConfiguration;
import io.Adrestus.core.Resourses.*;
import io.Adrestus.core.RingBuffer.handler.transactions.SignatureEventHandler;
import io.Adrestus.core.RingBuffer.publisher.BlockEventPublisher;
import io.Adrestus.core.RingBuffer.publisher.TransactionEventPublisher;
import io.Adrestus.core.Util.BlockSizeCalculator;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.SecurityAuditProofs;
import io.Adrestus.crypto.WalletAddress;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.crypto.elliptic.ECDSASignatureData;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomSerializerTreeMap;
import io.Adrestus.crypto.elliptic.mapper.StakingData;
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import io.Adrestus.crypto.vdf.engine.VdfEngine;
import io.Adrestus.crypto.vdf.engine.VdfEnginePietrzak;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.MathOperationUtil;
import io.Adrestus.util.SerializationUtil;
import io.distributedLedger.*;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

public class BlockTest {
    private static ArrayList<String> addreses = new ArrayList<>();
    private static ArrayList<ECKeyPair> keypair = new ArrayList<>();
    private static ArrayList<Transaction> transactions = new ArrayList<>();
    private static ArrayList<Transaction> outer_transactions = new ArrayList<>();
    private static SerializationUtil<AbstractBlock> serenc;
    private static SerializationUtil<Transaction> trx_serence;
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

    private static BLSPrivateKey sk7;
    private static BLSPublicKey vk7;

    private static BLSPrivateKey sk8;
    private static BLSPublicKey vk8;

    private static BLSPrivateKey sk9;
    private static BLSPublicKey vk9;

    private static BlockSizeCalculator sizeCalculator;
    private static KademliaData kad1, kad2, kad3, kad4, kad5, kad6;
    private static ECDSASignatureData signatureData1, signatureData2, signatureData3;
    private static TransactionCallback transactionCallback;
    private static ArrayList<String> mesages = new ArrayList<>();
    private static int version = 0x00;
    private static int size = 5;

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

    @SneakyThrows
    @BeforeAll
    public static void setup() {
        delete_test();
        sizeCalculator = new BlockSizeCalculator();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        serenc = new SerializationUtil<AbstractBlock>(AbstractBlock.class, list);
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

        sk7 = new BLSPrivateKey(7);
        vk7 = new BLSPublicKey(sk7);

        sk8 = new BLSPrivateKey(8);
        vk8 = new BLSPublicKey(sk8);

        SecureRandom random;
        String mnemonic_code = "fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2";
        random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(Hex.decode(mnemonic_code));

        ECDSASign ecdsaSign = new ECDSASign();

        List<SerializationUtil.Mapping> list2 = new ArrayList<>();
        list2.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        trx_serence = new SerializationUtil<Transaction>(Transaction.class, list2);

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

        signatureData1 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(addreses.get(0))), keypair.get(0));
        signatureData2 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(addreses.get(1))), keypair.get(1));
        signatureData3 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(addreses.get(2))), keypair.get(2));

        transactionCallback = new TransactionCallback() {
            @Override
            public void call(String value) {
                mesages.add(value);
            }
        };

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
            transaction.setTransactionCallback(transactionCallback);
            byte byf[] = trx_serence.encode(transaction);
            transaction.setHash(HashUtil.sha256_bytetoString(byf));
            await().atMost(500, TimeUnit.MILLISECONDS);

            ECDSASignatureData signatureData = ecdsaSign.secp256SignMessage(Hex.decode(transaction.getHash()), keypair.get(i));
            transaction.setSignature(signatureData);
            //MemoryPool.getInstance().add(transaction);
            transactions.add(transaction);
        }
        for (int i = 0; i < size - 1; i++) {
            Transaction transaction = new RegularTransaction();
            transaction.setFrom(addreses.get(i));
            transaction.setTo(addreses.get(i + 1));
            transaction.setStatus(StatusType.PENDING);
            transaction.setTimestamp(GetTime.GetTimeStampInString());
            transaction.setZoneFrom(0);
            transaction.setZoneTo(1);
            transaction.setAmount(100);
            transaction.setAmountWithTransactionFee(transaction.getAmount() * (10.0 / 100.0));
            transaction.setNonce(1);
            byte byf[] = trx_serence.encode(transaction);
            transaction.setHash(HashUtil.sha256_bytetoString(byf));
            await().atMost(500, TimeUnit.MILLISECONDS);

            ECDSASignatureData signatureData = ecdsaSign.secp256SignMessage(Hex.decode(transaction.getHash()), keypair.get(i));
            transaction.setSignature(signatureData);
            //MemoryPool.getInstance().add(transaction);
            outer_transactions.add(transaction);
        }

    }

    @Test
    public void block_test() throws Exception {
        AbstractBlock t = new TransactionBlock();
        t.setHash("hash");
        t.accept(new Genesis());
    }

    @Test
    public void block_test2() {
        // CachedSecurityHeaders.getInstance().getSecurityHeader().setpRnd(Hex.decode("c1f72aa5bd1e1d53c723b149259b63f759f40d5ab003b547d5c13d45db9a5da8"));
        // CachedSecurityHeaders.getInstance().getSecurityHeader().setRnd(Hex.decode("c1f72aa5bd1e1d53c723b149259b63f759f40d5ab003b547d5c13d45db9a5da8"));
        DefaultFactory factory = new DefaultFactory(new TransactionBlock(), new CommitteeBlock());
        var genesis = (Genesis) factory.getBlock(BlockType.GENESIS);
        var regural_block = factory.getBlock(BlockType.REGULAR);
        //factory.accept(genesis);
        //factory.accept(regural_block);
        int g = 4;
    }

    @Test
    public void commitee_block() {
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        SerializationUtil<CommitteeBlock> encode = new SerializationUtil<CommitteeBlock>(CommitteeBlock.class, list);
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
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        SerializationUtil<TransactionBlock> encode = new SerializationUtil<TransactionBlock>(TransactionBlock.class, list);

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
        CachedZoneIndex.getInstance().setZoneIndex(0);
        TransactionEventPublisher publisher = new TransactionEventPublisher(1024);
        SignatureEventHandler signatureEventHandler = new SignatureEventHandler(SignatureEventHandler.SignatureBehaviorType.SIMPLE_TRANSACTIONS);
        CachedBLSKeyPair.getInstance().setPublicKey(vk1);
        publisher
                .withAddressSizeEventHandler()
                .withTypeEventHandler()
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


        signatureEventHandler.setLatch(new CountDownLatch(size - 1));
        for (int i = 0; i < transactions.size(); i++) {
            publisher.publish(transactions.get(i));
            await().atMost(1000, TimeUnit.MILLISECONDS);
        }
        publisher.getJobSyncUntilRemainingCapacityZero();
        signatureEventHandler.getLatch().await();
        assertEquals(size - 1, MemoryTransactionPool.getInstance().getSize());
        assertTrue(mesages.isEmpty());
        publisher.close();


        DefaultFactory factory = new DefaultFactory();
        TransactionBlock transactionBlock = new TransactionBlock();
        var regural_block = factory.getBlock(BlockType.REGULAR);
        transactionBlock.accept(regural_block);


    }

    @Test
    public void transaction_block_test() throws Exception {
        CachedZoneIndex.getInstance().setZoneIndex(0);
        CachedBLSKeyPair.getInstance().setPrivateKey(sk1);
        CachedBLSKeyPair.getInstance().setPublicKey(vk1);
        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.setGeneration(1);
        committeeBlock.setViewID(1);
        CachedLatestBlocks.getInstance().setCommitteeBlock(committeeBlock);

        kad1 = new KademliaData(new SecurityAuditProofs(addreses.get(0), vk1, keypair.get(0).getPublicKey(), signatureData1), new NettyConnectionInfo("192.168.1.106", KademliaConfiguration.PORT));
        kad2 = new KademliaData(new SecurityAuditProofs(addreses.get(1), vk2, keypair.get(1).getPublicKey(), signatureData2), new NettyConnectionInfo("192.168.1.115", KademliaConfiguration.PORT));
        kad3 = new KademliaData(new SecurityAuditProofs(addreses.get(2), vk3, keypair.get(2).getPublicKey(), signatureData3), new NettyConnectionInfo("192.168.1.116", KademliaConfiguration.PORT));

        CachedLatestBlocks.getInstance().getCommitteeBlock().getStakingMap().put(new StakingData(1, 10.0), kad1);
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStakingMap().put(new StakingData(2, 11.0), kad2);
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStakingMap().put(new StakingData(3, 151.0), kad3);

        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).put(vk1, "192.168.1.106");
        //CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).put(vk2, "192.168.1.110");
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).put(vk2, "192.168.1.115");
        // CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).put(vk4, "192.168.1.115");
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).put(vk3, "192.168.1.116");

        CachedLeaderIndex.getInstance().setTransactionPositionLeader(1);

        BlockEventPublisher publisher = new BlockEventPublisher(AdrestusConfiguration.BLOCK_QUEUE_SIZE);
        publisher
                .withDuplicateHandler()
                .withGenerationHandler()
                .withHashHandler()
                .withHeaderEventHandler()
                .withHeightEventHandler()
                .withViewIDEventHandler()
                .withTimestampEventHandler()
                .withTransactionMerkleeEventHandler()
                .withInBoundEventHandler()
                .withOutBoundEventHandler()
                .withPatriciaTreeEventHandler()
                .withLeaderFeeRewardEventHandler()
                .withReplayFeeEventHandler()
                .withSumFeeRewardEventHandler()
                .mergeEventsAndPassVerifySig();

        publisher.start();

        TransactionBlock prevblock = new TransactionBlock();
        prevblock.setHeight(1);
        prevblock.setHash("hash");
        prevblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        Thread.sleep(1000);
        prevblock.setBlockProposer(vk1.toRaw());
        prevblock.setLeaderPublicKey(vk1);
        CachedLatestBlocks.getInstance().setTransactionBlock(prevblock);


        BlockIndex blockIndex = new BlockIndex();
        double sum = transactions.parallelStream().filter(val -> !val.getType().equals(TransactionType.UNCLAIMED_FEE_REWARD)).mapToDouble(Transaction::getAmountWithTransactionFee).sum();
        try {
            transactions.add(0, new UnclaimedFeeRewardTransaction(TransactionType.UNCLAIMED_FEE_REWARD, blockIndex.getAddressByPublicKey(CachedBLSKeyPair.getInstance().getPublicKey()), sum));
        } catch (NoSuchElementException e) {
        }

        int count = 0;
        while (count < 100) {
            TransactionBlock transactionBlock = new TransactionBlock();
            MerkleTreeImp tree = new MerkleTreeImp();
            ArrayList<MerkleNode> merkleNodeArrayList = new ArrayList<>();
            transactionBlock.getHeaderData().setPreviousHash(CachedLatestBlocks.getInstance().getTransactionBlock().getHash());
            transactionBlock.getHeaderData().setVersion(AdrestusConfiguration.version);
            transactionBlock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
            transactionBlock.setStatustype(StatusType.PENDING);
            transactionBlock.setHeight(CachedLatestBlocks.getInstance().getTransactionBlock().getHeight() + 1);
            transactionBlock.setGeneration(CachedLatestBlocks.getInstance().getCommitteeBlock().getGeneration());
            transactionBlock.setViewID(CachedLatestBlocks.getInstance().getTransactionBlock().getViewID() + 1);
            transactionBlock.setZone(CachedZoneIndex.getInstance().getZoneIndex());
            transactionBlock.setBlockProposer(CachedBLSKeyPair.getInstance().getPublicKey().toRaw());
            transactionBlock.setLeaderPublicKey(CachedBLSKeyPair.getInstance().getPublicKey());
            transactionBlock.setTransactionList((List<Transaction>) transactions.clone());


            transactionBlock.getTransactionList().forEach(transaction -> merkleNodeArrayList.add(new MerkleNode(transaction.getHash())));
            tree.my_generate2(merkleNodeArrayList);
            transactionBlock.setMerkleRoot(tree.getRootHash());

            //##########OutBound############
            Receipt.ReceiptBlock receiptBlock = new Receipt.ReceiptBlock(transactionBlock.getHeight(), transactionBlock.getGeneration(), transactionBlock.getMerkleRoot());
            ArrayList<Receipt> receiptList = new ArrayList<>();
            for (int i = 0; i < transactionBlock.getTransactionList().size(); i++) {
                Transaction transaction = transactionBlock.getTransactionList().get(i);
                if (transaction.getZoneFrom() != transaction.getZoneTo()) {
                    MerkleNode node = new MerkleNode(transaction.getHash());
                    tree.build_proofs2(merkleNodeArrayList, node);
                    receiptList.add(new Receipt(transaction.getZoneFrom(), transaction.getZoneTo(), receiptBlock, tree.getMerkleeproofs(), i));
                }
            }

            Map<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> outbound = receiptList
                    .stream()
                    .collect(Collectors.groupingBy(Receipt::getZoneTo, Collectors.groupingBy(Receipt::getReceiptBlock)));

            OutBoundRelay outBoundRelay = new OutBoundRelay(outbound);
            transactionBlock.setOutbound(outBoundRelay);

            Map<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> inbound_map = receiptList
                    .stream()
                    .collect(Collectors.groupingBy(Receipt::getZoneFrom, Collectors.groupingBy(Receipt::getReceiptBlock)));
            InboundRelay inboundRelay = new InboundRelay(inbound_map);
            transactionBlock.setInbound(inboundRelay);

            MemoryTreePool replica = new MemoryTreePool(((MemoryTreePool) TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex())));
            if (!transactionBlock.getTransactionList().isEmpty()) {
                TreePoolConstructBlock.getInstance().visitForgeTreePool(transactionBlock, replica);
            }

            transactionBlock.setPatriciaMerkleRoot(replica.getRootHash());
            BlockSizeCalculator blockSizeCalculator = new BlockSizeCalculator();
            blockSizeCalculator.setTransactionBlock(transactionBlock);
            byte[] tohash = serenc.encode(transactionBlock, blockSizeCalculator.TransactionBlockSizeCalculator());
            transactionBlock.setHash(HashUtil.sha256_bytetoString(tohash));
            publisher.publish(transactionBlock);
            publisher.getJobSyncUntilRemainingCapacityZero();
            count++;
        }
        publisher.close();


    }

    @Test
    public void transaction_block_test_outer() throws Exception {
        CachedZoneIndex.getInstance().setZoneIndex(0);
        CachedBLSKeyPair.getInstance().setPrivateKey(sk1);
        CachedBLSKeyPair.getInstance().setPublicKey(vk1);
        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.setGeneration(1);
        committeeBlock.setViewID(1);
        CachedLatestBlocks.getInstance().setCommitteeBlock(committeeBlock);

        kad1 = new KademliaData(new SecurityAuditProofs(addreses.get(0), vk1, keypair.get(0).getPublicKey(), signatureData1), new NettyConnectionInfo("192.168.1.106", KademliaConfiguration.PORT));
        kad2 = new KademliaData(new SecurityAuditProofs(addreses.get(1), vk2, keypair.get(1).getPublicKey(), signatureData2), new NettyConnectionInfo("192.168.1.115", KademliaConfiguration.PORT));
        kad3 = new KademliaData(new SecurityAuditProofs(addreses.get(2), vk3, keypair.get(2).getPublicKey(), signatureData3), new NettyConnectionInfo("192.168.1.116", KademliaConfiguration.PORT));

        CachedLatestBlocks.getInstance().getCommitteeBlock().getStakingMap().put(new StakingData(1, 10.0), kad1);
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStakingMap().put(new StakingData(2, 11.0), kad2);
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStakingMap().put(new StakingData(3, 151.0), kad3);

        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).put(vk1, "192.168.1.106");
        //CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).put(vk2, "192.168.1.110");
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).put(vk2, "192.168.1.115");
        // CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).put(vk4, "192.168.1.115");
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).put(vk3, "192.168.1.116");

        IDatabase<String, CommitteeBlock> database = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);
        database.save(String.valueOf(CachedLatestBlocks.getInstance().getCommitteeBlock().getGeneration()), CachedLatestBlocks.getInstance().getCommitteeBlock());
        CachedLeaderIndex.getInstance().setTransactionPositionLeader(1);

        BlockEventPublisher publisher = new BlockEventPublisher(AdrestusConfiguration.BLOCK_QUEUE_SIZE);
        publisher
                .withDuplicateHandler()
                .withGenerationHandler()
                .withHashHandler()
                .withHeaderEventHandler()
                .withHeightEventHandler()
                .withViewIDEventHandler()
                .withTimestampEventHandler()
                .withTransactionMerkleeEventHandler()
                .withInBoundEventHandler()
                .withOutBoundEventHandler()
                .withPatriciaTreeEventHandler()
                .withLeaderFeeRewardEventHandler()
                .withReplayFeeEventHandler()
                .withSumFeeRewardEventHandler()
                .mergeEventsAndPassVerifySig();

        publisher.start();

        BlockIndex blockIndex = new BlockIndex();
        double sum = outer_transactions.parallelStream().filter(val -> !val.getType().equals(TransactionType.UNCLAIMED_FEE_REWARD)).mapToDouble(Transaction::getAmountWithTransactionFee).sum();
        try {
            outer_transactions.add(0, new UnclaimedFeeRewardTransaction(TransactionType.UNCLAIMED_FEE_REWARD, blockIndex.getAddressByPublicKey(CachedBLSKeyPair.getInstance().getPublicKey()), sum));
        } catch (NoSuchElementException e) {
        }

        TransactionBlock prevblock = new TransactionBlock();
        prevblock.setHeight(1);
        prevblock.setHash("hash");
        prevblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        Thread.sleep(1000);
        prevblock.setBlockProposer(vk1.toRaw());
        prevblock.setLeaderPublicKey(vk1);
        CachedLatestBlocks.getInstance().setTransactionBlock(prevblock);
        int count = 0;
        while (count < 100) {
            TransactionBlock transactionBlock = new TransactionBlock();
            MerkleTreeImp tree = new MerkleTreeImp();
            ArrayList<MerkleNode> merkleNodeArrayList = new ArrayList<>();
            transactionBlock.getHeaderData().setPreviousHash(CachedLatestBlocks.getInstance().getTransactionBlock().getHash());
            transactionBlock.getHeaderData().setVersion(AdrestusConfiguration.version);
            transactionBlock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
            transactionBlock.setStatustype(StatusType.PENDING);
            transactionBlock.setHeight(CachedLatestBlocks.getInstance().getTransactionBlock().getHeight() + 1);
            transactionBlock.setGeneration(CachedLatestBlocks.getInstance().getCommitteeBlock().getGeneration());
            transactionBlock.setViewID(CachedLatestBlocks.getInstance().getTransactionBlock().getViewID() + 1);
            transactionBlock.setZone(CachedZoneIndex.getInstance().getZoneIndex());
            transactionBlock.setBlockProposer(CachedBLSKeyPair.getInstance().getPublicKey().toRaw());
            transactionBlock.setLeaderPublicKey(CachedBLSKeyPair.getInstance().getPublicKey());
            transactionBlock.setTransactionList((List<Transaction>) outer_transactions.clone());


            transactionBlock.getTransactionList().forEach(transaction -> merkleNodeArrayList.add(new MerkleNode(transaction.getHash())));
            tree.my_generate2(merkleNodeArrayList);
            transactionBlock.setMerkleRoot(tree.getRootHash());

            //##########OutBound############
            Receipt.ReceiptBlock receiptBlock = new Receipt.ReceiptBlock(transactionBlock.getHeight(), transactionBlock.getGeneration(), transactionBlock.getMerkleRoot());
            ArrayList<Receipt> receiptList = new ArrayList<>();
            for (int i = 0; i < transactionBlock.getTransactionList().size(); i++) {
                Transaction transaction = transactionBlock.getTransactionList().get(i);
                if (transaction.getZoneFrom() != transaction.getZoneTo()) {
                    MerkleNode node = new MerkleNode(transaction.getHash());
                    tree.build_proofs2(merkleNodeArrayList, node);
                    receiptList.add(new Receipt(transaction.getZoneFrom(), transaction.getZoneTo(), receiptBlock, tree.getMerkleeproofs(), i));
                }
            }
            Map<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> outbound = receiptList
                    .stream()
                    .collect(Collectors.groupingBy(Receipt::getZoneTo, Collectors.groupingBy(Receipt::getReceiptBlock)));

            OutBoundRelay outBoundRelay = new OutBoundRelay(outbound);
            transactionBlock.setOutbound(outBoundRelay);

            ArrayList<Receipt> receiptList1 = new ArrayList<>();
            Map<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> inbound_map = receiptList1
                    .stream()
                    .collect(Collectors.groupingBy(Receipt::getZoneFrom, Collectors.groupingBy(Receipt::getReceiptBlock)));
            InboundRelay inboundRelay = new InboundRelay(inbound_map);
            transactionBlock.setInbound(inboundRelay);
            CachedInboundTransactionBlocks.getInstance().generate(inboundRelay.getMap_receipts(), transactionBlock.getGeneration());

            MemoryTreePool replica = new MemoryTreePool(((MemoryTreePool) TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex())));
            if (!transactionBlock.getTransactionList().isEmpty()) {
                TreePoolConstructBlock.getInstance().visitForgeTreePool(transactionBlock, replica);
            }

            transactionBlock.setPatriciaMerkleRoot(replica.getRootHash());

            BlockSizeCalculator blockSizeCalculator = new BlockSizeCalculator();
            blockSizeCalculator.setTransactionBlock(transactionBlock);
            int size = blockSizeCalculator.TransactionBlockSizeCalculator();
            byte[] tohash = serenc.encode(transactionBlock, size);
            transactionBlock.setHash(HashUtil.sha256_bytetoString(tohash));
            TransactionBlock transactionBlock3 = SerializationUtils.deserialize(SerializationUtils.serialize(transactionBlock));
            transactionBlock3.setHash("");
            byte[] tohash2 = serenc.encode(transactionBlock3, size);
            assertEquals(HashUtil.sha256_bytetoString(tohash2), HashUtil.sha256_bytetoString(tohash));
            transactionBlock3.getTransactionList().remove(0);
            assertNotEquals(transactionBlock3.getTransactionList().size(), transactionBlock.getTransactionList().size());
            publisher.publish(transactionBlock);
            publisher.getJobSyncUntilRemainingCapacityZero();
            count++;
        }
        publisher.close();


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

        sk7 = new BLSPrivateKey(7);
        vk7 = new BLSPublicKey(sk7);

        sk8 = new BLSPrivateKey(8);
        vk8 = new BLSPublicKey(sk8);

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

        ECDSASignatureData signatureData1 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress1)), ecKeyPair1);
        ECDSASignatureData signatureData2 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress2)), ecKeyPair2);
        ECDSASignatureData signatureData3 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress3)), ecKeyPair3);
        ECDSASignatureData signatureData4 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress4)), ecKeyPair4);
        ECDSASignatureData signatureData5 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress5)), ecKeyPair5);
        ECDSASignatureData signatureData = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress5)), ecKeyPair5);
        ECDSASignatureData signatureData6 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress6)), ecKeyPair6);
        ECDSASignatureData signatureData7 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress7)), ecKeyPair7);
        ECDSASignatureData signatureData8 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress8)), ecKeyPair8);
        ECDSASignatureData signatureData9 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress9)), ecKeyPair9);
        ECDSASignatureData signatureData10 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress10)), ecKeyPair10);

        TreeFactory.getMemoryTree(0).store(adddress1, new PatriciaTreeNode(1000, 0));
        TreeFactory.getMemoryTree(0).store(adddress2, new PatriciaTreeNode(1000, 0));
        TreeFactory.getMemoryTree(0).store(adddress3, new PatriciaTreeNode(1000, 0));
        TreeFactory.getMemoryTree(0).store(adddress4, new PatriciaTreeNode(1000, 0));
        TreeFactory.getMemoryTree(0).store(adddress5, new PatriciaTreeNode(1000, 0));
        TreeFactory.getMemoryTree(0).store(adddress6, new PatriciaTreeNode(1000, 0));
        TreeFactory.getMemoryTree(0).store(adddress7, new PatriciaTreeNode(1000, 0));
        TreeFactory.getMemoryTree(0).store(adddress8, new PatriciaTreeNode(1000, 0));
        TreeFactory.getMemoryTree(0).store(adddress9, new PatriciaTreeNode(1000, 0));
        TreeFactory.getMemoryTree(0).store(adddress10, new PatriciaTreeNode(1000, 0));

        BlockEventPublisher publisher = new BlockEventPublisher(1024);

        publisher
                .withHashHandler()
                .withHeaderEventHandler()
                .withTimestampEventHandler()
                .withDuplicateHandler()
                .withHeightEventHandler()
                .withRandomizedEventHandler()
                .withSortedStakingEventHandler()
                .withMinimumStakingEventHandler()
                .withVerifyDifficultyEventHandler()
                .withVerifyVDFEventHandler()
                .withVRFEventHandler()
                .withLeaderRandomnessEventHandler()
                .mergeEvents();


        CommitteeBlock prevblock = new CommitteeBlock();
        prevblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        Thread.sleep(100);
        prevblock.setHash("hash3");
        prevblock.setGeneration(0);
        prevblock.setHeight(0);
        CachedLatestBlocks.getInstance().setCommitteeBlock(prevblock);
        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.getStakingMap().put(new StakingData(1, 10.0), new KademliaData(new SecurityAuditProofs(vk1, adddress1, ecKeyPair1.getPublicKey(), signatureData1), new NettyConnectionInfo("192.168.1.101", KademliaConfiguration.PORT)));
        committeeBlock.getStakingMap().put(new StakingData(2, 13.0), new KademliaData(new SecurityAuditProofs(vk2, adddress2, ecKeyPair2.getPublicKey(), signatureData2), new NettyConnectionInfo("192.168.1.102", KademliaConfiguration.PORT)));
        committeeBlock.getStakingMap().put(new StakingData(3, 7.0), new KademliaData(new SecurityAuditProofs(vk3, adddress3, ecKeyPair3.getPublicKey(), signatureData3), new NettyConnectionInfo("192.168.1.103", KademliaConfiguration.PORT)));
        committeeBlock.getStakingMap().put(new StakingData(4, 22.0), new KademliaData(new SecurityAuditProofs(vk4, adddress4, ecKeyPair4.getPublicKey(), signatureData4), new NettyConnectionInfo("192.168.1.104", KademliaConfiguration.PORT)));
        committeeBlock.getStakingMap().put(new StakingData(5, 6.0), new KademliaData(new SecurityAuditProofs(vk5, adddress5, ecKeyPair5.getPublicKey(), signatureData5), new NettyConnectionInfo("192.168.1.105", KademliaConfiguration.PORT)));
        committeeBlock.getStakingMap().put(new StakingData(6, 32.0), new KademliaData(new SecurityAuditProofs(vk6, adddress6, ecKeyPair6.getPublicKey(), signatureData6), new NettyConnectionInfo("192.168.1.106", KademliaConfiguration.PORT)));
        committeeBlock.getStakingMap().put(new StakingData(7, 33.0), new KademliaData(new SecurityAuditProofs(vk7, adddress7, ecKeyPair7.getPublicKey(), signatureData7), new NettyConnectionInfo("192.168.1.107", KademliaConfiguration.PORT)));
        committeeBlock.getStakingMap().put(new StakingData(8, 31.0), new KademliaData(new SecurityAuditProofs(vk8, adddress8, ecKeyPair8.getPublicKey(), signatureData8), new NettyConnectionInfo("192.168.1.108", KademliaConfiguration.PORT)));

        committeeBlock.setCommitteeProposer(new int[committeeBlock.getStakingMap().size()]);
        committeeBlock.setGeneration(1);
        committeeBlock.getHeaderData().setPreviousHash("hash");
        committeeBlock.setHeight(1);
        //committeeBlock.setVRF();
        committeeBlock.getHeaderData().setPreviousHash("hash3");
        committeeBlock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());

        //########################################################################
        VdfEngine vdf = new VdfEnginePietrzak(2048);
        CachedSecurityHeaders.getInstance().getSecurityHeader().setPRnd(Hex.decode("c1f72aa5bd1e1d53c723b149259b63f759f40d5ab003b547d5c13d45db9a5da8"));
        committeeBlock.setVRF("c1f72aa5bd1e1d53c723b149259b63f759f40d5ab003b547d5c13d45db9a5da8");
        IDatabase<String, CommitteeBlock> database = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);
        CommitteeBlock firstblock = new CommitteeBlock();
        firstblock.setDifficulty(112);
        firstblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        database.save("1", firstblock);
        Thread.sleep(200);
        CommitteeBlock secondblock = new CommitteeBlock();
        secondblock.setDifficulty(117);
        secondblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        Thread.sleep(200);
        database.save("2", secondblock);
        CommitteeBlock thirdblock = new CommitteeBlock();
        thirdblock.setHash("hash3");
        thirdblock.setDifficulty(119);
        thirdblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        database.save("3", thirdblock);
        Thread.sleep(200);


        // ###################find difficulty##########################
        int finish = database.findDBsize();

        int n = finish;
        int summdiffuclty = 0;
        long sumtime = 0;
        Map<String, CommitteeBlock> block_entries = database.seekBetweenRange(0, finish);
        ArrayList<String> entries = new ArrayList<String>(block_entries.keySet());

        if (entries.size() == 1) {
            summdiffuclty = block_entries.get(entries.get(0)).getDifficulty();
            sumtime = 100;
        } else {
            for (int i = 0; i < entries.size(); i++) {
                if (i == entries.size() - 1)
                    break;

                long older = GetTime.GetTimestampFromString(block_entries.get(entries.get(i)).getHeaderData().getTimestamp()).getTime();
                long newer = GetTime.GetTimestampFromString(block_entries.get(entries.get(i + 1)).getHeaderData().getTimestamp()).getTime();
                sumtime = sumtime + (newer - older);
                //System.out.println("edw "+(newer - older));
                summdiffuclty = summdiffuclty + block_entries.get(entries.get(i)).getDifficulty();
                //  System.out.println("edw "+(newer - older));
            }
        }

        double d = ((double) summdiffuclty / n);
        // String s=String.format("%4d",  sumtime / n);
        double t = ((double) sumtime / n);
        //  System.out.println(t);
        int difficulty = MathOperationUtil.multiplication((int) Math.round((t) / d));
        if (difficulty < 100) {
            committeeBlock.setStatustype(StatusType.ABORT);
            throw new IllegalArgumentException("VDF difficulty is not set correct abort");
        }
        committeeBlock.setDifficulty(difficulty);
        CachedLatestBlocks.getInstance().getCommitteeBlock().setDifficulty(difficulty);
        committeeBlock.setVDF(Hex.toHexString(vdf.solve(Hex.decode(committeeBlock.getVRF()), committeeBlock.getDifficulty())));
        CachedSecurityHeaders.getInstance().getSecurityHeader().setRnd(Hex.decode(committeeBlock.getVDF()));
        // ###################find difficulty##########################

        SecureRandom secureRandom = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        SecureRandom secureRandom2 = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        SecureRandom secureRandom3 = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        secureRandom.setSeed(Hex.decode(committeeBlock.getVDF()));
        secureRandom2.setSeed(Hex.decode(committeeBlock.getVDF()));
        secureRandom3.setSeed(Hex.decode(committeeBlock.getVDF()));


        //####### LEADER RANDOM ASSIGN##############
        int iteration = 0;
        ArrayList<Integer> replica = new ArrayList<>();
        while (iteration < committeeBlock.getStakingMap().size()) {
            int nextInt = secureRandom2.nextInt(committeeBlock.getStakingMap().size());
            if (!replica.contains(nextInt)) {
                replica.add(nextInt);
                iteration++;
            }
        }
        committeeBlock.setCommitteeProposer(Ints.toArray(replica));
        //########################################################################


        //ITS WRONG ONLY radmones_assignment_test ITS CORRECT PLEASE IGNORE IT
        //##### RANDOM ASSIGN TO STRUCTRURE MAP ##############
        ArrayList<Integer> exclude = new ArrayList<Integer>();
        ArrayList<Integer> order = new ArrayList<Integer>();
        for (Map.Entry<StakingData, KademliaData> entry : committeeBlock.getStakingMap().entrySet()) {
            int nextInt = generateRandom(secureRandom, 0, committeeBlock.getStakingMap().size() - 1, exclude);
            if (!exclude.contains(nextInt)) {
                exclude.add(nextInt);
            }
            order.add(nextInt);
        }
        int zone_count = 0;
        List<Map.Entry<StakingData, KademliaData>> entryList = committeeBlock.getStakingMap().entrySet().stream().collect(Collectors.toList());
        int MAX_ZONE_SIZE = committeeBlock.getStakingMap().size() / 4;

        int j = 0;
        while (zone_count < 4) {
            int index_count = 0;
            if (committeeBlock.getStakingMap().size() % 4 != 0 && zone_count == 0) {
                while (index_count < committeeBlock.getStakingMap().size() - 3) {
                    committeeBlock
                            .getStructureMap()
                            .get(zone_count)
                            .put(entryList.get(order.get(j)).getValue().getAddressData().getValidatorBlSPublicKey(), entryList.get(order.get(j)).getValue().getNettyConnectionInfo().getHost());
                    index_count++;
                    j++;
                }
                zone_count++;
            }
            index_count = 0;
            while (index_count < MAX_ZONE_SIZE) {
                committeeBlock
                        .getStructureMap()
                        .get(zone_count)
                        .put(entryList.get(order.get(j)).getValue().getAddressData().getValidatorBlSPublicKey(), entryList.get(order.get(j)).getValue().getNettyConnectionInfo().getHost());
                index_count++;
                j++;
            }
            zone_count++;
        }

        //##### RANDOM ASSIGN TO STRUCTRURE MAP ##############


        Thread.sleep(100);
        sizeCalculator.setCommitteeBlock(committeeBlock);
        String hash = HashUtil.sha256_bytetoString(serenc.encode(committeeBlock, sizeCalculator.CommitteeBlockSizeCalculator()));
        committeeBlock.setHash(hash);


        publisher.start();
        Thread.sleep(100);
        publisher.publish(committeeBlock);


        publisher.getJobSyncUntilRemainingCapacityZero();
        publisher.close();
        database.delete_db();
    }


    //ITS WRONG ONLY radmones_assignment_test ITS CORRECT PLEASE IGNORE IT
    @Test
    public void radmones_test() {
        SecureRandom random = new SecureRandom();
        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.getStakingMap().put(new StakingData(1, 10.0), new KademliaData(new SecurityAuditProofs(vk1), new NettyConnectionInfo("192.168.1.101", KademliaConfiguration.PORT)));
        committeeBlock.getStakingMap().put(new StakingData(2, 13.0), new KademliaData(new SecurityAuditProofs(vk2), new NettyConnectionInfo("192.168.1.102", KademliaConfiguration.PORT)));
        committeeBlock.getStakingMap().put(new StakingData(3, 7.0), new KademliaData(new SecurityAuditProofs(vk3), new NettyConnectionInfo("192.168.1.103", KademliaConfiguration.PORT)));
        committeeBlock.getStakingMap().put(new StakingData(4, 22.0), new KademliaData(new SecurityAuditProofs(vk4), new NettyConnectionInfo("192.168.1.104", KademliaConfiguration.PORT)));
        committeeBlock.getStakingMap().put(new StakingData(5, 6.0), new KademliaData(new SecurityAuditProofs(vk5), new NettyConnectionInfo("192.168.1.105", KademliaConfiguration.PORT)));
        committeeBlock.getStakingMap().put(new StakingData(6, 32.0), new KademliaData(new SecurityAuditProofs(vk6), new NettyConnectionInfo("192.168.1.106", KademliaConfiguration.PORT)));
        committeeBlock.getStakingMap().put(new StakingData(7, 33.0), new KademliaData(new SecurityAuditProofs(vk7), new NettyConnectionInfo("192.168.1.107", KademliaConfiguration.PORT)));
        committeeBlock.getStakingMap().put(new StakingData(8, 31.0), new KademliaData(new SecurityAuditProofs(vk8), new NettyConnectionInfo("192.168.1.108", KademliaConfiguration.PORT)));

        ArrayList<Integer> exclude = new ArrayList<Integer>();
        ArrayList<Integer> order = new ArrayList<Integer>();
        for (Map.Entry<StakingData, KademliaData> entry : committeeBlock.getStakingMap().entrySet()) {
            int nextInt = generateRandom(random, 0, committeeBlock.getStakingMap().size() - 1, exclude);
            if (!exclude.contains(nextInt)) {
                exclude.add(nextInt);
            }
            order.add(nextInt);
        }
        int zone_count = 0;
        List<Map.Entry<StakingData, KademliaData>> entryList = committeeBlock.getStakingMap().entrySet().stream().collect(Collectors.toList());
        int MAX_ZONE_SIZE = committeeBlock.getStakingMap().size() / 4;

        if (MAX_ZONE_SIZE >= 2) {
            int j = 0;
            while (zone_count < 4) {
                int index_count = 0;
                if (committeeBlock.getStakingMap().size() % 4 != 0 && zone_count == 0) {
                    while (index_count < committeeBlock.getStakingMap().size() - 3) {
                        committeeBlock
                                .getStructureMap()
                                .get(zone_count)
                                .put(entryList.get(order.get(j)).getValue().getAddressData().getValidatorBlSPublicKey(), entryList.get(order.get(j)).getValue().getNettyConnectionInfo().getHost());
                        index_count++;
                        j++;
                    }
                    zone_count++;
                }
                index_count = 0;
                while (index_count < MAX_ZONE_SIZE) {
                    committeeBlock
                            .getStructureMap()
                            .get(zone_count)
                            .put(entryList.get(order.get(j)).getValue().getAddressData().getValidatorBlSPublicKey(), entryList.get(order.get(j)).getValue().getNettyConnectionInfo().getHost());
                    index_count++;
                    j++;
                }
                zone_count++;
            }
        } else {
            for (int i = 0; i < order.size(); i++) {
                committeeBlock
                        .getStructureMap()
                        .get(0)
                        .put(entryList.get(order.get(i)).getValue().getAddressData().getValidatorBlSPublicKey(), entryList.get(order.get(i)).getValue().getNettyConnectionInfo().getHost());
            }
        }
    }

   /* @Test
    public void radmones_test2() {
        SecureRandom random = new SecureRandom();
        CommitteeBlock committeeBlock = new CommitteeBlock();
        int finish = 9;
        for (int i = 0; i < finish; i++) {
            BLSPrivateKey sk = new BLSPrivateKey(i);
            BLSPublicKey vk = new BLSPublicKey(sk);
            committeeBlock.getStakingMap().put(new StakingData(i, i), new KademliaData(new SecurityAuditProofs(vk), new NettyConnectionInfo(String.valueOf(i), KademliaConfiguration.PORT)));
        }
        ArrayList<Integer> exclude = new ArrayList<Integer>();
        ArrayList<Integer> order = new ArrayList<Integer>();
        for (Map.Entry<StakingData, KademliaData> entry : committeeBlock.getStakingMap().entrySet()) {
            int nextInt = generateRandom(random, 0, committeeBlock.getStakingMap().size() - 1, exclude);
            if (!exclude.contains(nextInt)) {
                exclude.add(nextInt);
            }
            order.add(nextInt);
        }
        int zone_count = 0;
        List<Map.Entry<StakingData, KademliaData>> entryList = committeeBlock.getStakingMap().entrySet().stream().collect(Collectors.toList());
        int MAX_ZONE_SIZE = committeeBlock.getStakingMap().size() / 4;

        if (MAX_ZONE_SIZE >= 2) {
            int j = 0;
            while (zone_count < 4) {
                int index_count = 0;
                if (committeeBlock.getStakingMap().size() % 4 != 0 && zone_count == 0) {
                    while (index_count < committeeBlock.getStakingMap().size() - 3) {
                        committeeBlock
                                .getStructureMap()
                                .get(zone_count)
                                .put(entryList.get(order.get(j)).getValue().getAddressData().getValidatorBlSPublicKey(), entryList.get(order.get(j)).getValue().getNettyConnectionInfo().getHost());
                        index_count++;
                        j++;
                    }
                    zone_count++;
                }
                index_count = 0;
                while (index_count < MAX_ZONE_SIZE) {
                    committeeBlock
                            .getStructureMap()
                            .get(zone_count)
                            .put(entryList.get(order.get(j)).getValue().getAddressData().getValidatorBlSPublicKey(), entryList.get(order.get(j)).getValue().getNettyConnectionInfo().getHost());
                    index_count++;
                    j++;
                }
                zone_count++;
            }
        } else {
            for (int i = 0; i < order.size(); i++) {
                committeeBlock
                        .getStructureMap()
                        .get(0)
                        .put(entryList.get(order.get(i)).getValue().getAddressData().getValidatorBlSPublicKey(), entryList.get(order.get(i)).getValue().getNettyConnectionInfo().getHost());
            }
        }
        int g = 3;
    }*/

    @Test
    public void radmones_assignment_test() {
        SecureRandom random = new SecureRandom();
        CommitteeBlock committeeBlock = new CommitteeBlock();
        int finish = 8;
        for (int i = 0; i < finish; i++) {
            BLSPrivateKey sk = new BLSPrivateKey(i);
            BLSPublicKey vk = new BLSPublicKey(sk);
            committeeBlock.getStakingMap().put(new StakingData(i, i), new KademliaData(new SecurityAuditProofs(vk), new NettyConnectionInfo(String.valueOf(i), KademliaConfiguration.PORT)));
        }
        ArrayList<Integer> exclude = new ArrayList<Integer>();
        ArrayList<Integer> order = new ArrayList<Integer>();
        for (Map.Entry<StakingData, KademliaData> entry : committeeBlock.getStakingMap().entrySet()) {
            int nextInt = generateRandom(random, 0, committeeBlock.getStakingMap().size() - 1, exclude);
            if (!exclude.contains(nextInt)) {
                exclude.add(nextInt);
            }
            order.add(nextInt);
        }
        int zone_count = 0;
        List<Map.Entry<StakingData, KademliaData>> entryList = committeeBlock.getStakingMap().entrySet().stream().collect(Collectors.toList());
        int MAX_ZONE_SIZE = committeeBlock.getStakingMap().size() / 4;
        int j = 0;
        if (MAX_ZONE_SIZE >= 2) {
            int addition = committeeBlock.getStakingMap().size() - MathOperationUtil.closestNumber(committeeBlock.getStakingMap().size(), 4);
            while (zone_count < 4) {
                if (zone_count == 0 && addition != 0) {
                    while (j < MAX_ZONE_SIZE + addition) {
                        committeeBlock
                                .getStructureMap()
                                .get(zone_count)
                                .put(entryList.get(order.get(j)).getValue().getAddressData().getValidatorBlSPublicKey(), entryList.get(order.get(j)).getValue().getNettyConnectionInfo().getHost());
                        j++;
                    }
                } else {
                    int index_count = 0;
                    while (index_count < MAX_ZONE_SIZE) {
                        committeeBlock
                                .getStructureMap()
                                .get(zone_count)
                                .put(entryList.get(order.get(j)).getValue().getAddressData().getValidatorBlSPublicKey(), entryList.get(order.get(j)).getValue().getNettyConnectionInfo().getHost());
                        index_count++;
                        j++;
                    }
                }
                zone_count++;
            }
        } else {
            for (int i = 0; i < order.size(); i++) {
                committeeBlock
                        .getStructureMap()
                        .get(0)
                        .put(entryList.get(order.get(i)).getValue().getAddressData().getValidatorBlSPublicKey(), entryList.get(order.get(i)).getValue().getNettyConnectionInfo().getHost());
            }
        }
        int g = 3;
    }

    public int generateRandom(SecureRandom secureRandom, int start, int end, ArrayList<Integer> excludeRows) {
        int range = end - start + 1;
        int random = secureRandom.nextInt(range);
        while (excludeRows.contains(random)) {
            random = secureRandom.nextInt(range);
        }

        return random;
    }
}
