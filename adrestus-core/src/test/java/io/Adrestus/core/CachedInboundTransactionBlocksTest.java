package io.Adrestus.core;

import io.Adrestus.core.Resourses.CachedInboundTransactionBlocks;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CachedInboundTransactionBlocksTest {


    @Test
    public void testAdd() {
        TransactionBlock transactionBlock = new TransactionBlock();
        transactionBlock.setGeneration(4);
        transactionBlock.setHash("hash");


        Receipt.ReceiptBlock receiptBlock1 = new Receipt.ReceiptBlock(1, 1, "1");
        Receipt.ReceiptBlock receiptBlock1a = new Receipt.ReceiptBlock(2, 6, "1a");
        Receipt.ReceiptBlock receiptBlock2 = new Receipt.ReceiptBlock(3, 2, "2");
        Receipt.ReceiptBlock receiptBlock3 = new Receipt.ReceiptBlock(4, 3, "3");
        Receipt.ReceiptBlock receiptBlock4 = new Receipt.ReceiptBlock(12, 3, "3");
        //its wrong each block must be unique for each zone need changes
        Receipt receipt1 = new Receipt(0, 1, receiptBlock1, null, 1, "a");
        Receipt receipt2 = new Receipt(1, 1, receiptBlock1a, null, 2, "b");
        Receipt receipt3 = new Receipt(2, 1, receiptBlock2, null, 1, "c");
        Receipt receipt4 = new Receipt(2, 1, receiptBlock2, null, 2, "d");
        Receipt receipt4a = new Receipt(2, 1, receiptBlock4, null, 2, "n");
        Receipt receipt5 = new Receipt(2, 1, receiptBlock3, null, 1, "e");
        Receipt receipt6 = new Receipt(3, 1, receiptBlock3, null, 2, "f");

        ArrayList<Receipt> list = new ArrayList<>();
        ArrayList<Receipt> list2 = new ArrayList<>();
        list.add(receipt1);
        list.add(receipt2);
        list.add(receipt3);
        list2.add(receipt4);
        list2.add(receipt4a);
        list2.add(receipt5);
        list2.add(receipt6);
        Map<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> map = list
                .stream()
                .collect(Collectors.groupingBy(Receipt::getZoneFrom, Collectors.groupingBy(Receipt::getReceiptBlock)));

        Map<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> map2 = list2
                .stream()
                .collect(Collectors.groupingBy(Receipt::getZoneFrom, Collectors.groupingBy(Receipt::getReceiptBlock)));

        Map<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> map3 = new HashMap<>();
        OutBoundRelay outBoundRelay1 = new OutBoundRelay(map);
        OutBoundRelay outBoundRelay2 = new OutBoundRelay(map2);
        OutBoundRelay outBoundRelay3 = new OutBoundRelay(map3);

        CachedInboundTransactionBlocks.getInstance().prepare(outBoundRelay1.getMap_receipts());
        CachedInboundTransactionBlocks.getInstance().prepare(outBoundRelay2.getMap_receipts());
        CachedInboundTransactionBlocks.getInstance().prepare(outBoundRelay3.getMap_receipts());
        Map<Integer, HashMap<Integer, HashSet<String>>> integerHashSetMap = CachedInboundTransactionBlocks.getInstance().getBlock_retrieval();
        assertEquals(1, integerHashSetMap.get(0).get(1).size());
        assertEquals(1, integerHashSetMap.get(1).get(6).size());
        assertEquals(1, integerHashSetMap.get(2).get(2).size());
        assertEquals(2, integerHashSetMap.get(2).get(3).size());
    }
}
