package io.Adrestus.core;

import io.Adrestus.core.Resourses.MemoryTransactionPool;
import io.Adrestus.util.GetTime;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MemoryTransactionPoolTest {

    @Test
    //@Order(1)
    public void chek_duplicate() throws Exception {
        Transaction transaction = new RegularTransaction();
        transaction.setAmount(100);
        transaction.setHash("Hash");
        transaction.setTimestamp(GetTime.GetTimeStampInString());
        int count = (int) MemoryTransactionPool.getInstance().getAllStream().count();
        MemoryTransactionPool.getInstance().add(transaction);
        MemoryTransactionPool.getInstance().add(transaction);
        assertEquals(count + 1, MemoryTransactionPool.getInstance().getAllStream().count());
        assertNotEquals(count + 2, MemoryTransactionPool.getInstance().getAllStream().count());
    }

    @Test
    //@Order(2)
    public void add_mempool() throws Exception {
        Transaction transaction = new RegularTransaction();
        transaction.setAmount(101);
        transaction.setHash("Hash1");
        transaction.setTimestamp(GetTime.GetTimeStampInString());
        assertEquals(false, MemoryTransactionPool.getInstance().add(transaction));
        assertEquals(true, MemoryTransactionPool.getInstance().add(transaction));


        Transaction transaction1 = new RegularTransaction();
        transaction1.setAmount(50);
        transaction1.setHash("Hash2");
        transaction1.setTimestamp(GetTime.GetTimeStampInString());
        assertEquals(false, MemoryTransactionPool.getInstance().add(transaction1));
        assertEquals(true, MemoryTransactionPool.getInstance().add(transaction1));

        Transaction transaction3 = new RegularTransaction();
        transaction3.setAmount(50);
        transaction3.setHash("Hash2");
        transaction.setTimestamp(GetTime.GetTimeStampInString());
        assertEquals(true, MemoryTransactionPool.getInstance().add(transaction3));
        //  MemoryPool.getInstance().printAll();
    }

    @Test
    // @Order(4)
    public void delete_mempool() throws Exception {

        ArrayList<Transaction> list = new ArrayList<Transaction>();
        Transaction transaction1 = new RegularTransaction();
        transaction1.setAmount(100);
        transaction1.setHash("Hash1");

        Transaction transaction2 = new RegularTransaction();
        transaction2.setAmount(100);
        transaction2.setHash("Hash2");

        Transaction transaction3 = new RegularTransaction();
        transaction3.setAmount(100);
        transaction3.setHash("Hash3");
        transaction3.setTimestamp(GetTime.GetTimeStampInString());

        Transaction transaction4 = new RegularTransaction();
        transaction4.setAmount(100);
        transaction4.setHash("Hash4");
        transaction4.setTimestamp(GetTime.GetTimeStampInString());

        MemoryTransactionPool.getInstance().add(transaction1);
        MemoryTransactionPool.getInstance().add(transaction2);
        MemoryTransactionPool.getInstance().add(transaction3);
        MemoryTransactionPool.getInstance().add(transaction4);

        list.add(transaction3);
        list.add(transaction4);

        MemoryTransactionPool.getInstance().delete(list);
        //MemoryPool.getInstance().printAll();
    }


    @Test
    // @Order(3)
    public void mempool_get_by_hash() throws Exception {
        Transaction transaction1 = new RegularTransaction();
        transaction1.setAmount(100);
        transaction1.setHash("Hash4");
        transaction1.setTimestamp(GetTime.GetTimeStampInString());
        MemoryTransactionPool.getInstance().add(transaction1);

        Optional<Transaction> res = MemoryTransactionPool.getInstance().getTransactionByHash("Hash4");
        if (res.isPresent())
            System.out.println(res.get().toString());

        //  MemoryPool.getInstance().getAll().forEach(x -> System.out.println(x.toString()));
    }

    @Test
    //  @Order(5)
    public void mempool_timestamp_check() throws Exception {
        MemoryTransactionPool.getInstance().getAll().clear();
        Transaction transaction1 = new RegularTransaction();
        transaction1.setHash("Hash5");
        transaction1.setFrom("Address1");
        transaction1.setZoneFrom(1);
        transaction1.setTimestamp(GetTime.GetTimeStampInString());

        Thread.sleep(1000);
        Transaction transaction2 = new RegularTransaction();
        transaction2.setHash("Hash6");
        transaction2.setFrom("Address1");
        transaction2.setZoneFrom(1);
        transaction2.setTimestamp(GetTime.GetTimeStampInString());

        MemoryTransactionPool.getInstance().add(transaction1);
        MemoryTransactionPool.getInstance().add(transaction2);
        MemoryTransactionPool.getInstance().printAll();
        assertEquals(true, MemoryTransactionPool.getInstance().checkTimestamp(transaction2));

    }
}
