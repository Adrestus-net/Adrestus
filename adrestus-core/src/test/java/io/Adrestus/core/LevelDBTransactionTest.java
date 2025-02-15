package io.Adrestus.core;

import com.google.common.reflect.TypeToken;
import io.Adrestus.core.RingBuffer.publisher.TransactionEventPublisher;
import io.Adrestus.crypto.elliptic.mapper.BigDecimalSerializer;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationFuryUtil;
import io.Adrestus.util.SerializationUtil;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LevelDBTransactionTest {

    @Test
    public void TransactionTest() {
        IDatabase<String, Transaction> database = new DatabaseFactory(String.class, Transaction.class, new TypeToken<Transaction>() {
        }.getType()).getDatabase(DatabaseType.LEVEL_DB);
        Transaction transaction = new RegularTransaction();
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setHash("1");
        transaction.setFrom("1");
        transaction.setTo("2");

        Transaction transaction2 = new RegularTransaction();
        transaction2.setAmount(BigDecimal.valueOf(200));
        transaction2.setHash("2");
        transaction2.setFrom("3");
        transaction2.setTo("1");

        Transaction transaction3 = new RegularTransaction();
        transaction3.setAmount(BigDecimal.valueOf(200));
        transaction3.setHash("3");
        transaction3.setFrom("4");
        transaction3.setTo("1");

        database.save("1", transaction);
        database.save("2", transaction2);
        database.save("3", transaction3);
        Optional<Transaction> wrapper = database.findByKey("1");
        Optional<Transaction> wrapper2 = database.findByKey("2");
        Optional<Transaction> wrapper3 = database.findByKey("3");
        assertEquals(transaction, wrapper.get());
        assertEquals(transaction2, wrapper2.get());
        assertEquals(transaction3, wrapper3.get());
        database.delete_db();
    }


    @Test
    public void TransactionAddEraseTest() {
        IDatabase<String, Transaction> database = new DatabaseFactory(String.class, Transaction.class, new TypeToken<Transaction>() {
        }.getType()).getDatabase(DatabaseType.LEVEL_DB);
        Transaction transaction = new RegularTransaction();
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setHash("1");
        transaction.setFrom("1");
        transaction.setTo("2");

        Transaction transaction2 = new RegularTransaction();
        transaction2.setAmount(BigDecimal.valueOf(200));
        transaction2.setHash("2");
        transaction2.setFrom("3");
        transaction2.setTo("1");

        Transaction transaction3 = new RegularTransaction();
        transaction3.setAmount(BigDecimal.valueOf(200));
        transaction3.setHash("3");
        transaction3.setFrom("4");
        transaction3.setTo("1");

        database.save("1", transaction);
        database.save("2", transaction2);
        database.save("3", transaction3);
        Optional<Transaction> wrapper = database.findByKey("1");
        Optional<Transaction> wrapper2 = database.findByKey("2");
        Optional<Transaction> wrapper3 = database.findByKey("3");
        assertEquals(transaction, wrapper.get());
        assertEquals(transaction2, wrapper2.get());
        assertEquals(transaction3, wrapper3.get());
        database.erase_db();
        assertEquals(0, database.findDBsize());
        database.delete_db();
    }


    @Test
    public void TransactionTest1() throws InterruptedException {
        IDatabase<String, Transaction> database = new DatabaseFactory(String.class, Transaction.class, new TypeToken<Transaction>() {
        }.getType()).getDatabase(DatabaseType.LEVEL_DB);

        Transaction transaction = new RegularTransaction();
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setHash("Hash1");
        transaction.setFrom("1");
        transaction.setTimestamp(GetTime.GetTimeStampInString());
        transaction.setTo("2");
        Thread.sleep(100);

        Transaction transaction2 = new RegularTransaction();
        transaction2.setAmount(BigDecimal.valueOf(200));
        transaction2.setHash("Hash2");
        transaction2.setFrom("1");
        transaction2.setTimestamp(GetTime.GetTimeStampInString());
        transaction2.setTo("2");
        Thread.sleep(100);

        Transaction transaction3 = new RegularTransaction();
        transaction3.setAmount(BigDecimal.valueOf(200));
        transaction3.setHash("Hash3");
        transaction3.setFrom("1");
        transaction3.setTimestamp(GetTime.GetTimeStampInString());
        transaction3.setTo("3");
        Thread.sleep(100);

        Transaction transaction4 = new RegularTransaction();
        transaction4.setAmount(BigDecimal.valueOf(200));
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

    @Test
    public void TransactionSerializeTest() throws InterruptedException {
        IDatabase<String, Transaction> database = new DatabaseFactory(String.class, RegularTransaction.class, new TypeToken<Transaction>() {
        }.getType()).getDatabase(DatabaseType.LEVEL_DB);
        Transaction transaction = new RegularTransaction();
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setHash("Hash123");
        transaction.setFrom("1");
        transaction.setTo("2");
        transaction.setTimestamp(GetTime.GetTimeStampInString());
        Thread.sleep(200);


        Transaction transaction2 = new RegularTransaction();
        transaction2.setAmount(BigDecimal.valueOf(200));
        transaction2.setHash("Hash124");
        transaction2.setFrom("3");
        transaction2.setTo("1");
        transaction2.setTimestamp(GetTime.GetTimeStampInString());
        Thread.sleep(200);

        Transaction transaction3 = new RegularTransaction();
        transaction3.setAmount(BigDecimal.valueOf(200));
        transaction3.setHash("Hash345");
        transaction3.setFrom("4");
        transaction3.setTo("1");
        transaction3.setTimestamp(GetTime.GetTimeStampInString());
        Thread.sleep(200);

        database.save("1", transaction);
        database.save("1", transaction2);
        database.save("1", transaction2);
        database.save("1", transaction3);
        //   database.save("1",transaction2);
        Optional<Transaction> wrapper2 = database.findByKey("1");
        Map<String, Transaction> map = database.seekFromStart();

        Type fluentType = new TypeToken<Map<String, Transaction>>() {
        }.getType();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        SerializationUtil valueMapper = new SerializationUtil<>(fluentType, list);

        byte[] buffer = SerializationFuryUtil.getInstance().getFury().serialize(map);
        Map<String, Transaction> copy = (Map<String, Transaction>) SerializationFuryUtil.getInstance().getFury().deserialize(buffer);

        assertEquals(map, copy);

        database.delete_db();
    }
}
