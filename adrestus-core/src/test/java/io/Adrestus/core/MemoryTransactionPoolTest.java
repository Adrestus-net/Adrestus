package io.Adrestus.core;

import io.Adrestus.core.Resourses.MemoryTransactionPool;
import io.Adrestus.util.GetTime;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MemoryTransactionPoolTest {

    @Test
    //@Order(1)
    public void chek_duplicate() throws Exception {
        MemoryTransactionPool.getInstance().getAll().clear();
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
        MemoryTransactionPool.getInstance().getAll().clear();
        Transaction transaction = new RegularTransaction();
        transaction.setAmount(101);
        transaction.setFrom("1");
        transaction.setHash("Hash1");
        transaction.setTimestamp(GetTime.GetTimeStampInString());
        assertEquals(false, MemoryTransactionPool.getInstance().add(transaction));
        assertEquals(true, MemoryTransactionPool.getInstance().add(transaction));


        Transaction transaction1 = new RegularTransaction();
        transaction1.setAmount(50);
        transaction1.setHash("Hash2");
        transaction1.setFrom("2");
        transaction1.setTimestamp(GetTime.GetTimeStampInString());
        assertEquals(false, MemoryTransactionPool.getInstance().add(transaction1));
        assertEquals(true, MemoryTransactionPool.getInstance().add(transaction1));

        Transaction transaction3 = new RegularTransaction();
        transaction3.setAmount(50);
        transaction3.setHash("Hash2");
        transaction3.setFrom("2");
        transaction.setTimestamp(GetTime.GetTimeStampInString());
        assertEquals(true, MemoryTransactionPool.getInstance().add(transaction3));
        //  MemoryPool.getInstance().printAll();
    }

    @Test
    // @Order(4)
    public void delete_mempool() throws Exception {
        MemoryTransactionPool.getInstance().clear();
        ArrayList<Transaction> list = new ArrayList<Transaction>();
        Transaction transaction1 = new RegularTransaction();
        transaction1.setAmount(100);
        transaction1.setFrom("1");
        transaction1.setHash("Hash1");

        Transaction transaction2 = new RegularTransaction();
        transaction2.setAmount(100);
        transaction2.setFrom("2");
        transaction2.setHash("Hash2");

        Transaction transaction3 = new RegularTransaction();
        transaction3.setAmount(100);
        transaction3.setHash("Hash3");
        transaction3.setFrom("3");
        transaction3.setTimestamp(GetTime.GetTimeStampInString());

        Transaction transaction4 = new RegularTransaction();
        transaction4.setAmount(100);
        transaction4.setHash("Hash4");
        transaction4.setFrom("4");
        transaction4.setTimestamp(GetTime.GetTimeStampInString());

        MemoryTransactionPool.getInstance().add(transaction1);
        MemoryTransactionPool.getInstance().add(transaction2);
        MemoryTransactionPool.getInstance().add(transaction3);
        MemoryTransactionPool.getInstance().add(transaction4);

        transaction3.setStatus(StatusType.SUCCES);
        transaction4.setStatus(StatusType.SUCCES);
        list.add(transaction3);
        list.add(transaction4);

        MemoryTransactionPool.getInstance().delete(list);
        List<Transaction> l = MemoryTransactionPool.getInstance().getAll();
        assertEquals(2, MemoryTransactionPool.getInstance().getAll().size());
        //MemoryPool.getInstance().printAll();
    }


    @Test
    // @Order(4)
    public void delete_mempool2() throws Exception {
        MemoryTransactionPool.getInstance().clear();
        Transaction transaction1 = new RegularTransaction();
        transaction1.setAmount(100);
        transaction1.setFrom("1");
        transaction1.setHash("Hash1");

        Transaction transaction2 = new RegularTransaction();
        transaction2.setAmount(100);
        transaction2.setFrom("1");
        transaction2.setHash("Hash2");

        Transaction transaction3 = new RegularTransaction();
        transaction3.setAmount(100);
        transaction3.setHash("Hash3");
        transaction3.setFrom("3");
        transaction3.setTimestamp(GetTime.GetTimeStampInString());

        Transaction transaction4 = new RegularTransaction();
        transaction4.setAmount(100);
        transaction4.setHash("Hash4");
        transaction4.setFrom("4");
        transaction4.setTimestamp(GetTime.GetTimeStampInString());

        MemoryTransactionPool.getInstance().add(transaction1);
        MemoryTransactionPool.getInstance().add(transaction2);
        MemoryTransactionPool.getInstance().add(transaction3);
        MemoryTransactionPool.getInstance().add(transaction4);


        MemoryTransactionPool.getInstance().delete(transaction2);
        List<Transaction> l = MemoryTransactionPool.getInstance().getAll();
        assertEquals(1, MemoryTransactionPool.getInstance().getFromSize("1"));
        assertEquals(3, MemoryTransactionPool.getInstance().getAll().size());
        //MemoryPool.getInstance().printAll();
    }

    @Test
    // @Order(3)
    public void mempool_get_by_hash() throws Exception {
        MemoryTransactionPool.getInstance().getAll().clear();
        Transaction transaction1 = new RegularTransaction();
        transaction1.setAmount(100);
        transaction1.setFrom("1");
        transaction1.setHash("Hash4");
        transaction1.setTimestamp(GetTime.GetTimeStampInString());
        MemoryTransactionPool.getInstance().add(transaction1);

        Optional<Transaction> res = MemoryTransactionPool.getInstance().getObjectByHash("Hash4");
        assertEquals(transaction1, res.get());

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

        Thread.sleep(1000);
        Transaction transaction3 = new RegularTransaction();
        transaction3.setHash("Hash6");
        transaction3.setFrom("Address1");
        transaction3.setZoneFrom(1);
        transaction3.setTimestamp(GetTime.GetTimeStampInString());

        MemoryTransactionPool.getInstance().add(transaction1);
        MemoryTransactionPool.getInstance().add(transaction2);
        boolean val = MemoryTransactionPool.getInstance().checkTimestamp(transaction3);
        assertEquals(true, val);
    }

    @Test
    //  @Order(5)
    public void mempool_address_check() throws Exception {
        MemoryTransactionPool.getInstance().clear();
        Transaction transaction1 = new RegularTransaction();
        transaction1.setHash("Hash5");
        transaction1.setFrom("ZAddress1");
        transaction1.setZoneFrom(1);
        transaction1.setTimestamp(GetTime.GetTimeStampInString());

        Thread.sleep(1000);
        Transaction transaction2 = new RegularTransaction();
        transaction2.setHash("Hash6");
        transaction2.setFrom("DAddress1");
        transaction2.setZoneFrom(1);
        transaction2.setTimestamp(GetTime.GetTimeStampInString());

        Thread.sleep(1000);
        Transaction transaction3 = new RegularTransaction();
        transaction3.setHash("Hash7");
        transaction3.setFrom("CAddress1");
        transaction3.setZoneFrom(1);
        transaction3.setTimestamp(GetTime.GetTimeStampInString());

        Thread.sleep(1000);
        Transaction transaction4 = new RegularTransaction();
        transaction4.setHash("Hash4");
        transaction4.setFrom("EAddress1");
        transaction4.setZoneFrom(1);
        transaction4.setTimestamp(GetTime.GetTimeStampInString());

        MemoryTransactionPool.getInstance().add(transaction1);
        MemoryTransactionPool.getInstance().add(transaction2);
        MemoryTransactionPool.getInstance().add(transaction3);
        MemoryTransactionPool.getInstance().add(transaction4);

        boolean val = MemoryTransactionPool.getInstance().checkAdressExists(transaction2);
        Transaction transaction6 = new RegularTransaction();
        transaction6.setFrom("as");
        transaction6.setHash("as");
        boolean val3 = MemoryTransactionPool.getInstance().checkAdressExists(transaction6);

        assertEquals(true, val);
        assertEquals(false, val3);
    }

    @Test
    //  @Order(5)
    public void Stream_Size_check() throws Exception {
        MemoryTransactionPool.getInstance().clear();
        Transaction transaction1 = new RegularTransaction();
        transaction1.setHash("Hash1");
        transaction1.setFrom("Address1");
        transaction1.setZoneFrom(1);

        Transaction transaction2 = new RegularTransaction();
        transaction2.setHash("Hash2");
        transaction2.setFrom("Address2");
        transaction2.setZoneFrom(2);


        Transaction transaction3 = new RegularTransaction();
        transaction3.setHash("Hash3");
        transaction3.setFrom("Address3");
        transaction3.setZoneFrom(1);


        Transaction transaction4 = new RegularTransaction();
        transaction4.setHash("Hash4");
        transaction4.setFrom("Address4");
        transaction4.setZoneFrom(1);


        Transaction transaction5 = new RegularTransaction();
        transaction5.setHash("Hash5");
        transaction5.setFrom("Address5");
        transaction5.setZoneFrom(1);

        Transaction transaction6 = new RegularTransaction();
        transaction6.setHash("Hash5");
        transaction6.setFrom("Address6");
        transaction6.setZoneFrom(1);

        MemoryTransactionPool.getInstance().add(transaction1);
        MemoryTransactionPool.getInstance().add(transaction2);
        MemoryTransactionPool.getInstance().add(transaction3);
        MemoryTransactionPool.getInstance().add(transaction4);
        MemoryTransactionPool.getInstance().add(transaction5);
        MemoryTransactionPool.getInstance().add(transaction6);

        assertEquals(6, MemoryTransactionPool.getInstance().getAllStream().count());
        assertEquals(6, MemoryTransactionPool.getInstance().getAll().size());
    }
}
