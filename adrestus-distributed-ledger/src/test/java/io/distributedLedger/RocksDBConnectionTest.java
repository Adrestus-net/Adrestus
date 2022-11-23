package io.distributedLedger;

import io.Adrestus.config.Directory;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
    public void add_get2() {
        IDatabase<String, String> database = new DatabaseFactory(String.class, String.class).getDatabase(DatabaseType.ROCKS_DB,DatabaseInstance.ZONE_1_TRANSACTION_BLOCK);
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
        Map<String, String> map = new LinkedHashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");
        map.put("key4", "value4");
        map.put("key5", "value5");
        map.put("key6", "value6");
        map.put("key7", "value7");

        database.saveAll(map);

        Map<String, String> res = database.findBetweenRange("key2");
        assertEquals(map.get("key2"), res.get("key2"));
        assertEquals(map.get("key3"), res.get("key3"));
        assertEquals(map.get("key4"), res.get("key4"));
        assertEquals(6, res.entrySet().size());

        database.delete_db();
    }

    @Test
    public void find_between_range2() {
        IDatabase<String, String> database = new DatabaseFactory(String.class, String.class).getDatabase(DatabaseType.ROCKS_DB);
        database.save("key1", "value1");
        database.save("key2", "value2");
        database.save("key3", "value3");
        database.save("key4", "value4");
        database.save("key5", "value5");
        database.save("key6", "value6");
        database.save("key7", "value7");


        Map<String, String> res = database.findBetweenRange("key2");
        assertEquals(6, res.entrySet().size());

        database.delete_db();
    }


}
