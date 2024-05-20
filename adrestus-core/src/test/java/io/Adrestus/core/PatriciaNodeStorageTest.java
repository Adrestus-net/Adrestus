package io.Adrestus.core;

import com.google.common.collect.Iterables;
import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.Trie.PatriciaTreeTransactionType;
import io.Adrestus.Trie.StorageInfo;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.RingBuffer.publisher.ReceiptEventPublisher;
import io.Adrestus.core.RingBuffer.publisher.TransactionEventPublisher;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomSerializerTreeMap;
import io.Adrestus.util.SerializationUtil;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import io.distributedLedger.ZoneDatabaseFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PatriciaNodeStorageTest {
    private static SerializationUtil<Receipt> recep;

    @BeforeAll
    public static void setup() {
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        recep = new SerializationUtil<Receipt>(Receipt.class, list);

    }

    @Test
    public void PublisherReceiptTest() throws InterruptedException {
        CachedZoneIndex.getInstance().setZoneIndex(0);
        ReceiptEventPublisher publisher = new ReceiptEventPublisher(1024);
        publisher.withReplayEventHandler().mergeEvents();
        publisher.start();


        IDatabase<String, TransactionBlock> transactionBlockIDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));

        TransactionBlock transactionBlock1 = new TransactionBlock();
        transactionBlock1.setHeight(3);
        transactionBlock1.setHash("hash1");

        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store("From1a", new PatriciaTreeNode(0, 0));

        Receipt.ReceiptBlock receiptBlock1 = new Receipt.ReceiptBlock(1, 1, "1");
        Receipt receipt1 = new Receipt(0, 1, receiptBlock1, null, 1);

        ArrayList<Receipt> list1 = new ArrayList<>();
        list1.add(receipt1);
        Map<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> map = list1
                .stream()
                .collect(Collectors.groupingBy(Receipt::getZoneFrom, Collectors.groupingBy(Receipt::getReceiptBlock)));

        InboundRelay inboundRelay = new InboundRelay(map);
        transactionBlock1.setInbound(inboundRelay);

        transactionBlock1
                .getInbound()
                .getMap_receipts()
                .forEach((key, value) -> value
                        .entrySet()
                        .stream()
                        .forEach(entry -> {
                            entry.getValue().stream().forEach(receipt -> {
                                int position = Iterables.indexOf(entry.getValue(), u -> u.equals(receipt));
                                String hash = HashUtil.sha256_bytetoString(recep.encode(receipt));
                                TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress("From1a").get().addReceiptPosition(hash, CachedZoneIndex.getInstance().getZoneIndex(), transactionBlock1.getHeight(), receipt.getZoneFrom(), receipt.getReceiptBlock().getHeight(), position);
                            });
                        }));
        transactionBlockIDatabase.save(String.valueOf(transactionBlock1.getHeight()), transactionBlock1);


        ReceiptBlock receiptBlock = new ReceiptBlock();
        Transaction a = new RegularTransaction();
        a.setFrom("From1a");
        receiptBlock.setReceipt(receipt1);
        receiptBlock.setTransaction(a);
        publisher.publish(receiptBlock);
        publisher.getJobSyncUntilRemainingCapacityZero();
        publisher.close();

    }

    @Test
    public void PublisherTransactionTest() throws InterruptedException {
        CachedZoneIndex.getInstance().setZoneIndex(0);
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store("from1", new PatriciaTreeNode(0, 0));
        TreeFactory.getMemoryTree(1).store("from1", new PatriciaTreeNode(0, 0));
        IDatabase<String, TransactionBlock> transactionBlockIDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
        IDatabase<String, TransactionBlock> transactionBlockIDatabase1 = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(1));
        TransactionEventPublisher publisher = new TransactionEventPublisher(1024);
        TransactionEventPublisher publisher2 = new TransactionEventPublisher(1024);
        publisher.withDuplicateEventHandler().withZoneEventHandler().mergeEvents();
        publisher2.withTimestampEventHandler().mergeEvents();
        publisher.start();
        publisher2.start();

        TransactionBlock transactionBlock1 = new TransactionBlock();
        transactionBlock1.setHeight(1);
        transactionBlock1.setHash("hash1");
        TransactionBlock transactionBlock2 = new TransactionBlock();
        transactionBlock2.setHeight(2);
        transactionBlock2.setHash("hash2");
        TransactionBlock transactionBlock3 = new TransactionBlock();
        transactionBlock3.setHeight(3);
        transactionBlock3.setHash("hash3");
        TransactionBlock transactionBlock4 = new TransactionBlock();
        transactionBlock4.setHeight(4);
        transactionBlock4.setHash("hash4");
        ArrayList<Transaction> list = new ArrayList<>();
        ArrayList<Transaction> list2 = new ArrayList<>();
        ArrayList<Transaction> list3 = new ArrayList<>();
        ArrayList<Transaction> list4 = new ArrayList<>();
        Transaction transaction = new RegularTransaction();
        transaction.setHash("hash1");
        transaction.setFrom("from1");
        Thread.sleep(500);
        transaction.setTimestamp(new Timestamp(System.currentTimeMillis()).toString());
        RewardsTransaction reward = new RewardsTransaction();
        reward.setHash("hashREW");
        reward.setType(TransactionType.REWARDS);
        reward.setRecipientAddress("from1");
        Thread.sleep(500);
        reward.setTimestamp(new Timestamp(System.currentTimeMillis()).toString());
        Transaction transaction2 = new RegularTransaction();
        transaction2.setHash("hash2");
        transaction2.setFrom("from1");
        transaction2.setZoneFrom(1);
        Thread.sleep(500);
        transaction2.setTimestamp(new Timestamp(System.currentTimeMillis()).toString());
        Transaction transaction3 = new RegularTransaction();
        transaction3.setHash("hash3");
        transaction3.setFrom("from1");
        transaction3.setZoneFrom(1);
        Thread.sleep(500);
        transaction3.setTimestamp(new Timestamp(System.currentTimeMillis()).toString());
        Transaction transaction3a = new RegularTransaction();
        transaction3a.setHash("hash3a");
        transaction3a.setFrom("from1");
        transaction3a.setZoneFrom(1);
        Thread.sleep(12000);
        transaction3a.setTimestamp(new Timestamp(System.currentTimeMillis()).toString());
        Transaction transaction4 = new RegularTransaction();
        transaction4.setHash("hash3");
        transaction4.setFrom("from1");
        transaction4.setZoneFrom(1);
        Thread.sleep(500);
        transaction4.setTimestamp(new Timestamp(System.currentTimeMillis()).toString());
        list.add(transaction);
        list.add(reward);
        list2.add(transaction2);
        list3.add(transaction3);
        list3.add(transaction3a);
        list4.add(transaction4);
        transactionBlock1.setTransactionList(list);
        transactionBlock2.setTransactionList(list2);
        transactionBlock3.setTransactionList(list3);
        transactionBlock4.setTransactionList(list4);
        transactionBlockIDatabase.save(String.valueOf(transactionBlock1.getHeight()), transactionBlock1);
        transactionBlockIDatabase1.save(String.valueOf(transactionBlock2.getHeight()), transactionBlock2);
        transactionBlockIDatabase1.save(String.valueOf(transactionBlock3.getHeight()), transactionBlock3);
        int position = Iterables.indexOf(transactionBlock1.getTransactionList(), u -> u.equals(transaction));
        int positionre = Iterables.indexOf(transactionBlock1.getTransactionList(), u -> u.equals(reward));
        int position2 = Iterables.indexOf(transactionBlock2.getTransactionList(), u -> u.equals(transaction2));
        int position3 = Iterables.indexOf(transactionBlock3.getTransactionList(), u -> u.equals(transaction3));
        int position3a = Iterables.indexOf(transactionBlock3.getTransactionList(), u -> u.equals(transaction3a));
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress("from1").get().addTransactionPosition(PatriciaTreeTransactionType.REGULAR, transaction.getHash(), CachedZoneIndex.getInstance().getZoneIndex(), transactionBlock1.getHeight(), position);
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress("from1").get().addTransactionPosition(PatriciaTreeTransactionType.REWARDS, reward.getHash(), CachedZoneIndex.getInstance().getZoneIndex(), transactionBlock1.getHeight(), positionre);
        TreeFactory.getMemoryTree(1).getByaddress("from1").get().addTransactionPosition(PatriciaTreeTransactionType.REWARDS, reward.getHash(), 1, transactionBlock1.getHeight(), positionre);
        TreeFactory.getMemoryTree(1).getByaddress("from1").get().addTransactionPosition(PatriciaTreeTransactionType.REGULAR, transaction2.getHash(), 1, transactionBlock2.getHeight(), position2);
        TreeFactory.getMemoryTree(1).getByaddress("from1").get().addTransactionPosition(PatriciaTreeTransactionType.REGULAR, transaction3.getHash(), 1, transactionBlock3.getHeight(), position3);
        TreeFactory.getMemoryTree(1).getByaddress("from1").get().addTransactionPosition(PatriciaTreeTransactionType.REGULAR, transaction3a.getHash(), 1, transactionBlock3.getHeight(), position3a);

        //Check manually with debug if pass DuplicateEventHandler
        publisher.publish(transaction);
        publisher.publish(reward);
        publisher.publish(transaction2);
        publisher.getJobSyncUntilRemainingCapacityZero();
        CachedZoneIndex.getInstance().setZoneIndex(1);
        publisher2.publish(transaction4);
        publisher2.getJobSyncUntilRemainingCapacityZero();
        publisher2.close();
        publisher.close();
    }

    @Test
    public void TransactionMaxTest() {
        TreeFactory.getMemoryTree(0).store("From1a", new PatriciaTreeNode(0, 0));
        StorageInfo s1 = new StorageInfo(0, 1, 0);
        StorageInfo s2 = new StorageInfo(0, 2, 1);
        StorageInfo s3 = new StorageInfo(0, 3, 2);
        StorageInfo s4 = new StorageInfo(0, 4, 3);
        StorageInfo s5 = new StorageInfo(0, 5, 4);
        StorageInfo s6 = new StorageInfo(0, 5, 5);
        StorageInfo s7 = new StorageInfo(1, 5, 6);
        TreeFactory.getMemoryTree(0).getByaddress("From1a").get().addTransactionPosition(PatriciaTreeTransactionType.REGULAR, "1", s1.getOrigin_zone(), s1.getBlockHeight(), s1.getPosition());
        TreeFactory.getMemoryTree(0).getByaddress("From1a").get().addTransactionPosition(PatriciaTreeTransactionType.REGULAR, "2", s2.getOrigin_zone(), s2.getBlockHeight(), s2.getPosition());
        TreeFactory.getMemoryTree(0).getByaddress("From1a").get().addTransactionPosition(PatriciaTreeTransactionType.REGULAR, "3", s3.getOrigin_zone(), s3.getBlockHeight(), s3.getPosition());
        TreeFactory.getMemoryTree(0).getByaddress("From1a").get().addTransactionPosition(PatriciaTreeTransactionType.REGULAR, "4", s4.getOrigin_zone(), s4.getBlockHeight(), s4.getPosition());
        TreeFactory.getMemoryTree(0).getByaddress("From1a").get().addTransactionPosition(PatriciaTreeTransactionType.REGULAR, "5", s5.getOrigin_zone(), s5.getBlockHeight(), s5.getPosition());
        TreeFactory.getMemoryTree(0).getByaddress("From1a").get().addTransactionPosition(PatriciaTreeTransactionType.REGULAR, "6", s6.getOrigin_zone(), s6.getBlockHeight(), s6.getPosition());
        TreeFactory.getMemoryTree(0).getByaddress("From1a").get().addTransactionPosition(PatriciaTreeTransactionType.REGULAR, "7", s7.getOrigin_zone(), s7.getBlockHeight(), s7.getPosition());
        StorageInfo res = TreeFactory.getMemoryTree(0).getByaddress("From1a").get().findLatestStorageInfo(PatriciaTreeTransactionType.REGULAR, 0).get();
        HashMap<Integer, HashSet<Integer>> map = TreeFactory.getMemoryTree(0).getByaddress("From1a").get().retrieveAllTransactionsByOriginZone(PatriciaTreeTransactionType.REGULAR, 0);
        assertEquals(2, res.getPositions().size());
        assertEquals(5, map.size());
        assertEquals(2, map.get(5).size());
    }

    @Test
    public void TransactionTest() {
        CachedZoneIndex.getInstance().setZoneIndex(0);
        IDatabase<String, TransactionBlock> transactionBlockIDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        SerializationUtil<AbstractBlock> serenc = new SerializationUtil<AbstractBlock>(AbstractBlock.class, list);

        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store("From1a", new PatriciaTreeNode(0, 0));
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store("From2a", new PatriciaTreeNode(0, 0));
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store("From3a", new PatriciaTreeNode(0, 0));

        TransactionBlock transactionBlock1 = new TransactionBlock();
        transactionBlock1.setHeight(1);
        transactionBlock1.setHash("hash1");
        ArrayList<Transaction> list1 = new ArrayList<>();
        Transaction trx1 = new RegularTransaction();
        trx1.setHash("1");
        trx1.setFrom("From1a");
        Transaction trx2 = new RegularTransaction();
        trx2.setHash("2");
        trx2.setFrom("From2a");
        Transaction trx3 = new RegularTransaction();
        trx3.setHash("3");
        trx3.setFrom("From3a");

        list1.add(trx1);
        list1.add(trx2);
        list1.add(trx3);
        transactionBlock1.setTransactionList(list1);
        transactionBlock1.getTransactionList().stream().forEach(trx -> {
            int position = Iterables.indexOf(transactionBlock1.getTransactionList(), u -> u.equals(trx));
            //1-->origin_zone
            //1-->height
            //2-->position
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(trx.getFrom()).get().addTransactionPosition(PatriciaTreeTransactionType.REGULAR, trx.getHash(), trx.getZoneFrom(), transactionBlock1.getHeight(), position);
        });

        transactionBlockIDatabase.save(String.valueOf(transactionBlock1.getHeight()), transactionBlock1);

        TransactionBlock transactionBlock2 = new TransactionBlock();
        transactionBlock2.setHeight(2);
        transactionBlock2.setHash("hash2");
        ArrayList<Transaction> list2 = new ArrayList<>();
        Transaction trx1a = new RegularTransaction();
        trx1a.setHash("1a");
        trx1a.setFrom("From1a");
        Transaction trx2a = new RegularTransaction();
        trx2a.setHash("2a");
        trx2a.setFrom("From2a");
        Transaction trx3a = new RegularTransaction();
        trx3a.setHash("3a");
        trx3a.setFrom("From3a");
        list2.add(trx1a);
        list2.add(trx2a);
        list2.add(trx3a);
        transactionBlock2.setTransactionList(list2);
        transactionBlock2.getTransactionList().stream().forEach(trx -> {
            int position = Iterables.indexOf(transactionBlock2.getTransactionList(), u -> u.equals(trx));
            //1-->origin_zone
            //1-->height
            //2-->position
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(trx.getFrom()).get().addTransactionPosition(PatriciaTreeTransactionType.REGULAR, trx.getHash(), trx.getZoneFrom(), transactionBlock2.getHeight(), position);
        });
        transactionBlockIDatabase.save(String.valueOf(transactionBlock2.getHeight()), transactionBlock2);

        TransactionBlock transactionBlock3 = new TransactionBlock();
        transactionBlock3.setHeight(3);
        transactionBlock3.setHash("hash3");
        ArrayList<Transaction> list3 = new ArrayList<>();
        Transaction trx1c = new RegularTransaction();
        trx1c.setHash("1c");
        trx1c.setFrom("From1a");
        Transaction trx2c = new RegularTransaction();
        trx2c.setHash("2c");
        trx2c.setFrom("From2a");
        Transaction trx3c = new RegularTransaction();
        trx3c.setHash("3c");
        trx3c.setFrom("From2a");
        list3.add(trx1c);
        list3.add(trx2c);
        list3.add(trx3c);
        transactionBlock3.setTransactionList(list3);
        transactionBlock3.getTransactionList().stream().forEach(trx -> {
            int position = Iterables.indexOf(transactionBlock3.getTransactionList(), u -> u.equals(trx));
            //1-->origin_zone
            //1-->height
            //2-->position
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(trx.getFrom()).get().addTransactionPosition(PatriciaTreeTransactionType.REGULAR, trx.getHash(), trx.getZoneFrom(), transactionBlock3.getHeight(), position);
        });
        transactionBlockIDatabase.save(String.valueOf(transactionBlock3.getHeight()), transactionBlock3);

        TransactionBlock transactionBlock4 = new TransactionBlock();
        transactionBlock4.setHeight(4);
        transactionBlock4.setHash("hash4");
        ArrayList<Transaction> list4 = new ArrayList<>();
        Transaction trx1d = new RegularTransaction();
        trx1d.setHash("1d");
        trx1d.setFrom("From1a");
        Transaction trx2d = new RegularTransaction();
        trx2d.setHash("2d");
        trx2d.setFrom("From1a");
        Transaction trx3d = new RegularTransaction();
        trx3d.setHash("3d");
        trx3d.setFrom("From1a");
        list4.add(trx1d);
        list4.add(trx2d);
        list4.add(trx3d);
        transactionBlock4.setTransactionList(list4);
        transactionBlock4.getTransactionList().stream().forEach(trx -> {
            int position = Iterables.indexOf(transactionBlock4.getTransactionList(), u -> u.equals(trx));
            //1-->origin_zone
            //1-->height
            //2-->position
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(trx.getFrom()).get().addTransactionPosition(PatriciaTreeTransactionType.REGULAR, trx.getHash(), trx.getZoneFrom(), transactionBlock4.getHeight(), position);
        });

        transactionBlockIDatabase.save(String.valueOf(transactionBlock4.getHeight()), transactionBlock4);


        TransactionBlock transactionBlock5 = new TransactionBlock();
        transactionBlock5.setHeight(5);
        transactionBlock5.setHash("hash5");
        ArrayList<Transaction> list5 = new ArrayList<>();
        Transaction trx5d = new RegularTransaction();
        trx5d.setHash("5d");
        trx5d.setFrom("From1a");
        trx5d.setZoneFrom(0);
        Transaction trx5da = new RegularTransaction();
        trx5da.setHash("2d");
        trx5da.setFrom("From1a");
        trx5da.setZoneFrom(1);
        Transaction trx5dc = new RegularTransaction();
        trx5dc.setHash("3d");
        trx5dc.setFrom("From1a");
        trx5dc.setZoneFrom(1);
        list5.add(trx5d);
        list5.add(trx5da);
        list5.add(trx5dc);
        transactionBlock5.setTransactionList(list5);
        transactionBlock5.getTransactionList().stream().forEach(trx -> {
            int position = Iterables.indexOf(transactionBlock5.getTransactionList(), u -> u.equals(trx));
            //1-->origin_zone
            //1-->height
            //2-->position
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(trx.getFrom()).get().addTransactionPosition(PatriciaTreeTransactionType.REGULAR, trx.getHash(), trx.getZoneFrom(), transactionBlock5.getHeight(), position);
        });

        transactionBlockIDatabase.save(String.valueOf(transactionBlock5.getHeight()), transactionBlock4);

        PatriciaTreeNode pat = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress("From1a").get();
        assertEquals(2, pat.getTransactionCapacities(PatriciaTreeTransactionType.REGULAR).size());
        assertEquals(5, pat.getTransactionCapacities(PatriciaTreeTransactionType.REGULAR).get(CachedZoneIndex.getInstance().getZoneIndex()).getPositions().size());
        assertEquals(1, pat.getTransactionCapacities(PatriciaTreeTransactionType.REGULAR).get(CachedZoneIndex.getInstance().getZoneIndex()).getPositions().get(1).size());
        assertEquals(3, pat.getTransactionCapacities(PatriciaTreeTransactionType.REGULAR).get(CachedZoneIndex.getInstance().getZoneIndex()).getPositions().get(4).size());
        assertEquals(2, pat.getTransactionCapacities(PatriciaTreeTransactionType.REGULAR).get(1).getPositions().get(5).size());
        String hash1 = "1d";
        String hash2 = "1c";
        List<StorageInfo> s1 = pat.retrieveTransactionInfoByHash(PatriciaTreeTransactionType.REGULAR, hash1);
        List<StorageInfo> s2 = pat.retrieveTransactionInfoByHash(PatriciaTreeTransactionType.REGULAR, hash2);
        assertEquals(hash1, transactionBlockIDatabase.findByKey(String.valueOf(s1.get(0).getBlockHeight())).get().getTransactionList().get(s1.get(0).getPosition()).getHash());
        assertEquals(hash2, transactionBlockIDatabase.findByKey(String.valueOf(s2.get(0).getBlockHeight())).get().getTransactionList().get(s2.get(0).getPosition()).getHash());
        int g3 = 2;


        transactionBlockIDatabase.delete_db();
    }

    @Test
    public void ReceiptsTest() {
        CachedZoneIndex.getInstance().setZoneIndex(0);
        IDatabase<String, TransactionBlock> transactionBlockIDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));

        TransactionBlock transactionBlock1 = new TransactionBlock();
        transactionBlock1.setHeight(3);
        transactionBlock1.setHash("hash1");


        //ATTENTION FROM1A IS GET_TO NOT GET_FROM
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store("From1a", new PatriciaTreeNode(0, 0));

        Receipt.ReceiptBlock receiptBlock1 = new Receipt.ReceiptBlock(1, 1, "1");
        Receipt.ReceiptBlock receiptBlock1a = new Receipt.ReceiptBlock(2, 6, "1a");
        Receipt.ReceiptBlock receiptBlock2 = new Receipt.ReceiptBlock(3, 2, "2");
        Receipt.ReceiptBlock receiptBlock3 = new Receipt.ReceiptBlock(4, 3, "3");
        //its wrong each block must be unique for each zone need changes
        Receipt receipt1 = new Receipt(0, 1, receiptBlock1, null, 1);
        Receipt receipt2 = new Receipt(0, 1, receiptBlock1a, null, 2);
        Receipt receipt3 = new Receipt(2, 1, receiptBlock2, null, 1);
        Receipt receipt4 = new Receipt(2, 1, receiptBlock2, null, 2);
        Receipt receipt5 = new Receipt(3, 1, receiptBlock3, null, 1);
        Receipt receipt6 = new Receipt(4, 1, receiptBlock3, null, 2);

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

        InboundRelay inboundRelay = new InboundRelay(map);
        transactionBlock1.setInbound(inboundRelay);

        transactionBlockIDatabase.save(String.valueOf(transactionBlock1.getHeight()), transactionBlock1);


        String hashToSearch = HashUtil.sha256_bytetoString(recep.encode(receipt4));
        //ATTENTION FROM_1A IS GET_TO NOT GET_FROM
        transactionBlock1
                .getInbound()
                .getMap_receipts()
                .forEach((key, value) -> value
                        .entrySet()
                        .stream()
                        .forEach(entry -> {
                            entry.getValue().stream().forEach(receipt -> {
                                int position = Iterables.indexOf(entry.getValue(), u -> u.equals(receipt));
                                String hash = HashUtil.sha256_bytetoString(recep.encode(receipt));
                                TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress("From1a").get().addReceiptPosition(hash, CachedZoneIndex.getInstance().getZoneIndex(), transactionBlock1.getHeight(), receipt.getZoneFrom(), receipt.getReceiptBlock().getHeight(), position);
                            });
                        }));

        PatriciaTreeNode pat = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress("From1a").get();
        //1-->origin_zone
        //1-->BlockHeight
        //2-->zone
        //3-->receiptBlockHeight
        //4--->position
        StorageInfo s1 = new StorageInfo(CachedZoneIndex.getInstance().getZoneIndex(), 3, 2, 3, 1);
        StorageInfo random = new StorageInfo(CachedZoneIndex.getInstance().getZoneIndex(), 3, 2, 5, 6);
        StorageInfo s1cp = pat.retrieveReceiptInfoByHash(hashToSearch).get(0);
        assertEquals(s1, s1cp);
        assertEquals(receipt4, transactionBlockIDatabase.findByKey(String.valueOf(s1cp.getBlockHeight())).get().getInbound().getMap_receipts().get(s1cp.getZone()).entrySet().stream().filter(entry -> entry.getKey().getHeight() == s1cp.getReceiptBlockHeight()).findFirst().get().getValue().get(s1cp.getPosition()));
        try {
            transactionBlockIDatabase.findByKey(String.valueOf(random.getBlockHeight())).get().getInbound().getMap_receipts().get(random.getZone()).entrySet().stream().filter(entry -> entry.getKey().getHeight() == random.getReceiptBlockHeight()).findFirst().get().getValue().get(random.getPosition());
        } catch (NoSuchElementException e) {
        }
        assertEquals(1, pat.getReceiptCapacities().size());
        assertEquals(2, pat.getReceiptCapacities().get(0).getPositions().get(3).get(0).size());
        assertEquals(1, pat.getReceiptCapacities().get(0).getPositions().get(3).get(2).size());
        assertEquals(1, pat.getReceiptCapacities().get(0).getPositions().get(3).get(3).size());
        assertEquals(1, pat.getReceiptCapacities().get(0).getPositions().get(3).get(4).size());
        //0-->originzone
        //3-->blockheight
        //2-->zone
        //3-->receiptblockheight
        //size()->positions_receipt
        assertEquals(2, pat.getReceiptCapacities().get(0).getPositions().get(3).get(2).get(3).size());

        TransactionBlock transactionBlock2 = new TransactionBlock();
        transactionBlock2.setHeight(2);
        transactionBlock2.setHash("hash2");

        Receipt.ReceiptBlock receiptBlock2a = new Receipt.ReceiptBlock(1, 1, "1");
        Receipt.ReceiptBlock receiptBlock2b = new Receipt.ReceiptBlock(2, 6, "1a");
        Receipt.ReceiptBlock receiptBlock2c = new Receipt.ReceiptBlock(3, 2, "2");
        Receipt.ReceiptBlock receiptBlock3d = new Receipt.ReceiptBlock(4, 3, "3");
        //its wrong each block must be unique for each zone need changes
        Receipt receipt7 = new Receipt(0, 1, receiptBlock2a, null, 1);
        Receipt receipt8 = new Receipt(1, 1, receiptBlock2a, null, 2);
        Receipt receipt9 = new Receipt(2, 1, receiptBlock2c, null, 1);
        Receipt receipt10 = new Receipt(2, 1, receiptBlock2c, null, 2);
        Receipt receipt11 = new Receipt(3, 1, receiptBlock3d, null, 1);
        Receipt receipt12 = new Receipt(4, 1, receiptBlock2b, null, 2);

        ArrayList<Receipt> list1 = new ArrayList<>();
        list1.add(receipt7);
        list1.add(receipt8);
        list1.add(receipt9);
        list1.add(receipt10);
        list1.add(receipt11);
        list1.add(receipt12);
        Map<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> map1 = list1
                .stream()
                .collect(Collectors.groupingBy(Receipt::getZoneFrom, Collectors.groupingBy(Receipt::getReceiptBlock)));

        InboundRelay inboundRelay1 = new InboundRelay(map1);
        transactionBlock2.setInbound(inboundRelay1);

        transactionBlockIDatabase.save(String.valueOf(transactionBlock2.getHeight()), transactionBlock2);


        String hashToSearc2 = HashUtil.sha256_bytetoString(recep.encode(receipt12));
        //ATTENTION FROM1A IS GET_TO NOT GET_FROM
        transactionBlock2
                .getInbound()
                .getMap_receipts()
                .forEach((key, value) -> value
                        .entrySet()
                        .stream()
                        .forEach(entry -> {
                            entry.getValue().stream().forEach(receipt -> {
                                int position = Iterables.indexOf(entry.getValue(), u -> u.equals(receipt));
                                String hash = HashUtil.sha256_bytetoString(recep.encode(receipt));
                                TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress("From1a").get().addReceiptPosition(hash, CachedZoneIndex.getInstance().getZoneIndex(), transactionBlock2.getHeight(), receipt.getZoneFrom(), receipt.getReceiptBlock().getHeight(), position);
                            });
                        }));

        PatriciaTreeNode pat2 = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress("From1a").get();
        StorageInfo s2 = new StorageInfo(CachedZoneIndex.getInstance().getZoneIndex(), 2, 4, 2, 0);
        StorageInfo s2cp = pat2.retrieveReceiptInfoByHash(hashToSearc2).get(0);
        assertEquals(s2, s2cp);
        assertEquals(receipt12, transactionBlockIDatabase.findByKey(String.valueOf(s2cp.getBlockHeight())).get().getInbound().getMap_receipts().get(s2cp.getZone()).entrySet().stream().filter(entry -> entry.getKey().getHeight() == s2cp.getReceiptBlockHeight()).findFirst().get().getValue().get(s2cp.getPosition()));

    }
}
