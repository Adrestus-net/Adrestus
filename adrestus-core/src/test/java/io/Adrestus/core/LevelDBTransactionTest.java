package io.Adrestus.core;

import com.google.common.reflect.TypeToken;
import io.Adrestus.core.RingBuffer.publisher.TransactionEventPublisher;
import io.Adrestus.util.GetTime;
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
        IDatabase<String, LevelDBTransactionWrapper<Transaction>> database = new DatabaseFactory(String.class, Transaction.class, new TypeToken<LevelDBTransactionWrapper<Transaction>>() {
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


    @Test
    public void TransactionTest1() throws InterruptedException {
        IDatabase<String, LevelDBTransactionWrapper<Transaction>> database = new DatabaseFactory(String.class, Transaction.class, new TypeToken<LevelDBTransactionWrapper<Transaction>>() {
        }.getType()).getDatabase(DatabaseType.LEVEL_DB);
        Transaction transaction = new RegularTransaction();
        transaction.setAmount(100);
        transaction.setHash("Hash1");
        transaction.setFrom("1");
        transaction.setTimestamp(GetTime.GetTimeStampInString());
        transaction.setTo("2");
        Thread.sleep(100);

        Transaction transaction2 = new RegularTransaction();
        transaction2.setAmount(200);
        transaction2.setHash("Hash2");
        transaction2.setFrom("1");
        transaction2.setTimestamp(GetTime.GetTimeStampInString());
        transaction2.setTo("2");
        Thread.sleep(100);

        Transaction transaction3 = new RegularTransaction();
        transaction3.setAmount(200);
        transaction3.setHash("Hash3");
        transaction3.setFrom("1");
        transaction3.setTimestamp(GetTime.GetTimeStampInString());
        transaction3.setTo("3");
        Thread.sleep(100);

        Transaction transaction4 = new RegularTransaction();
        transaction4.setAmount(200);
        transaction4.setHash("Hash3");
        transaction4.setFrom("1");
        transaction4.setTimestamp(GetTime.GetTimeStampInString());
        transaction4.setTo("5");
        Thread.sleep(100);

        database.save("1", transaction);
        database.save("1", transaction2);
        database.save("1", transaction3);

        TransactionEventPublisher publisher = new TransactionEventPublisher(1024);

        publisher
                .withTimestampEventHandler().mergeEvents();
        publisher.start();
        publisher.publish(transaction4);
        publisher.getJobSyncUntilRemainingCapacityZero();

        database.delete_db();
    }
}
