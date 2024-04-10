package io.Adrestus.core;

import com.google.common.reflect.TypeToken;
import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.MerkleNode;
import io.Adrestus.Trie.MerkleTreeImp;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.Resourses.MemoryReceiptPool;
import io.Adrestus.core.Resourses.MemoryTransactionPool;
import io.Adrestus.core.RingBuffer.handler.transactions.SignatureEventHandler;
import io.Adrestus.core.RingBuffer.publisher.BlockEventPublisher;
import io.Adrestus.core.RingBuffer.publisher.TransactionEventPublisher;
import io.Adrestus.core.Util.BlockSizeCalculator;
import io.Adrestus.crypto.HashUtil;
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
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import io.Adrestus.network.CachedEventLoop;
import io.Adrestus.network.IPFinder;
import io.Adrestus.rpc.RpcAdrestusServer;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import io.distributedLedger.*;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReceiptsTest {
    private static BLSPrivateKey sk1;
    private static BLSPublicKey vk1;
    private static BLSPrivateKey sk3;
    private static BLSPublicKey vk3;
    private static TransactionBlock transactionBlock;
    private static BLSPrivateKey sk2;
    private static BLSPublicKey vk2;
    private static BLSPrivateKey sk4;
    private static BLSPublicKey vk4;

    private static BLSPrivateKey sk5;
    private static BLSPublicKey vk5;

    private static BLSPrivateKey sk6;
    private static BLSPublicKey vk6;
    private static SerializationUtil<AbstractBlock> serenc;
    private static ArrayList<MerkleNode> merkleNodeArrayList;
    private static MerkleTreeImp tree;

    @BeforeAll
    public static void setup() throws Exception {
        CachedZoneIndex.getInstance().setZoneIndex(0);
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

        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        serenc = new SerializationUtil<AbstractBlock>(AbstractBlock.class, list);
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
                .withSameOriginEventHandler()
                .withDuplicateEventHandler()
                .mergeEventsAndPassThen(new SignatureEventHandler(SignatureEventHandler.SignatureBehaviorType.SIMPLE_TRANSACTIONS));
        publisher.start();

        SecureRandom random;
        String mnemonic_code = "fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2";
        random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(Hex.decode(mnemonic_code));

        ECDSASign ecdsaSign = new ECDSASign();

        List<SerializationUtil.Mapping> lists = new ArrayList<>();
        lists.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        SerializationUtil<Transaction> enc = new SerializationUtil<Transaction>(Transaction.class, lists);

        ArrayList<String> addreses = new ArrayList<>();
        ArrayList<ECKeyPair> keypair = new ArrayList<>();
        int version = 0x00;
        int size = 10;
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


        int j = 1;
        for (int i = 0; i < size - 1; i++) {
            Transaction transaction = new RegularTransaction();
            transaction.setFrom(addreses.get(i));
            transaction.setTo(addreses.get(i + 1));
            transaction.setStatus(StatusType.PENDING);
            transaction.setTimestamp(GetTime.GetTimeStampInString());
            transaction.setZoneFrom(0);
            transaction.setZoneTo(j);
            transaction.setAmount(i);
            transaction.setAmountWithTransactionFee(transaction.getAmount() * (10.0 / 100.0));
            transaction.setNonce(1);
            byte byf[] = enc.encode(transaction);
            transaction.setHash(HashUtil.sha256_bytetoString(byf));
            //  await().atMost(500, TimeUnit.MILLISECONDS);

            ECDSASignatureData signatureData = ecdsaSign.secp256SignMessage(Hex.decode(transaction.getHash()), keypair.get(i));
            transaction.setSignature(signatureData);
            //MemoryPool.getInstance().add(transaction);
            publisher.publish(transaction);
            //await().atMost(1000, TimeUnit.MILLISECONDS);
            if (j == 3)
                j = 0;
            j++;
        }
        publisher.getJobSyncUntilRemainingCapacityZero();
        publisher.close();


        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.getHeaderData().setTimestamp("2022-11-18 15:01:29.304");
        committeeBlock.getStructureMap().get(0).put(vk1, IPFinder.getLocal_address());
        committeeBlock.getStructureMap().get(0).put(vk2, "192.168.1.112");
        committeeBlock.getStructureMap().get(0).put(vk3, "192.168.1.114");
        committeeBlock.getStructureMap().get(1).put(vk4, "192.168.1.116");
        committeeBlock.getStructureMap().get(1).put(vk5, "192.168.1.117");
        committeeBlock.getStructureMap().get(1).put(vk6, "192.168.1.118");
        CachedLatestBlocks.getInstance().setCommitteeBlock(committeeBlock);
        CachedZoneIndex.getInstance().setZoneIndexInternalIP();
        tree = new MerkleTreeImp();
        transactionBlock = new TransactionBlock();
        transactionBlock.setGeneration(4);
        transactionBlock.setHeight(100);
        transactionBlock.setTransactionList(MemoryTransactionPool.getInstance().getAll());
        merkleNodeArrayList = new ArrayList<>();
        transactionBlock.getTransactionList().stream().forEach(x -> {
            merkleNodeArrayList.add(new MerkleNode(x.getHash()));
        });
        tree.my_generate2(merkleNodeArrayList);
        transactionBlock.setMerkleRoot(tree.getRootHash());
        byte[] tohash = serenc.encode(transactionBlock);
        transactionBlock.setHash(HashUtil.sha256_bytetoString(tohash));
    }

    @SneakyThrows
    @Test
    @Order(1)
    public void serialize_test() throws InterruptedException {
        //Thread.sleep(2000);
        TransactionBlock transactionBlock = new TransactionBlock();
        transactionBlock.setGeneration(4);
        transactionBlock.setHash("hash");


        Receipt.ReceiptBlock receiptBlock1 = new Receipt.ReceiptBlock( 1, 1, "1");
        Receipt.ReceiptBlock receiptBlock1a = new Receipt.ReceiptBlock( 2, 6, "1a");
        Receipt.ReceiptBlock receiptBlock2 = new Receipt.ReceiptBlock( 3, 2, "2");
        Receipt.ReceiptBlock receiptBlock3 = new Receipt.ReceiptBlock( 4, 3, "3");
        //its wrong each block must be unique for each zone need changes
        Receipt receipt1 = new Receipt(0, 1, receiptBlock1, null,1,"a");
        Receipt receipt2 = new Receipt(0, 1, receiptBlock1a, null,2,"b");
        Receipt receipt3 = new Receipt(2, 1, receiptBlock2, null,1,"c");
        Receipt receipt4 = new Receipt(2, 1, receiptBlock2, null,2,"d");
        Receipt receipt5 = new Receipt(3, 1, receiptBlock3, null,1,"e");
        Receipt receipt6 = new Receipt(3, 1, receiptBlock3, null,2,"f");

        ArrayList<Receipt> list = new ArrayList<>();
        list.add(receipt1);
        list.add(receipt2);
        list.add(receipt3);
        list.add(receipt4);
        list.add(receipt5);
        list.add(receipt6);
        Map<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> map = list
                .stream()
                .collect(Collectors.groupingBy(Receipt::getZoneFrom, Collectors.groupingBy(Receipt::getReceiptBlock)));

        OutBoundRelay outBoundRelay = new OutBoundRelay(map);
        transactionBlock.setOutbound(outBoundRelay);
//        transactionBlock.getOutbound().getMap_receipts().values().forEach(receiptBlock -> receiptBlock.keySet().forEach(vals -> vals.setBlock_hash(transactionBlock.getHash())));
        Integer[] size = transactionBlock.getOutbound().getMap_receipts().keySet().toArray(new Integer[0]);
//        for (int i=0;i<size.length;i++) {
//            List<String> ReceiptIPWorkers = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(size[i]).values().stream().collect(Collectors.toList());
//            List<byte[]> toSendReceipt = new ArrayList<>();
//            transactionBlock
//                    .getOutbound()
//                    .getMap_receipts()
//                    .get(size[i])
//                    .values()
//                    .forEach(receipts_list -> {
//                        receipts_list.forEach(
//                                receipt -> {
//                                    System.out.println(receipt.toString());
//                                });
//                    });
//        }
//     map.
//             entrySet().
//             forEach(receipt_map-> receipt_map.getValue().
//                               values().
//                               forEach(receipts_list->receipts_list.
//                                       stream().
//                                       forEach(receipt -> {
//                                           System.out.println(receipt.getTransaction());})));
        for (int i = 0; i < 1000; i++) {
            byte[] buffer = serenc.encode(transactionBlock);
            TransactionBlock clone2 = (TransactionBlock) transactionBlock.clone();
            TransactionBlock clone = (TransactionBlock) serenc.decode(buffer);
            Map.Entry<Receipt.ReceiptBlock, List<Receipt>> entry = clone.getOutbound().getMap_receipts().get(0).entrySet().iterator().next();
            assertEquals(transactionBlock, clone2);
            assertEquals(transactionBlock, clone);
//            clone2.getOutbound().getMap_receipts().values().forEach(receiptBlock -> receiptBlock.keySet().forEach(vals -> vals.setBlock_hash("random")));
//            assertNotEquals(clone2, transactionBlock);
        }

    }

    @Test
    @Order(2)
    public void outbound_test() throws Exception {
        CachedZoneIndex.getInstance().setZoneIndexInternalIP();
        //Thread.sleep(2000);
        BlockEventPublisher publisher = new BlockEventPublisher(1024);

        String OriginalRootHash = transactionBlock.getMerkleRoot();
        Receipt.ReceiptBlock receiptBlock = new Receipt.ReceiptBlock(transactionBlock.getHeight(), transactionBlock.getGeneration(), transactionBlock.getMerkleRoot());
        ArrayList<Receipt> receiptList = new ArrayList<>();
        for (int i = 0; i < transactionBlock.getTransactionList().size(); i++) {
            Transaction transaction = transactionBlock.getTransactionList().get(i);
            MerkleNode node = new MerkleNode(transaction.getHash());
            tree.build_proofs2(merkleNodeArrayList, node);
            receiptList.add(new Receipt(transaction.getZoneFrom(), transaction.getZoneTo(), receiptBlock,  i, tree.getMerkleeproofs(),transaction.getHash()));
        }

        Map<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> map = receiptList
                .stream()
                .collect(Collectors.groupingBy(Receipt::getZoneTo, Collectors.groupingBy(Receipt::getReceiptBlock, Collectors.mapping(Receipt::merge, Collectors.toList()))));

        for (Integer key : map.keySet()) {
            map.get(key).entrySet().stream().forEach(val -> {
                val.getValue().stream().forEach(x -> assertEquals(OriginalRootHash, tree.GenerateRoot(x.getProofs())));
            });
        }

        OutBoundRelay outBoundRelay = new OutBoundRelay(map);
        transactionBlock.setOutbound(outBoundRelay);

        byte[] buffer = serenc.encode(transactionBlock);
        TransactionBlock clone = (TransactionBlock) serenc.decode(buffer);
        assertEquals(transactionBlock, clone);


        publisher.withOutBoundEventHandler().mergeEvents();
        publisher.start();
        publisher.publish(transactionBlock);

        publisher.getJobSyncUntilRemainingCapacityZero();
        publisher.close();
    }

    @Test
    //@Order(2)
    public void outbound_test2() throws Exception {
        BlockSizeCalculator blockSizeCalculator = new BlockSizeCalculator();
        BlockEventPublisher publisher = new BlockEventPublisher(1024);
        TransactionBlock transactionBlock = new TransactionBlock();
        transactionBlock.setHash("");
        transactionBlock.setGeneration(8);
        transactionBlock.setZone(2);
        transactionBlock.setHeight(15);
        transactionBlock.setMerkleRoot("1");

        Receipt.ReceiptBlock receiptBlock1 = new Receipt.ReceiptBlock(transactionBlock.getHeight(), transactionBlock.getGeneration(), transactionBlock.getMerkleRoot());
        //its wrong each block must be unique for each zone need changes
        Receipt receipt1 = new Receipt(0, 1, receiptBlock1,null,1,new RegularTransaction("a").getHash());
        Receipt receipt2 = new Receipt(0, 1, receiptBlock1,null,2,new RegularTransaction("b").getHash());
        blockSizeCalculator.setTransactionBlock(transactionBlock);

        ArrayList<Receipt> list = new ArrayList<>();
        list.add(receipt1);
        list.add(receipt2);

        Map<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> map = list
                .stream()
                .collect(Collectors.groupingBy(Receipt::getZoneTo, Collectors.groupingBy(Receipt::getReceiptBlock)));

        OutBoundRelay outBoundRelay = new OutBoundRelay(map);
        transactionBlock.setOutbound(outBoundRelay);

        blockSizeCalculator.setTransactionBlock(transactionBlock);
        byte[] tohash = serenc.encode(transactionBlock, blockSizeCalculator.TransactionBlockSizeCalculator());
        transactionBlock.setHash(HashUtil.sha256_bytetoString(tohash));
//        transactionBlock.getOutbound().getMap_receipts().values().forEach(receiptBlock -> receiptBlock.keySet().forEach(vals -> vals.setBlock_hash(transactionBlock.getHash())));
        TransactionBlock clone2 = (TransactionBlock) transactionBlock.clone();
        TransactionBlock clone3 = (TransactionBlock) transactionBlock.clone();
        publisher.withHashHandler().mergeEvents();
        publisher.start();
        publisher.publish(transactionBlock);
        byte[] buffer = serenc.encode(transactionBlock);
        TransactionBlock clonem = (TransactionBlock) serenc.decode(buffer);
        assertEquals(clonem, transactionBlock);
        publisher.publish(clonem);
        publisher.getJobSyncUntilRemainingCapacityZero();
        publisher.close();
        assertEquals(clone2, transactionBlock);
        assertEquals(clone3, transactionBlock);
//        clone3.getOutbound().getMap_receipts().values().forEach(receiptBlock -> receiptBlock.keySet().forEach(vals -> vals.setBlock_hash("random")));
//        assertNotEquals(clone3, transactionBlock);
        assertEquals(transactionBlock.getHash(), HashUtil.sha256_bytetoString(tohash));
//        assertEquals(transactionBlock.getOutbound().getMap_receipts().get(1).keySet().stream().findFirst().get().getBlock_hash(), HashUtil.sha256_bytetoString(tohash));
//        assertEquals(transactionBlock.getOutbound().getMap_receipts().get(1).values().stream().findFirst().get().stream().findFirst().get().getReceiptBlock().getBlock_hash(), HashUtil.sha256_bytetoString(tohash));
    }

    @Test
    @Order(3)
    public void inbound_test() throws Exception {
        IDatabase<String, TransactionBlock> database = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.ZONE_0_TRANSACTION_BLOCK);
        IDatabase<String, LevelDBTransactionWrapper<Transaction>> transaction_database = new DatabaseFactory(String.class, Transaction.class, new TypeToken<LevelDBTransactionWrapper<Transaction>>() {
        }.getType()).getDatabase(DatabaseType.LEVEL_DB);
        IDatabase<String, LevelDBReceiptWrapper<Receipt>> receipt_database = new DatabaseFactory(String.class, Receipt.class, new TypeToken<LevelDBReceiptWrapper<Receipt>>() {
        }.getType()).getDatabase(DatabaseType.LEVEL_DB);
        database.save(String.valueOf(transactionBlock.getHeight()), transactionBlock);
        //  CachedEventLoop.getInstance().setEventloop(Eventloop.create().withCurrentThread());
        // new Thread(CachedEventLoop.getInstance().getEventloop());
        RpcAdrestusServer<AbstractBlock> example = new RpcAdrestusServer<AbstractBlock>(new TransactionBlock(), DatabaseInstance.ZONE_0_TRANSACTION_BLOCK, IPFinder.getLocal_address(), ZoneDatabaseFactory.getDatabaseRPCPort(CachedZoneIndex.getInstance().getZoneIndex()), CachedEventLoop.getInstance().getEventloop());
        new Thread(example).start();
        CachedEventLoop.getInstance().start();

        BlockEventPublisher publisher = new BlockEventPublisher(1024);
        CachedZoneIndex.getInstance().setZoneIndex(1);
        //Give the correct one closer to commite block zone validators check validators ip closest to vk4
        CachedBLSKeyPair.getInstance().setPublicKey(vk4);
        CachedBLSKeyPair.getInstance().setPrivateKey(sk4);
        String OriginalRootHash = transactionBlock.getMerkleRoot();
        Receipt.ReceiptBlock receiptBlock = new Receipt.ReceiptBlock(transactionBlock.getHeight(), transactionBlock.getGeneration(), transactionBlock.getMerkleRoot());


        for (int i = 0; i < transactionBlock.getTransactionList().size(); i++) {
            Transaction transaction = transactionBlock.getTransactionList().get(i);
            transaction_database.save(transaction.getFrom(), transaction);
            int index = Collections.binarySearch(transactionBlock.getTransactionList(), transactionBlock.getTransactionList().get(i));
            MerkleNode node = new MerkleNode(transaction.getHash());
            tree.build_proofs2(merkleNodeArrayList, node);
            if (CachedZoneIndex.getInstance().getZoneIndex() == transaction.getZoneTo())
                MemoryReceiptPool.getInstance().add(new Receipt(transaction.getZoneFrom(), transaction.getZoneTo(), receiptBlock, tree.getMerkleeproofs(), index,transaction.getHash()));
        }

        Map<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> map = ((ArrayList<Receipt>) MemoryReceiptPool.getInstance().getAll())
                .stream()
                .collect(Collectors.groupingBy(Receipt::getZoneFrom, Collectors.groupingBy(Receipt::getReceiptBlock)));
        // .collect(Collectors.groupingBy(Receipt::getZoneFrom, Collectors.groupingBy(Receipt::getReceiptBlock, Collectors.mapping(Receipt::merge, Collectors.toList()))));


        for (Integer key : map.keySet()) {
            map.get(key).entrySet().stream().forEach(val -> {
                val.getValue().stream().forEach(x -> assertEquals(OriginalRootHash, tree.GenerateRoot(x.getProofs())));
            });
        }

        InboundRelay inboundRelay = new InboundRelay(map);
        transactionBlock.setInbound(inboundRelay);

        transactionBlock
                .getInbound()
                .getMap_receipts()
                .get(transactionBlock.getInbound().getMap_receipts().keySet().toArray()[0])
                .entrySet()
                .stream()
                .forEach(entry -> {
                    entry.getValue().stream().forEach(receipt -> {
                        Transaction trx=transactionBlock.getTransactionList().get(receipt.getPosition());
                        receipt_database.save(trx.getFrom(), receipt);
                        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).deposit(trx.getFrom(), trx.getAmount(), TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()));
                        MemoryReceiptPool.getInstance().delete(receipt);
                    });
                });
        byte[] buffer = serenc.encode(transactionBlock);
        TransactionBlock clone = (TransactionBlock) serenc.decode(buffer);
        assertEquals(transactionBlock, clone);

        publisher.withInBoundEventHandler().mergeEvents();
        publisher.start();
        publisher.publish(transactionBlock);

        publisher.getJobSyncUntilRemainingCapacityZero();
        publisher.close();

        database.delete_db();
        receipt_database.delete_db();
        transaction_database.delete_db();
    }
}
