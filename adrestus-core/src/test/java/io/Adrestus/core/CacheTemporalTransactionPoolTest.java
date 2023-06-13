package io.Adrestus.core;

import io.Adrestus.core.Resourses.CacheTemporalTransactionPool;
import io.Adrestus.core.Resourses.MemoryTransactionPool;
import io.Adrestus.util.GetTime;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CacheTemporalTransactionPoolTest {

    @BeforeAll
    public static void setup() {
        MemoryTransactionPool.getInstance().getSize();
        CacheTemporalTransactionPool.getInstance().setup(false);
    }

    @Test
    public void add() {
        CacheTemporalTransactionPool.getInstance().clear();
        Transaction transaction1 = new RegularTransaction();
        transaction1.setHash("Hash1");
        transaction1.setFrom("Address1");
        transaction1.setZoneFrom(1);
        transaction1.setTimestamp(GetTime.GetTimeStampInString());

        CacheTemporalTransactionPool.getInstance().add(transaction1);

        assertEquals(1, CacheTemporalTransactionPool.getInstance().size());
    }

    @Test
    public void add2() throws InterruptedException {
        CacheTemporalTransactionPool.getInstance().clear();
        Transaction transaction1 = new RegularTransaction();
        transaction1.setHash("Hash1");
        transaction1.setFrom("Address1");
        transaction1.setZoneFrom(1);
        transaction1.setTimestamp(GetTime.GetTimeStampInString());

        CacheTemporalTransactionPool.getInstance().add(transaction1);

        Thread.sleep(500);
        Transaction transaction2 = new RegularTransaction();
        transaction2.setHash("Hash2");
        transaction2.setFrom("Address1");
        transaction2.setZoneFrom(1);
        transaction2.setTimestamp(GetTime.GetTimeStampInString());

        CacheTemporalTransactionPool.getInstance().add(transaction2);
        assertEquals(1, CacheTemporalTransactionPool.getInstance().size());
    }

    @Test
    public void add3() throws InterruptedException {
        CacheTemporalTransactionPool.getInstance().clear();
        Transaction transaction1 = new RegularTransaction();
        transaction1.setHash("Hash1");
        transaction1.setFrom("Address1");
        transaction1.setZoneFrom(1);
        transaction1.setNonce(1);
        transaction1.setTimestamp(GetTime.GetTimeStampInString());

        CacheTemporalTransactionPool.getInstance().add(transaction1);

        Thread.sleep(500);
        Transaction transaction2 = new RegularTransaction();
        transaction2.setHash("Hash2");
        transaction2.setFrom("Address1");
        transaction2.setZoneFrom(1);
        transaction1.setNonce(2);
        transaction2.setTimestamp(GetTime.GetTimeStampInString());

        CacheTemporalTransactionPool.getInstance().add(transaction2);
        Thread.sleep(1500);
        assertEquals(0, CacheTemporalTransactionPool.getInstance().size());
    }


    //change the cache configuration EXPIRATION_MINUTES and scheduleAtFixedRate
    @SneakyThrows
    //@Test
    public void TimerInvalidate() throws InterruptedException {
        CacheTemporalTransactionPool.getInstance().clear();
        Transaction transaction1 = new RegularTransaction();
        transaction1.setHash("Hash1");
        transaction1.setFrom("Address1");
        transaction1.setZoneFrom(1);
        transaction1.setNonce(1);
        transaction1.setTimestamp(GetTime.GetTimeStampInString());


        Thread.sleep(500);
        Transaction transaction2 = new RegularTransaction();
        transaction2.setHash("Hash2");
        transaction2.setFrom("Address1");
        transaction2.setZoneFrom(1);
        transaction1.setNonce(2);
        transaction2.setTimestamp(GetTime.GetTimeStampInString());

        MemoryTransactionPool.getInstance().add(transaction1);
        MemoryTransactionPool.getInstance().add(transaction2);
        CacheTemporalTransactionPool.getInstance().add(transaction1);
        CacheTemporalTransactionPool.getInstance().add(transaction2);
        Thread.sleep(4000);
        assertEquals(0, CacheTemporalTransactionPool.getInstance().size());
    }
}
