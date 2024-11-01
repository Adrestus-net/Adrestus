package io.Adrestus.protocol;

import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.OutBoundRelay;
import io.Adrestus.core.Receipt;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.network.IPFinder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ReceiptTaskTest {
    private static BLSPrivateKey sk1;
    private static BLSPublicKey vk1;
    private static BLSPrivateKey sk3;
    private static BLSPublicKey vk3;

    private static BLSPrivateKey sk2;
    private static BLSPublicKey vk2;
    private static BLSPrivateKey sk4;
    private static BLSPublicKey vk4;

    private static BLSPrivateKey sk5;
    private static BLSPublicKey vk5;

    private static BLSPrivateKey sk6;
    private static BLSPublicKey vk6;

    private static CommitteeBlock committeeBlock;
    private static TransactionBlock transactionBlock;

    @BeforeAll
    public static void setup() throws Exception {
        if (System.getenv("MAVEN_OPTS") != null) {
            return;
        }
        CachedZoneIndex.getInstance().setZoneIndex(0);
        sk1 = new BLSPrivateKey(1);
        vk1 = new BLSPublicKey(sk1);

        sk2 = new BLSPrivateKey(2);
        vk2 = new BLSPublicKey(sk2);

        sk3 = new BLSPrivateKey(3);
        vk3 = new BLSPublicKey(sk3);

        sk4 = new BLSPrivateKey(4);
        vk4 = new BLSPublicKey(sk4);

        sk5 = new BLSPrivateKey(5);
        vk5 = new BLSPublicKey(sk5);

        sk6 = new BLSPrivateKey(6);
        vk6 = new BLSPublicKey(sk6);

        committeeBlock = new CommitteeBlock();
        committeeBlock.getHeaderData().setTimestamp("2022-11-18 15:01:29.304");
        committeeBlock.getStructureMap().get(0).put(vk1, IPFinder.getLocal_address());
        committeeBlock.getStructureMap().get(1).put(vk2, "192.168.1.112");
        committeeBlock.getStructureMap().get(2).put(vk3, "192.168.1.114");
        committeeBlock.getStructureMap().get(2).put(vk4, "192.168.1.116");
        committeeBlock.getStructureMap().get(3).put(vk5, "192.168.1.117");
        committeeBlock.getStructureMap().get(3).put(vk6, "192.168.1.118");
        CachedLatestBlocks.getInstance().setCommitteeBlock(committeeBlock);
        CachedZoneIndex.getInstance().setZoneIndexInternalIP();

        transactionBlock = new TransactionBlock();
        transactionBlock.setGeneration(1);
        transactionBlock.setHash("hash1");

        Receipt.ReceiptBlock receiptBlock1 = new Receipt.ReceiptBlock(1, 1, "1");
        Receipt.ReceiptBlock receiptBlock1a = new Receipt.ReceiptBlock(2, 6, "1a");
        Receipt.ReceiptBlock receiptBlock2 = new Receipt.ReceiptBlock(3, 2, "2");
        Receipt.ReceiptBlock receiptBlock3 = new Receipt.ReceiptBlock(4, 3, "3");
        //its wrong each block must be unique for each zone need changes
        Receipt receipt1 = new Receipt(0, 1, receiptBlock1, 1, null);
        Receipt receipt2 = new Receipt(0, 1, receiptBlock1a, 1, null);
        Receipt receipt3 = new Receipt(1, 1, receiptBlock2, 1, null);
        Receipt receipt4 = new Receipt(2, 1, receiptBlock2, 2, null);
        Receipt receipt5 = new Receipt(3, 1, receiptBlock3, 1, null);
        Receipt receipt6 = new Receipt(3, 1, receiptBlock3, 2, null);

        ArrayList<Receipt> list = new ArrayList<>();
        list.add(receipt1);
        list.add(receipt2);
        list.add(receipt3);
        list.add(receipt4);
        list.add(receipt5);
        list.add(receipt6);
        Map<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> map = list
                .stream()
                .collect(Collectors.groupingBy(Receipt::getZoneTo, Collectors.groupingBy(Receipt::getReceiptBlock)));
        OutBoundRelay outBoundRelay = new OutBoundRelay(map);
        transactionBlock.setOutbound(outBoundRelay);

        Map<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> inboundmap = list
                .stream()
                .collect(Collectors.groupingBy(Receipt::getZoneFrom, Collectors.groupingBy(Receipt::getReceiptBlock)));

        CachedLatestBlocks.getInstance().setTransactionBlock(transactionBlock);
    }

    @Test
    public void Test() throws InterruptedException {
        if (System.getenv("MAVEN_OPTS") != null) {
            return;
        }
        IAdrestusFactory factory = new AdrestusFactory();
        List<AdrestusTask> tasks = new java.util.ArrayList<>(List.of(factory.createSendReceiptTask()));
        ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
        tasks.stream().map(Worker::new).forEach(executor::execute);
        // All tasks were executed, now shutdown
        Thread.sleep(10000);
        tasks.forEach(val -> {
            try {
                if (val != null)
                    val.close();
            } catch (NullPointerException e) {
            }
        });
        tasks.clear();
        executor.shutdown();
        executor.shutdownNow();
        while (!executor.isTerminated()) {
            Thread.yield();
        }
    }
}
