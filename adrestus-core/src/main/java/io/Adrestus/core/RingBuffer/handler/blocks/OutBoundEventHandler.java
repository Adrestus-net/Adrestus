package io.Adrestus.core.RingBuffer.handler.blocks;

import io.Adrestus.Trie.MerkleNode;
import io.Adrestus.Trie.MerkleTreeOptimizedImp;
import io.Adrestus.core.Receipt;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.Transaction;
import io.Adrestus.core.TransactionBlock;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class OutBoundEventHandler implements BlockEventHandler<AbstractBlockEvent> {
    private static Logger LOG = LoggerFactory.getLogger(OutBoundEventHandler.class);
    private AtomicInteger atomicInteger;
    @Override
    public void onEvent(AbstractBlockEvent blockEvent, long l, boolean b) throws Exception {
        TransactionBlock transactionBlock = (TransactionBlock) blockEvent.getBlock();
        TransactionBlock transactionBlockclonable = (TransactionBlock) transactionBlock.clone();
        final LinkedHashMap<Integer, LinkedHashMap<Receipt.ReceiptBlock, List<Receipt>>> outer_receipts = transactionBlockclonable.getOutbound().getMap_receipts();
        if (transactionBlockclonable.getOutbound().getMap_receipts().isEmpty())
            return;

        if (outer_receipts.size() > 3) {
            LOG.info("Size of zone is invalid");
            transactionBlock.setStatustype(StatusType.ABORT);
            return;
        }
        outer_receipts.values().forEach(val -> val.values().stream().forEach(col -> col.stream().forEach(rcp -> {
            if (rcp.getZoneFrom() != CachedZoneIndex.getInstance().getZoneIndex()) {
                LOG.info("Sender zone is invalid");
                transactionBlock.setStatustype(StatusType.ABORT);
                return;
            }
        })));

        Collections.sort(transactionBlockclonable.getTransactionList());
        ExecutorService service = Executors.newFixedThreadPool(3);
        Set<Integer> keyset = outer_receipts.keySet();


        atomicInteger = new AtomicInteger(3);
        CountDownLatch latch = new CountDownLatch(3);
        try {
            Map<Receipt.ReceiptBlock, List<Receipt>> zone_1 = outer_receipts.get(keyset.toArray()[0]);
            service.submit(() -> {
                ServiceSubmit(zone_1, transactionBlockclonable);
                latch.countDown();
            });
        } catch (Exception e) {
            latch.countDown();
        }
        try {
            Map<Receipt.ReceiptBlock, List<Receipt>> zone_2 = outer_receipts.get(keyset.toArray()[1]);
            service.submit(() -> {
                ServiceSubmit(zone_2, transactionBlockclonable);
                latch.countDown();
            });
        } catch (Exception e) {
            latch.countDown();
        }
        try {
            Map<Receipt.ReceiptBlock, List<Receipt>> zone_3 = outer_receipts.get(keyset.toArray()[2]);
            service.submit(() -> {
                ServiceSubmit(zone_3, transactionBlockclonable);
                latch.countDown();
            });
        } catch (Exception e) {
            latch.countDown();
        }
        latch.await();
        service.shutdownNow();
        service = null;
        if (atomicInteger.get() != 3) {
            LOG.info("Validation check of Outbound list is invalid abort");
            transactionBlock.setStatustype(StatusType.ABORT);
            return;
        }
    }

    public void ServiceSubmit(Map<Receipt.ReceiptBlock, List<Receipt>> zone, TransactionBlock transactionBlock) {
        for (Map.Entry<Receipt.ReceiptBlock, List<Receipt>> entry : zone.entrySet()) {
            entry.getValue().stream().forEach(receipt -> {
                Transaction transaction = transactionBlock.getTransactionList().get(receipt.getPosition());
                final MerkleTreeOptimizedImp outer_tree = new MerkleTreeOptimizedImp();
                final ArrayList<MerkleNode> merkleNodeArrayList = new ArrayList<>();
                transactionBlock.getTransactionList().forEach(val -> merkleNodeArrayList.add(new MerkleNode(val.getHash())));
                outer_tree.constructTree(merkleNodeArrayList);
                boolean check = PreconditionsChecks(receipt, entry.getKey(), outer_tree, transactionBlock, transaction, receipt.getPosition());
                if (!check)
                    atomicInteger.decrementAndGet();
                outer_tree.clear();
            });
        }
    }
    public boolean PreconditionsChecks(final Receipt receipt, final Receipt.ReceiptBlock receiptBlock, final MerkleTreeOptimizedImp outer_tree, final TransactionBlock transactionBlock, Transaction transaction, int index) {
        String root = outer_tree.generateRoot(receipt.getProofs());
        boolean bool3 = StringUtils.equals(transactionBlock.getMerkleRoot(), root);
        boolean bool5 = StringUtils.equals(receiptBlock.getOutboundMerkleRoot(), root);
        int val3 = Integer.compare(index, receipt.getPosition());
        int val4 = Integer.compare(transactionBlock.getHeight(), receiptBlock.getHeight());
        int val5 = Integer.compare(transactionBlock.getGeneration(), receiptBlock.getGeneration());
        int val6 = Integer.compare(receipt.getZoneFrom(), CachedZoneIndex.getInstance().getZoneIndex());

        if (val3 == 0 && val4 == 0.0 && val5 == 0 && val6 == 0 && bool3 && bool5)
            return true;
        return false;
    }
}
