package io.distributedLedger;

import com.linkedin.paldb.api.PalDB;
import com.linkedin.paldb.api.Serializer;
import com.linkedin.paldb.api.StoreReader;
import com.linkedin.paldb.api.StoreWriter;
import io.Adrestus.core.AbstractBlock;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.TransactionBlock;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RocksDBConnectionTest {

    @Test
    public void test_database_file() {
        IDatabase<String, String> database = new DatabaseFactory(String.class,String.class).getDatabase(DatabaseType.ROCKS_DB);
        assertEquals(true, database.isDBexists());
    }

    @Test
    public void add_get1() {
        IDatabase<String, String> database = new DatabaseFactory(String.class,String.class).getDatabase(DatabaseType.ROCKS_DB);
        database.save("key", "value");
        Optional<String> value = database.findByKey("key");

        assertEquals("value", value.get());

        database.delete_db();
    }

     @Test
    public void add_get2() {
        IDatabase<String, AbstractBlock> database = new DatabaseFactory(String.class,AbstractBlock.class).getDatabase(DatabaseType.ROCKS_DB);
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
    public void find_between_range1() {
       /* IDatabase<String, Integer> database = new DatabaseFactory(Integer.class).getDatabase(DatabaseType.PAL_DB);
        database.save("key1", 1);
        database.save("key2", 2);
        database.save("key3", 3);
        database.save("key4", 4);
        database.save("key5", 5);
        database.save("key6", 5);


        Map<String,Integer>map= database.findBetweenRange("key1");

        for (Map.Entry<String,Integer> entry : map.entrySet())
            System.out.println("Key = " + entry.getKey() +
                    ", Value = " + entry.getValue());
        database.delete_db();*/
    }

}
