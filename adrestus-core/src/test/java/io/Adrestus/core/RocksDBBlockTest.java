package io.Adrestus.core;

import com.google.common.reflect.TypeToken;
import io.Adrestus.MemoryTreePool;
import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomSerializerTreeMap;
import io.Adrestus.crypto.elliptic.mapper.StakingData;
import io.Adrestus.mapper.MemoryTreePoolSerializer;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.util.SerializationUtil;
import io.distributedLedger.*;
import io.vavr.control.Option;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class RocksDBBlockTest {


    @Test
    public void add_get2() {
        IDatabase<String, AbstractBlock> database = new DatabaseFactory(String.class, AbstractBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.ZONE_1_TRANSACTION_BLOCK);
        String hash = "Hash";
        TransactionBlock prevblock = new TransactionBlock();
        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.setGeneration(1);
        committeeBlock.setViewID(1);
        prevblock.setHeight(1);
        prevblock.setHash(hash);
        database.save(hash, prevblock);
        TransactionBlock copy = (TransactionBlock) database.findByKey(hash).get();
        assertEquals(prevblock, copy);
        System.out.println(copy.toString());
        database.delete_db();
    }

    @Test
    public void add_erase3() {
        IDatabase<String, AbstractBlock> database = new DatabaseFactory(String.class, AbstractBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.ZONE_1_TRANSACTION_BLOCK);
        String hash = "Hash";
        TransactionBlock prevblock = new TransactionBlock();
        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.setGeneration(1);
        committeeBlock.setViewID(1);
        prevblock.setHeight(1);
        prevblock.setHash(hash);
        database.save(hash, prevblock);
        TransactionBlock copy = (TransactionBlock) database.findByKey(hash).get();
        assertEquals(prevblock, copy);
        assertEquals(1, database.findDBsize());
        database.erase_db();
        assertEquals(0, database.findDBsize());
        database.delete_db();
    }

    @Test
    public void add_get3() {
        IDatabase<String, AbstractBlock> database = new DatabaseFactory(String.class, AbstractBlock.class).getDatabase(DatabaseType.ROCKS_DB);
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        SerializationUtil<AbstractBlock> serenc = new SerializationUtil<AbstractBlock>(AbstractBlock.class, list);
        String hash = "Hash";
        CommitteeBlock committeeBlock = new CommitteeBlock();
        KademliaData data = new KademliaData();
        committeeBlock.getStakingMap().put(new StakingData(1, 10.0), data);
        committeeBlock.getStakingMap().put(new StakingData(2, 13.0), data);
        committeeBlock.getStructureMap().get(0).put(new BLSPublicKey(), "");
        committeeBlock.getStructureMap().put(0, new LinkedHashMap<BLSPublicKey, String>());
        committeeBlock.setGeneration(1);
        committeeBlock.setViewID(1);
        committeeBlock.setHeight(1);
        committeeBlock.setHash(hash);
        committeeBlock.setDifficulty(119);
        committeeBlock.setVRF("sadsada");
        committeeBlock.setVDF("sadaa");
        CommitteeBlock cp = (CommitteeBlock) serenc.decode(serenc.encode(committeeBlock));
        assertEquals(committeeBlock, cp);
        database.save(hash, committeeBlock);
        CommitteeBlock copy = (CommitteeBlock) database.findByKey(hash).get();
        assertEquals(committeeBlock, copy);
        System.out.println(copy.toString());
        database.delete_db();
    }


    @Test
    public void delete() {
        IDatabase<String, AbstractBlock> database = new DatabaseFactory(String.class, AbstractBlock.class).getDatabase(DatabaseType.ROCKS_DB);
        String hash = "Hash";
        TransactionBlock prevblock = new TransactionBlock();
        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.setGeneration(1);
        committeeBlock.setViewID(1);
        prevblock.setHeight(1);
        prevblock.setHash(hash);
        database.save(hash, prevblock);
        TransactionBlock copy = (TransactionBlock) database.findByKey(hash).get();
        assertEquals(prevblock, copy);
        database.deleteByKey(hash);
        Optional<AbstractBlock> empty = database.findByKey(hash);
        assertEquals(Optional.empty(), empty);
        database.delete_db();
    }


    @Test
    public void find_by_list_key() {
        IDatabase<String, AbstractBlock> database = new DatabaseFactory(String.class, AbstractBlock.class).getDatabase(DatabaseType.ROCKS_DB);
        TransactionBlock transactionBlock1 = new TransactionBlock();
        transactionBlock1.setHash("hash1");

        TransactionBlock transactionBlock2 = new TransactionBlock();
        transactionBlock2.setHash("hash2");

        database.save("hash1", transactionBlock1);
        database.save("hash2", transactionBlock2);
        ArrayList<String> list = new ArrayList<>();
        list.add("hash1");
        list.add("hash2");

        List<AbstractBlock> values = database.findByListKey(list);
        assertEquals(transactionBlock1, values.get(0));
        assertEquals(transactionBlock2, values.get(1));

        database.delete_db();
    }

    @Test
    public void add_get4() throws Exception {
        Type fluentType = new TypeToken<MemoryTreePool>() {
        }.getType();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        SerializationUtil valueMapper = new SerializationUtil<>(fluentType, list);

        String address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        PatriciaTreeNode treeNode = new PatriciaTreeNode(2, 1);
        TreeFactory.getMemoryTree(1).store(address, treeNode);
        MemoryTreePool m = (MemoryTreePool) TreeFactory.getMemoryTree(1);

        //m.getByaddress(address);
        //use only special
        byte[] buffer = valueMapper.encode_special(m, SerializationUtils.serialize(m).length);

        IDatabase<String, byte[]> database = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_0);
        database.save("hash1", buffer);

        Optional<byte[]> value = database.findByKey("hash1");

        MemoryTreePool copy = (MemoryTreePool) valueMapper.decode(value.get());


        //copy.store(address, treeNode);
        Option<PatriciaTreeNode> pat = copy.getByaddress(address);

        if (!pat.isDefined())
            System.out.println("error");
        int g = 3;
        database.delete_db();
    }

    @Test
    public void seek_last() {
        IDatabase<String, TransactionBlock> database = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.ZONE_1_TRANSACTION_BLOCK);
        String hash = "Hash";
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

        TransactionBlock transactionBlock5 = new TransactionBlock();
        transactionBlock5.setHeight(5);
        transactionBlock5.setHash("hash5");

        TransactionBlock transactionBlock6 = new TransactionBlock();
        transactionBlock6.setHeight(6);
        transactionBlock6.setHash("hash6");

        database.save("hash1", transactionBlock1);
        database.save("hash2", transactionBlock2);
        database.save("hash3", transactionBlock3);
        database.save("hash4", transactionBlock4);
        database.save("hash5", transactionBlock5);
        database.save("hash6", transactionBlock6);


        Optional<TransactionBlock> copy = database.seekLast();
        Optional<TransactionBlock> first = database.seekFirst();
        assertEquals(transactionBlock6, copy.get());
        assertEquals(transactionBlock1, first.get());
        database.delete_db();
    }

    @Test
    public void seek_first() {
        IDatabase<String, TransactionBlock> database = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.ZONE_1_TRANSACTION_BLOCK);
        String hash = "Hash";
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

        TransactionBlock transactionBlock5 = new TransactionBlock();
        transactionBlock5.setHeight(5);
        transactionBlock5.setHash("hash5");

        TransactionBlock transactionBlock6 = new TransactionBlock();
        transactionBlock6.setHeight(6);
        transactionBlock6.setHash("hash6");

        database.save("hash1", transactionBlock1);
        database.save("hash2", transactionBlock2);
        database.save("hash3", transactionBlock3);
        database.save("hash4", transactionBlock4);
        database.save("hash5", transactionBlock5);
        database.save("hash6", transactionBlock6);


        Map<String, TransactionBlock> map = database.seekFromStart();
        List<TransactionBlock> result = new ArrayList<TransactionBlock>(map.values());
        assertEquals(transactionBlock1, result.get(0));
        assertEquals(transactionBlock2, result.get(1));
        assertEquals(transactionBlock3, result.get(2));
        assertEquals(transactionBlock4, result.get(3));
        assertEquals(transactionBlock5, result.get(4));
        assertEquals(transactionBlock6, result.get(5));
        List<String> stdCodeList = result.stream()
                .filter(val -> val.getHeight() > 3)
                .map(TransactionBlock::getHash)
                .collect(Collectors.toList());
        List<TransactionBlock> result2 = result.stream()
                .filter(val -> !stdCodeList.contains(val.getHash()))
                .collect(Collectors.toList());
        database.delete_db();
    }

    @Test
    public void save_all() {
        IDatabase<String, TransactionBlock> database = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.ZONE_1_TRANSACTION_BLOCK);
        String hash = "Hash";
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

        TransactionBlock transactionBlock5 = new TransactionBlock();
        transactionBlock5.setHeight(5);
        transactionBlock5.setHash("hash5");

        TransactionBlock transactionBlock6 = new TransactionBlock();
        transactionBlock6.setHeight(6);
        transactionBlock6.setHash("hash6");

        Map<String, TransactionBlock> map = new HashMap<>();
        map.put("hash1", transactionBlock1);
        map.put("hash2", transactionBlock2);
        map.put("hash3", transactionBlock3);
        map.put("hash4", transactionBlock4);
        map.put("hash5", transactionBlock5);
        map.put("hash6", transactionBlock6);

        database.saveAll(map);

        Optional<TransactionBlock> copy = database.seekLast();
        assertEquals(transactionBlock6, copy.get());
        database.delete_db();
    }

    @Test
    public void find_between_range() {
        IDatabase<String, TransactionBlock> database = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.ZONE_1_TRANSACTION_BLOCK);
        String hash = "Hash";
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

        TransactionBlock transactionBlock5 = new TransactionBlock();
        transactionBlock5.setHeight(5);
        transactionBlock5.setHash("hash5");

        TransactionBlock transactionBlock6 = new TransactionBlock();
        transactionBlock6.setHeight(6);
        transactionBlock6.setHash("hash6");

        Map<String, TransactionBlock> map = new HashMap<>();
        map.put("hash1", transactionBlock1);
        map.put("hash2", transactionBlock2);
        map.put("hash3", transactionBlock3);
        map.put("hash4", transactionBlock4);
        map.put("hash5", transactionBlock5);
        map.put("hash6", transactionBlock6);

        database.saveAll(map);

        Map<String, TransactionBlock> map_returned = database.findBetweenRange("hash1");
        Optional<String> firstKey = map_returned.keySet().stream().findFirst();
        assertEquals("hash1", firstKey.get());
        database.delete_db();
    }
    @Test
    public void find_between_range2() {
        IDatabase<String, TransactionBlock> database = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.ZONE_1_TRANSACTION_BLOCK);
        String hash = "Hash";
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

        TransactionBlock transactionBlock5 = new TransactionBlock();
        transactionBlock5.setHeight(5);
        transactionBlock5.setHash("hash5");

        TransactionBlock transactionBlock6 = new TransactionBlock();
        transactionBlock6.setHeight(6);
        transactionBlock6.setHash("hash6");

        Map<String, TransactionBlock> map = new HashMap<>();
        map.put("hash1", transactionBlock1);
        map.put("hash2", transactionBlock2);
        map.put("hash3", transactionBlock3);
        map.put("hash4", transactionBlock4);
        map.put("hash5", transactionBlock5);
        map.put("hash6", transactionBlock6);

        database.saveAll(map);

        Map<String, TransactionBlock> map_returned = database.findBetweenRange("hash145");
        assertEquals(0, map_returned.size());
        database.delete_db();
    }

    @Test
    public void save_all_tree() throws Exception {
        IDatabase<String, byte[]> tree_database = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getPatriciaTreeZoneInstance(0));

        Type fluentType = new TypeToken<MemoryTreePool>() {
        }.getType();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        SerializationUtil valueMapper = new SerializationUtil<>(fluentType, list);

        String address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        PatriciaTreeNode treeNode = new PatriciaTreeNode(2, 1);
        TreeFactory.getMemoryTree(1).store(address, treeNode);
        MemoryTreePool m = (MemoryTreePool) TreeFactory.getMemoryTree(1);

        //m.getByaddress(address);
        //use only special
        byte[] bt = valueMapper.encode_special(m, SerializationUtils.serialize(m).length);
        Map<String, byte[]> map = new HashMap<>();
        map.put(m.getRootHash(), bt);
        tree_database.saveAll(map);

        Optional<byte[]> copy = tree_database.seekLast();
        assertEquals(m, valueMapper.decode(copy.get()));
        tree_database.delete_db();


    }
}
