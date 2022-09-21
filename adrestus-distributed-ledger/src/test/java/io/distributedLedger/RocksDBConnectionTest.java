package io.distributedLedger;

import io.Adrestus.core.AbstractBlock;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.TransactionBlock;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RocksDBConnectionTest {

    @Test
    public void test_database_file() {
        IDatabase<String, String> database = new DatabaseFactory(String.class, String.class).getDatabase(DatabaseType.ROCKS_DB);
        assertEquals(true, database.isDBexists());
        database.delete_db();
    }

    @Test
    public void add_get1() {
        IDatabase<String, String> database = new DatabaseFactory(String.class, String.class).getDatabase(DatabaseType.ROCKS_DB);
        database.save("key", "value");
        Optional<String> value = database.findByKey("key");

        assertEquals("value", value.get());

        database.delete_db();
    }

    @Test
    public void put_ALL() throws InterruptedException {
        IDatabase<String, String> database = new DatabaseFactory(String.class, String.class).getDatabase(DatabaseType.ROCKS_DB);
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");
        map.put("key4", "value4");
        map.put("key5", "value5");
        map.put("key5", "value6");
        database.saveAll(map);

        Thread.sleep(100);
        assertEquals(5, database.findDBsize());

        database.delete_db();
    }

    @Test
    public void find_between_range() {
        IDatabase<String, String> database = new DatabaseFactory(String.class, String.class).getDatabase(DatabaseType.ROCKS_DB);
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");
        map.put("key4", "value4");

        database.saveAll(map);

        Map<String, String> res = database.findBetweenRange("key2");
        assertEquals(map.get("key2"), res.get("key2"));
        assertEquals(map.get("key3"), res.get("key3"));
        assertEquals(map.get("key4"), res.get("key4"));
        assertEquals(3, res.entrySet().size());

        database.delete_db();
    }

    @Test
    public void add_get2() {
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


}
