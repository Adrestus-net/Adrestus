package io.Adrestus.core;

import com.google.common.reflect.TypeToken;
import io.Adrestus.core.RingBuffer.publisher.TransactionEventPublisher;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import io.distributedLedger.LevelDBTransactionWrapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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
        transaction2.setHash("Hash12");
        transaction2.setFrom("3");
        transaction2.setTo("1");

        Transaction transaction3 = new RegularTransaction();
        transaction3.setAmount(200);
        transaction3.setHash("Hash3");
        transaction3.setFrom("4");
        transaction3.setTo("1");

        database.save("1", transaction);
        Optional<LevelDBTransactionWrapper<Transaction>> wrapper = database.findByKey("1");
        System.out.println(wrapper.get().toString());
        database.save("1", transaction);
        database.save("1", transaction2);
        database.save("1", transaction2);
        database.save("1", transaction3);
        //   database.save("1",transaction2);
        Optional<LevelDBTransactionWrapper<Transaction>> wrapper2 = database.findByKey("1");

        System.out.println(wrapper2.get().toString());
        assertEquals(1, wrapper2.get().getFrom().size());
        assertEquals(2, wrapper2.get().getTo().size());
        assertEquals(transaction3, wrapper2.get().getTo().get(1));
        assertEquals(transaction2, wrapper2.get().getTo().get(0));
        database.delete_db();
    }

    @Test
    public void TransactionReceiptTest() {
        IDatabase<String, LevelDBTransactionWrapper<Transaction>> database = new DatabaseFactory(String.class, Transaction.class, new TypeToken<LevelDBTransactionWrapper<Transaction>>() {
        }.getType()).getDatabase(DatabaseType.LEVEL_DB);
        IDatabase<String, LevelDBTransactionWrapper<Receipt>> receiptdatabase = new DatabaseFactory(String.class, Receipt.class, new TypeToken<LevelDBTransactionWrapper<Receipt>>() {
        }.getType()).getDatabase(DatabaseType.LEVEL_DB);
        Transaction transaction = new RegularTransaction();
        transaction.setAmount(100);
        transaction.setHash("Hash");
        transaction.setFrom("1");
        transaction.setTo("2");

        Receipt receipt=new Receipt(0,2,transaction);
        receipt.setAddress("2");
        receipt.setReceiptBlock(new Receipt.ReceiptBlock());
        database.save("1", transaction);
        receiptdatabase.save(receipt.getAddress(),receipt);
        Optional<LevelDBTransactionWrapper<Transaction>> wrapper = database.findByKey("1");
        Optional<LevelDBTransactionWrapper<Receipt>> wrapperreceipt = receiptdatabase.findByKey("2");
        assertEquals(transaction, wrapper.get().getFrom().get(0));
        assertEquals(receipt, wrapperreceipt.get().getTo().get(0));

        database.delete_db();
        receiptdatabase.delete_db();
    }
    @Test
    public void TransactionAddEraseTest() {
        IDatabase<String, LevelDBTransactionWrapper<Transaction>> database = new DatabaseFactory(String.class, Transaction.class, new TypeToken<LevelDBTransactionWrapper<Transaction>>() {
        }.getType()).getDatabase(DatabaseType.LEVEL_DB);
        Transaction transaction = new RegularTransaction();
        transaction.setAmount(100);
        transaction.setHash("Hash");
        transaction.setFrom("1");
        transaction.setTo("2");

        Transaction transaction2 = new RegularTransaction();
        transaction2.setAmount(200);
        transaction2.setHash("Hash12");
        transaction2.setFrom("3");
        transaction2.setTo("1");

        Transaction transaction3 = new RegularTransaction();
        transaction3.setAmount(200);
        transaction3.setHash("Hash3");
        transaction3.setFrom("4");
        transaction3.setTo("1");

        database.save("1", transaction);
        Optional<LevelDBTransactionWrapper<Transaction>> wrapper = database.findByKey("1");
        System.out.println(wrapper.get().toString());
        database.save("1", transaction);
        database.save("1", transaction2);
        database.save("1", transaction2);
        database.save("1", transaction3);
        //   database.save("1",transaction2);
        assertNotEquals(0, database.findDBsize());
        database.erase_db();
        assertEquals(0, database.findDBsize());
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

    @Test
    public void TransactionSerializeTest() throws InterruptedException {
        IDatabase<String, LevelDBTransactionWrapper<Transaction>> database = new DatabaseFactory(String.class, RegularTransaction.class, new TypeToken<LevelDBTransactionWrapper<Transaction>>() {
        }.getType()).getDatabase(DatabaseType.LEVEL_DB);
        Transaction transaction = new RegularTransaction();
        transaction.setAmount(100);
        transaction.setHash("Hash123");
        transaction.setFrom("1");
        transaction.setTo("2");
        transaction.setTimestamp(GetTime.GetTimeStampInString());
        Thread.sleep(200);


        Transaction transaction2 = new RegularTransaction();
        transaction2.setAmount(200);
        transaction2.setHash("Hash124");
        transaction2.setFrom("3");
        transaction2.setTo("1");
        transaction2.setTimestamp(GetTime.GetTimeStampInString());
        Thread.sleep(200);

        Transaction transaction3 = new RegularTransaction();
        transaction3.setAmount(200);
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
        Optional<LevelDBTransactionWrapper<Transaction>> wrapper2 = database.findByKey("1");
        Map<String, LevelDBTransactionWrapper<Transaction>> map = database.seekFromStart();
        int buffsize = 0;
        for (Map.Entry<String, LevelDBTransactionWrapper<Transaction>> entry : map.entrySet()) {
            buffsize += entry.getValue().getFrom().size() + entry.getValue().getTo().size();
        }
        buffsize = buffsize * 1024;

        Type fluentType = new TypeToken<Map<String, LevelDBTransactionWrapper<Transaction>>>() {
        }.getType();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        SerializationUtil valueMapper = new SerializationUtil<>(fluentType, list);

        byte[] buffer = valueMapper.encode_special(map, buffsize);
        Map<String, LevelDBTransactionWrapper<Transaction>> copy = (Map<String, LevelDBTransactionWrapper<Transaction>>) valueMapper.decode(buffer);

        assertEquals(map, copy);

        database.delete_db();
    }
}
