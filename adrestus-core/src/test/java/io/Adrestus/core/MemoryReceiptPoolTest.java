package io.Adrestus.core;

import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.Resourses.MemoryReceiptPool;
import io.Adrestus.util.SerializationUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

        Receipt receipt = new Receipt(0, 1, new RegularTransaction("hash1"));
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

        Receipt receipt = new Receipt(0, 1, new RegularTransaction("hash1"));
        assertEquals(false, MemoryReceiptPool.getInstance().add(receipt));
        assertEquals(true, MemoryReceiptPool.getInstance().add(receipt));


        Receipt receipt1 = new Receipt(0, 1, new RegularTransaction("hash2"));
        assertEquals(false, MemoryReceiptPool.getInstance().add(receipt1));
        assertEquals(true, MemoryReceiptPool.getInstance().add(receipt1));

        Receipt receipt3 = new Receipt(0, 1, new RegularTransaction("hash2"));
        assertEquals(true, MemoryReceiptPool.getInstance().add(receipt3));
        assertEquals(2, MemoryReceiptPool.getInstance().getAll().size());
    }

    @Test
    // @Order(4)
    public void delete_mempool() throws Exception {
        MemoryReceiptPool.getInstance().getAll().clear();

        ArrayList<Receipt> list = new ArrayList<Receipt>();
        Receipt receipt1 = new Receipt(0, 1, new RegularTransaction("hash1"));

        Receipt receipt2 = new Receipt(0, 1, new RegularTransaction("hash2"));

        Receipt receipt3 = new Receipt(0, 1, new RegularTransaction("hash3"));

        Receipt receipt4 = new Receipt(0, 1, new RegularTransaction("hash4"));


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
    // @Order(4)
    public void delete_receipt() throws Exception {
        MemoryReceiptPool.getInstance().getAll().clear();

        ArrayList<Receipt> list = new ArrayList<Receipt>();
        Receipt receipt1 = new Receipt(0, 1, new RegularTransaction("hash1"));

        Receipt receipt2 = new Receipt(0, 1, new RegularTransaction("hash2"));

        Receipt receipt3 = new Receipt(0, 1, new RegularTransaction("hash3"));

        Receipt receipt4 = new Receipt(0, 1, new RegularTransaction("hash4"));


        MemoryReceiptPool.getInstance().add(receipt1);
        MemoryReceiptPool.getInstance().add(receipt2);
        MemoryReceiptPool.getInstance().add(receipt3);
        MemoryReceiptPool.getInstance().add(receipt4);

        list.add(receipt3);
        list.add(receipt4);

        MemoryReceiptPool.getInstance().delete(new Receipt(new RegularTransaction("hash1")));
        MemoryReceiptPool.getInstance().printAll();
        assertEquals(3, MemoryReceiptPool.getInstance().getAll().size());
    }


    @Test
    // @Order(3)
    public void mempool_get_by_hash() throws Exception {
        MemoryReceiptPool.getInstance().getAll().clear();

        Receipt receipt1 = new Receipt(0, 1, new RegularTransaction("hash1"));
        MemoryReceiptPool.getInstance().add(receipt1);

        Optional<Receipt> res = MemoryReceiptPool.getInstance().getObjectByHash("hash1");
        if (res.isPresent())
            System.out.println(res.get().toString());

        //  MemoryPool.getInstance().getAll().forEach(x -> System.out.println(x.toString()));
    }

    @Test
    // @Order(3)
    public void check_if_order_is_maintend() throws Exception {
        MemoryReceiptPool.getInstance().getAll().clear();

        Receipt receipt1 = new Receipt(0, 1, new RegularTransaction("f"));
        Receipt receipt2 = new Receipt(0, 1, new RegularTransaction("a"));
        Receipt receipt3 = new Receipt(0, 1, new RegularTransaction("d"));
        Receipt receipt4 = new Receipt(0, 1, new RegularTransaction("z"));
        MemoryReceiptPool.getInstance().add(receipt1);
        MemoryReceiptPool.getInstance().add(receipt2);
        MemoryReceiptPool.getInstance().add(receipt3);
        MemoryReceiptPool.getInstance().add(receipt4);

        List<Receipt> list = MemoryReceiptPool.getInstance().getAll();

        assertEquals("a", list.get(0).getTransaction().getHash());
        assertEquals("d", list.get(1).getTransaction().getHash());
        assertEquals("f", list.get(2).getTransaction().getHash());
        assertEquals("z", list.get(3).getTransaction().getHash());
        //  MemoryPool.getInstance().getAll().forEach(x -> System.out.println(x.toString()));
    }

    @Test
    // @Order(3)
    public void group_by() throws Exception {
        SerializationUtil<Receipt> serenc = new SerializationUtil<Receipt>(Receipt.class);
        MemoryReceiptPool.getInstance().getAll().clear();
        Receipt.ReceiptBlock receiptBlock1 = new Receipt.ReceiptBlock("1", 1, 1, "1");
        Receipt.ReceiptBlock receiptBlock2 = new Receipt.ReceiptBlock("2", 2, 2, "2");

        //its wrong each block must be unique for each zone need changes
        Receipt receipt1 = new Receipt(0, 1, receiptBlock1, new RegularTransaction("a"));
        Receipt receipt2 = new Receipt(0, 1, receiptBlock1, new RegularTransaction("b"));
        Receipt receipt3 = new Receipt(2, 1, receiptBlock1, new RegularTransaction("c"));
        Receipt receipt4 = new Receipt(2, 1, receiptBlock2, new RegularTransaction("d"));
        Receipt receipt5 = new Receipt(3, 1, receiptBlock2, new RegularTransaction("f"));
        Receipt receipt6 = new Receipt(3, 1, receiptBlock2, new RegularTransaction("e"));
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
