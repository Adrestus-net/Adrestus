package io.Adrestus.core;

import com.google.common.reflect.TypeToken;
import io.Adrestus.MemoryTreePool;
import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.core.Util.BlockSizeCalculator;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.BLSSignatureData;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.BLSSignature;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.crypto.elliptic.ECDSASignatureData;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.mapper.BigDecimalSerializer;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomSerializerTreeMap;
import io.Adrestus.crypto.elliptic.mapper.StakingData;
import io.Adrestus.mapper.MemoryTreePoolSerializer;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.util.SerializationUtil;
import io.distributedLedger.*;
import io.vavr.control.Option;
import lombok.SneakyThrows;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class RocksDBBlockTest {
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
    private static ECKeyPair ecKeyPair1, ecKeyPair2, ecKeyPair3;
    private static String address1, address2, address3;


    @SneakyThrows
    @BeforeAll
    public static void setup() {
        sizeCalculator = new BlockSizeCalculator();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        serenc = new SerializationUtil<AbstractBlock>(AbstractBlock.class, list);

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
    }

    @Test
    public void SerializeBlockDatabase() throws CloneNotSupportedException {
        IDatabase<String, TransactionBlock> database = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(1));
        HashMap<BLSPublicKey, BLSSignatureData> hashMap = new HashMap<BLSPublicKey, BLSSignatureData>();
        BLSSignatureData blsSignatureData1 = new BLSSignatureData();
        blsSignatureData1.getSignature()[0] = BLSSignature.sign("toSign".getBytes(StandardCharsets.UTF_8), sk1);
        blsSignatureData1.getMessageHash()[0] = "asdas";
        BLSSignatureData blsSignatureData2 = new BLSSignatureData();
        BLSSignatureData blsSignatureData3 = new BLSSignatureData();
        BLSSignatureData blsSignatureData4 = new BLSSignatureData();
        hashMap.put(vk3, blsSignatureData1);
        hashMap.put(vk4, blsSignatureData2);
        hashMap.put(vk1, blsSignatureData3);
        hashMap.put(vk2, blsSignatureData4);
        AbstractBlock block = new TransactionBlock();
        block.setHash("1");
        block.setHeight(1);
        block.AddAllSignatureData(hashMap);
        TransactionBlock a = (TransactionBlock) block.clone();
        database.save(String.valueOf(a.getHeight()), a);
        TransactionBlock copy = (TransactionBlock) database.findByKey("1").get();
        assertEquals(block, copy);
        System.out.println(copy.toString());
        database.delete_db();
    }

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
    public void Check_DBinstances() {
        IDatabase<String, AbstractBlock> database = new DatabaseFactory(String.class, AbstractBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.ZONE_1_TRANSACTION_BLOCK);
        IDatabase<String, AbstractBlock> committe = new DatabaseFactory(String.class, AbstractBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);
        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.setHash("2");
        committeeBlock.setHeight(2);
        TransactionBlock prevblock = new TransactionBlock();
        prevblock.setHash("1");
        prevblock.setHeight(1);
        database.save("1", prevblock);
        committe.save("2", committeeBlock);
        IDatabase<String, AbstractBlock> databasea = new DatabaseFactory(String.class, AbstractBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.ZONE_1_TRANSACTION_BLOCK);
        IDatabase<String, AbstractBlock> committea = new DatabaseFactory(String.class, AbstractBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);
        TransactionBlock copy = (TransactionBlock) databasea.findByKey("1").get();
        CommitteeBlock copy2 = (CommitteeBlock) committea.findByKey("2").get();
        assertEquals(prevblock, copy);
        assertEquals(committeeBlock, copy2);
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
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        SerializationUtil<AbstractBlock> serenc = new SerializationUtil<AbstractBlock>(AbstractBlock.class, list);
        String hash = "Hash";
        CommitteeBlock committeeBlock = new CommitteeBlock();
        KademliaData data = new KademliaData();
        committeeBlock.getStakingMap().put(new StakingData(1, BigDecimal.valueOf(10.0)), data);
        committeeBlock.getStakingMap().put(new StakingData(2, BigDecimal.valueOf(13.0)), data);
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
    public void find_by_key() {
        IDatabase<String, AbstractBlock> database = new DatabaseFactory(String.class, AbstractBlock.class).getDatabase(DatabaseType.ROCKS_DB);
        TransactionBlock transactionBlock1 = new TransactionBlock();
        transactionBlock1.setHash("hash1");

        database.save("hash1", transactionBlock1);

        transactionBlock1.setHash("new hash");
        Optional<AbstractBlock> values = database.findByKey("hash1");
        assertNotEquals(transactionBlock1, values.get());

        database.delete_db();
    }

    @Test
    public void add_get4() throws Exception {
        Type fluentType = new TypeToken<MemoryTreePool>() {
        }.getType();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        SerializationUtil valueMapper = new SerializationUtil<>(fluentType, list);

        String address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        PatriciaTreeNode treeNode = new PatriciaTreeNode(BigDecimal.valueOf(2), 1);
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
        map.put("hash2", transactionBlock2);
        map.put("hash3", transactionBlock3);
        map.put("hash4", transactionBlock4);
        map.put("hash5", transactionBlock5);
        map.put("hash6", transactionBlock6);

        database.saveAll(map);

        Map<String, TransactionBlock> map_returned = database.findBetweenRange("hash2");
        assertEquals(map, map_returned);
        Optional<String> firstKey = map_returned.keySet().stream().findFirst();
        assertEquals("hash2", firstKey.get());
        assertEquals(5, map_returned.size());
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
    public void seek_between_range() {
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

        Map<String, TransactionBlock> map2 = new HashMap<>();
        map2.put("hash3", transactionBlock3);
        map2.put("hash4", transactionBlock4);
        map2.put("hash5", transactionBlock5);
        map2.put("hash6", transactionBlock6);

        Map<String, TransactionBlock> map3 = new HashMap<>();
        map3.put("hash3", transactionBlock3);
        map3.put("hash4", transactionBlock4);
        map3.put("hash5", transactionBlock5);
        map3.put("hash6", transactionBlock6);

        database.saveAll(map);

        Map<String, TransactionBlock> map_returned = database.seekBetweenRange(3, database.seekLast().get().getHeight());
        Map<String, TransactionBlock> map_returned3 = database.findBetweenRange("hash3");
        assertEquals(map3, map_returned3);
        assertEquals(map2, map_returned);
        Optional<String> firstKey = map_returned.keySet().stream().findFirst();
        assertEquals("hash3", firstKey.get());
        assertEquals(4, map_returned.size());
        database.delete_db();
    }

    @Test
    public void save_all_tree() throws Exception {
        IDatabase<String, byte[]> tree_database = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getPatriciaTreeZoneInstance(0));

        Type fluentType = new TypeToken<MemoryTreePool>() {
        }.getType();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        SerializationUtil valueMapper = new SerializationUtil<>(fluentType, list);

        String address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        PatriciaTreeNode treeNode = new PatriciaTreeNode(BigDecimal.valueOf(2), 1);
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
