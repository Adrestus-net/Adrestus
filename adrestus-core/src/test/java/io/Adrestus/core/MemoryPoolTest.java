package io.Adrestus.core;

import io.Adrestus.core.Resourses.MemoryPool;
import io.Adrestus.core.Resourses.IMemoryPool;
import io.Adrestus.util.GetTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MemoryPoolTest {

    @Test
    public void add_mempool() throws Exception {
        Transaction transaction = new RegularTransaction();
        transaction.setAmount(100);
        transaction.setHash("Hash");

        assertEquals(false, MemoryPool.getInstance().add(transaction));
        assertEquals(true, MemoryPool.getInstance().add(transaction));


        Transaction transaction1 = new RegularTransaction();
        transaction1.setAmount(50);
        transaction1.setHash("Hash2");

        assertEquals(false, MemoryPool.getInstance().add(transaction1));
        assertEquals(true, MemoryPool.getInstance().add(transaction1));

        Transaction transaction3 = new RegularTransaction();
        transaction3.setAmount(50);
        transaction3.setHash("Hash2");

        assertEquals(true, MemoryPool.getInstance().add(transaction3));
        MemoryPool.getInstance().printAll();
    }

    @Test
    public void delete_mempool() throws Exception {

        ArrayList<Transaction> list=new ArrayList<Transaction>();
        Transaction transaction1 = new RegularTransaction();
        transaction1.setAmount(100);
        transaction1.setHash("Hash1");

        Transaction transaction2 = new RegularTransaction();
        transaction2.setAmount(100);
        transaction2.setHash("Hash2");

        Transaction transaction3 = new RegularTransaction();
        transaction3.setAmount(100);
        transaction3.setHash("Hash3");


        Transaction transaction4 = new RegularTransaction();
        transaction4.setAmount(100);
        transaction4.setHash("Hash4");


        MemoryPool.getInstance().add(transaction1);
        MemoryPool.getInstance().add(transaction2);
        MemoryPool.getInstance().add(transaction3);
        MemoryPool.getInstance().add(transaction4);

        list.add(transaction3);
        list.add(transaction4);

        MemoryPool.getInstance().delete(list);
        MemoryPool.getInstance().printAll();
    }


    @Test
    public void mempool_get_by_hash() throws Exception {
        Transaction transaction1 = new RegularTransaction();
        transaction1.setAmount(100);
        transaction1.setHash("Hash4");

        MemoryPool.getInstance().add(transaction1);

        Optional<Transaction> res=MemoryPool.getInstance().getTransactionByHash("Hash4");
        if(res.isPresent())
            System.out.println(res.get().toString());

        MemoryPool.getInstance().getAll().forEach(x-> System.out.println(x.toString()));
    }

    @Test
    public void mempool_timestamp_check() throws Exception {
        Transaction transaction1 = new RegularTransaction();
        transaction1.setHash("Hash1");
        transaction1.setFrom("Address1");
        transaction1.setZoneFrom(1);
        transaction1.setTimestamp(GetTime.GetTimeStamp());

        Thread.sleep(1000);
        Transaction transaction2 = new RegularTransaction();
        transaction2.setHash("Hash2");
        transaction2.setFrom("Address1");
        transaction2.setZoneFrom(1);
        transaction2.setTimestamp(GetTime.GetTimeStamp());

        MemoryPool.getInstance().add(transaction1);

        assertEquals(true,MemoryPool.getInstance().checkTimestamp(transaction2));

    }
}
