package io.distributedLedger;

import io.Adrestus.core.RegularTransaction;
import io.Adrestus.core.Transaction;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LevelDBConnectionTest {

    @Test
    public void test_database_file() {
        IDatabase<String, String> database = new DatabaseFactory(String.class, String.class).getDatabase(DatabaseType.LEVEL_DB);
        assertEquals(true, database.isDBexists());
        database.delete_db();
    }

    @Test
    public void add_get1() {
        IDatabase<String, String> database = new DatabaseFactory(String.class, String.class).getDatabase(DatabaseType.LEVEL_DB);
        database.save("key", "value");
        Optional<String> value = database.findByKey("key");

        assertEquals("value", value.get());

        database.delete_db();
    }

    @Test
    public void put_ALL() throws InterruptedException {
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
    public void find_between_range() {
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
    public void delete() {
        IDatabase<String, String> database = new DatabaseFactory(String.class, String.class).getDatabase(DatabaseType.LEVEL_DB);
        database.save("key", "value");
        database.save("key", "values");
        Optional<String> value = database.findByKey("key");

        assertEquals("values", value.get());

        database.deleteByKey("key");
        assertEquals(0, database.findDBsize());
        database.delete_db();

    }

    @Test
    public void TransactionTest() {
        IDatabase<String, LevelDBTransactionWrapper> database = new DatabaseFactory(String.class, LevelDBTransactionWrapper.class).getDatabase(DatabaseType.LEVEL_DB);
        Transaction transaction = new RegularTransaction();
        transaction.setAmount(100);
        transaction.setHash("Hash");
        transaction.setFrom("1");
        transaction.setTo("2");

        Transaction transaction2 = new RegularTransaction();
        transaction2.setAmount(200);
        transaction2.setHash("Hash1");
        transaction2.setFrom("3");
        transaction2.setTo("1");

        database.save("1", transaction);
        Optional<LevelDBTransactionWrapper> wrapper = database.findByKey("1");
        System.out.println(wrapper.get().toString());
        database.save("1", transaction);
        database.save("1", transaction2);
        database.save("1", transaction2);
        //   database.save("1",transaction2);
        Optional<LevelDBTransactionWrapper> wrapper2 = database.findByKey("1");

        System.out.println(wrapper2.get().toString());
        assertEquals(1, wrapper2.get().getFrom().size());
        assertEquals(1, wrapper2.get().getTo().size());
    }
}
