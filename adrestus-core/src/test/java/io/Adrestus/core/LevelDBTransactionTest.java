package io.Adrestus.core;

import com.google.common.reflect.TypeToken;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import io.distributedLedger.LevelDBTransactionWrapper;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LevelDBTransactionTest {

    @Test
    public void TransactionTest() {
        IDatabase<String, LevelDBTransactionWrapper<Transaction>> database = new DatabaseFactory(String.class,Transaction.class, new TypeToken<LevelDBTransactionWrapper<Transaction>>() {
        }.getType()).getDatabase(DatabaseType.LEVEL_DB);
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
        Optional<LevelDBTransactionWrapper<Transaction>> wrapper = database.findByKey("1");
        System.out.println(wrapper.get().toString());
        database.save("1", transaction);
        database.save("1", transaction2);
        database.save("1", transaction2);
        //   database.save("1",transaction2);
        Optional<LevelDBTransactionWrapper<Transaction>> wrapper2 = database.findByKey("1");

        System.out.println(wrapper2.get().toString());
        assertEquals(1, wrapper2.get().getFrom().size());
        assertEquals(1, wrapper2.get().getTo().size());
        database.delete_db();
    }
}
