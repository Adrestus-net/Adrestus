package io.distributedLedger;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LevelDBConnectionTest {

    @Test
    public void myEtest_database_file() {
        IDatabase<String, String> database = new DatabaseFactory(String.class, String.class).getDatabase(DatabaseType.LEVEL_DB);
        assertEquals(true, database.isDBexists());
        database.delete_db();
    }

    @Test
    public void myDadd_get1() {
        IDatabase<String, String> database = new DatabaseFactory(String.class, String.class).getDatabase(DatabaseType.LEVEL_DB);
        database.save("key", "value");
        Optional<String> value = database.findByKey("key");

        assertEquals("value", value.get());

        database.delete_db();
    }

    @Test
    public void myCput_ALL() throws InterruptedException {
        IDatabase<String, String> database = new DatabaseFactory(String.class, String.class).getDatabase(DatabaseType.LEVEL_DB);
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
    public void myBfind_between_range() {
        IDatabase<String, String> database = new DatabaseFactory(String.class, String.class).getDatabase(DatabaseType.LEVEL_DB);
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");
        map.put("key4", "value4");

        database.saveAll(map);
        assertEquals(4, database.findDBsize());
        Map<String, String> res = database.findBetweenRange("key2");
        assertEquals(map.get("key2"), res.get("key2"));
        assertEquals(map.get("key3"), res.get("key3"));
        assertEquals(map.get("key4"), res.get("key4"));
        assertEquals(3, res.entrySet().size());

        database.delete_db();
    }

    @Test
    public void myBfind_between_range2() {
        IDatabase<String, String> database = new DatabaseFactory(String.class, String.class).getDatabase(DatabaseType.LEVEL_DB);
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");
        map.put("key4", "value4");

        database.saveAll(map);
        assertEquals(4, database.findDBsize());
        Map<String, String> res = database.findBetweenRange("key234");
        assertEquals(0, res.entrySet().size());

        database.delete_db();
    }

    @Test
    public void myAdelete() {
        IDatabase<String, String> database = new DatabaseFactory(String.class, String.class).getDatabase(DatabaseType.LEVEL_DB);
        database.save("key", "value");
        database.save("key", "values");
        Optional<String> value = database.findByKey("key");

        assertEquals("values", value.get());

        database.deleteByKey("key");
        assertEquals(0, database.findDBsize());
        database.delete_db();
    }


}
