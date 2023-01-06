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
import io.Adrestus.mapper.MemoryTreePoolSerializer;
import io.Adrestus.util.SerializationUtil;
import io.distributedLedger.*;
import io.vavr.control.Option;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        committeeBlock.getStakingMap().put(10.0, null);
        committeeBlock.getStakingMap().put(13.0, null);
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

}
