package io.Adrestus.core;

import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.Resourses.MemoryReceiptPool;
import io.Adrestus.core.Resourses.MemoryTransactionPool;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.util.SerializationUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class MemoryReceiptPoolTest {


    @BeforeAll
    public static void setup() {
        CachedZoneIndex.getInstance().setZoneIndex(1);
    }

    @Test
    //@Order(1)
    public void chek_duplicate() throws Exception {
        MemoryReceiptPool.getInstance().getAll().clear();

        Receipt receipt = new Receipt(0, 1, new Receipt.ReceiptBlock(1, 1, "as"), 1, null);
        int count = (int) MemoryReceiptPool.getInstance().getAllStream().count();
        MemoryReceiptPool.getInstance().add(receipt);
        MemoryReceiptPool.getInstance().add(receipt);
        assertEquals(count + 1, MemoryReceiptPool.getInstance().getAllStream().count());
        assertNotEquals(count + 2, MemoryReceiptPool.getInstance().getAllStream().count());
    }

    @Test
    //@Order(2)
    public void add_mempool() throws Exception {
        MemoryReceiptPool.getInstance().getAll().clear();

        Receipt receipt = new Receipt(0, 1, new Receipt.ReceiptBlock(1, 1, "as"), 1, null);
        assertEquals(false, MemoryReceiptPool.getInstance().add(receipt));
        assertEquals(true, MemoryReceiptPool.getInstance().add(receipt));


        Receipt receipt1 = new Receipt(0, 1, new Receipt.ReceiptBlock(1, 1, "as"), 2, null);
        assertEquals(false, MemoryReceiptPool.getInstance().add(receipt1));
        assertEquals(true, MemoryReceiptPool.getInstance().add(receipt1));

        Receipt receipt3 = new Receipt(0, 1, new Receipt.ReceiptBlock(1, 1, "as"), 2, null);
        assertEquals(true, MemoryReceiptPool.getInstance().add(receipt3));
        assertEquals(2, MemoryReceiptPool.getInstance().getAll().size());
    }

    @Test
    // @Order(4)
    public void delete_mempool() throws Exception {
        MemoryReceiptPool.getInstance().getAll().clear();

        ArrayList<Receipt> list = new ArrayList<Receipt>();
        Receipt receipt1 = new Receipt(0, 1, new Receipt.ReceiptBlock(1, 1, "as"), 1, null);

        Receipt receipt2 = new Receipt(0, 1, new Receipt.ReceiptBlock(1, 1, "as"), 2, null);

        Receipt receipt3 = new Receipt(0, 1, new Receipt.ReceiptBlock(1, 1, "as"), 3, null);

        Receipt receipt4 = new Receipt(0, 1, new Receipt.ReceiptBlock(1, 1, "as"), 4, null);


        MemoryReceiptPool.getInstance().add(receipt1);
        MemoryReceiptPool.getInstance().add(receipt2);
        MemoryReceiptPool.getInstance().add(receipt3);
        MemoryReceiptPool.getInstance().add(receipt4);

        list.add(receipt3);
        list.add(receipt4);

        MemoryReceiptPool.getInstance().delete(list);
        MemoryReceiptPool.getInstance().printAll();
    }

    @Test
    // @Order(3)
    public void InboundOutboundCheck() throws Exception {
        MemoryReceiptPool.getInstance().getAll().clear();
        CachedZoneIndex.getInstance().setZoneIndex(0);
        Receipt receipt1 = new Receipt(0, 1, new Receipt.ReceiptBlock(1, 1, "as"), 1, null);

        Receipt receipt2 = new Receipt(0, 1, new Receipt.ReceiptBlock(2, 1, "as"), 2, null);

        Receipt receipt3 = new Receipt(2, 0, new Receipt.ReceiptBlock(3, 1, "as"), 3, null);

        Receipt receipt4 = new Receipt(2, 0, new Receipt.ReceiptBlock(4, 1, "as"), 4, null);

        Receipt receipt5 = new Receipt(2, 0, new Receipt.ReceiptBlock(5, 1, "as"), 5, null);

        Receipt receipt6 = new Receipt(3, 3, new Receipt.ReceiptBlock(6, 1, "as"), 6, null);

        MemoryReceiptPool.getInstance().add(receipt1);
        MemoryReceiptPool.getInstance().add(receipt2);
        MemoryReceiptPool.getInstance().add(receipt3);
        MemoryReceiptPool.getInstance().add(receipt4);
        MemoryReceiptPool.getInstance().add(receipt5);
        MemoryReceiptPool.getInstance().add(receipt6);

        ArrayList<Receipt>ListByZone= (ArrayList<Receipt>) MemoryReceiptPool.getInstance().getListByZone(CachedZoneIndex.getInstance().getZoneIndex());
        ArrayList<Receipt>InboundList= (ArrayList<Receipt>) MemoryReceiptPool.getInstance().getInboundList(CachedZoneIndex.getInstance().getZoneIndex());
        ArrayList<Receipt>OutBoundList= (ArrayList<Receipt>) MemoryReceiptPool.getInstance().getOutBoundList(CachedZoneIndex.getInstance().getZoneIndex());
        ArrayList<Receipt>ListToDelete= (ArrayList<Receipt>)MemoryReceiptPool.getInstance().getListToDelete(CachedZoneIndex.getInstance().getZoneIndex());

        assertEquals(2,ListByZone.size());
        assertEquals(3,InboundList.size());
        assertEquals(2,OutBoundList.size());
        assertEquals(1,ListToDelete.size());

        assertEquals(receipt1, ListByZone.get(0));
        assertEquals(receipt2, ListByZone.get(1));

        assertEquals(receipt3, InboundList.get(0));
        assertEquals(receipt4, InboundList.get(1));
        assertEquals(receipt5, InboundList.get(2));

        assertEquals(receipt1, OutBoundList.get(0));
        assertEquals(receipt2, OutBoundList.get(1));

        assertEquals(receipt6, ListToDelete.get(0));

    }
    @Test
    //@Order(4)
    public void delete_receipt() throws Exception {
        MemoryReceiptPool.getInstance().getAll().clear();

        ArrayList<Receipt> list = new ArrayList<Receipt>();
        Receipt receipt1 = new Receipt(0, 1, new Receipt.ReceiptBlock(1, 1, "as"), 1, null);

        Receipt receipt2 = new Receipt(0, 1, new Receipt.ReceiptBlock(1, 1, "as"), 2, null);

        Receipt receipt3 = new Receipt(0, 1, new Receipt.ReceiptBlock(1, 1, "as"), 3, null);

        Receipt receipt4 = new Receipt(0, 1, new Receipt.ReceiptBlock(1, 1, "as"), 4, null);


        MemoryReceiptPool.getInstance().add(receipt1);
        MemoryReceiptPool.getInstance().add(receipt2);
        MemoryReceiptPool.getInstance().add(receipt3);
        MemoryReceiptPool.getInstance().add(receipt4);

        list.add(receipt3);
        list.add(receipt4);

        assertEquals(4, MemoryReceiptPool.getInstance().getAll().size());
        MemoryReceiptPool.getInstance().delete(receipt1);
        MemoryReceiptPool.getInstance().delete(receipt4);
        MemoryReceiptPool.getInstance().printAll();
        assertEquals(2, MemoryReceiptPool.getInstance().getAll().size());
    }


//    @Test
//    // @Order(3)
//    public void mempool_get_by_hash() throws Exception {
//        MemoryReceiptPool.getInstance().getAll().clear();
//
//        Receipt receipt1 = new Receipt(0, 1);
//        MemoryReceiptPool.getInstance().add(receipt1);
//
//        Optional<Receipt> res = MemoryReceiptPool.getInstance().getObjectByHash("hash1");
//        if (res.isPresent())
//            System.out.println(res.get().toString());
//
//        //  MemoryPool.getInstance().getAll().forEach(x -> System.out.println(x.toString()));
//    }

    @Test
    // @Order(3)
    public void check_if_order_is_maintend() throws Exception {
        MemoryReceiptPool.getInstance().getAll().clear();

        Receipt receipt1 = new Receipt(0, 1, new Receipt.ReceiptBlock(2, 1, "b"), 1, null);
        Receipt receipt2 = new Receipt(0, 1, new Receipt.ReceiptBlock(4, 1, "c"), 1, null);
        Receipt receipt3 = new Receipt(0, 1, new Receipt.ReceiptBlock(1, 1, "a"), 1, null);
        Receipt receipt4 = new Receipt(0, 1, new Receipt.ReceiptBlock(3, 1, "d"), 1, null);
        MemoryReceiptPool.getInstance().add(receipt1);
        MemoryReceiptPool.getInstance().add(receipt2);
        MemoryReceiptPool.getInstance().add(receipt3);
        MemoryReceiptPool.getInstance().add(receipt4);

        ArrayList<Receipt> list = new ArrayList<>(MemoryReceiptPool.getInstance().getAll());

        assertEquals(receipt3, list.get(0));
        assertEquals(receipt1, list.get(1));
        assertEquals(receipt4, list.get(2));
        assertEquals(receipt2, list.get(3));
        //  MemoryPool.getInstance().getAll().forEach(x -> System.out.println(x.toString()));
    }

    @Test
    // @Order(3)
    public void group_by() throws Exception {
        MemoryReceiptPool.getInstance().getAll().clear();
        List<SerializationUtil.Mapping> lists = new ArrayList<>();
        lists.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        SerializationUtil<Receipt> serenc = new SerializationUtil<Receipt>(Receipt.class, lists);
        MemoryReceiptPool.getInstance().getAll().clear();
        Receipt.ReceiptBlock receiptBlock1 = new Receipt.ReceiptBlock(1, 1, "1");
        Receipt.ReceiptBlock receiptBlock2 = new Receipt.ReceiptBlock(2, 2, "2");

        RegularTransaction tr1 = new RegularTransaction("a");
        RegularTransaction tr2 = new RegularTransaction("b");
        RegularTransaction tr3 = new RegularTransaction("c");
        RegularTransaction tr4 = new RegularTransaction("d");
        RegularTransaction tr5 = new RegularTransaction("e");
        RegularTransaction tr6 = new RegularTransaction("f");
        tr1.setTo("1");
        tr2.setTo("2");
        tr3.setTo("3");
        tr4.setTo("4");
        tr5.setTo("5");
        tr6.setTo("6");
        //its wrong each block must be unique for each zone need changes
        Receipt receipt1 = new Receipt(0, 1, receiptBlock1, 1, null);
        Receipt receipt2 = new Receipt(0, 1, receiptBlock1, 2, null);
        Receipt receipt3 = new Receipt(2, 1, receiptBlock1, 3, null);
        Receipt receipt4 = new Receipt(2, 1, receiptBlock2, 4, null);
        Receipt receipt5 = new Receipt(3, 1, receiptBlock2, 5, null);
        Receipt receipt6 = new Receipt(3, 1, receiptBlock2, 6, null);
        Receipt clon = serenc.decode(serenc.encode(receipt1));
        assertEquals(receipt1, clon);
        MemoryReceiptPool.getInstance().add(receipt1);
        MemoryReceiptPool.getInstance().add(receipt2);
        MemoryReceiptPool.getInstance().add(receipt3);
        MemoryReceiptPool.getInstance().add(receipt4);
        MemoryReceiptPool.getInstance().add(receipt5);
        MemoryReceiptPool.getInstance().add(receipt6);

        List<Receipt> list = MemoryReceiptPool.getInstance().getAll();
        Map<Integer, List<Receipt>> studlistGrouped = list.stream().collect(Collectors.groupingBy(w -> w.getZoneFrom()));
        Map<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> map = list
                .stream()
                .collect(Collectors.groupingBy(Receipt::getZoneFrom, Collectors.groupingBy(Receipt::getReceiptBlock, Collectors.mapping(Receipt::merge, Collectors.toList()))));
    }
}
