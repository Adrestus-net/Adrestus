package io.Adrestus.core.RingBuffer.handler.blocks;

import io.Adrestus.Trie.MerkleNode;
import io.Adrestus.Trie.MerkleTreeImp;
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

    @Override
    public void onEvent(AbstractBlockEvent blockEvent, long l, boolean b) throws Exception {
        TransactionBlock transactionBlock = (TransactionBlock) blockEvent.getBlock();
        final LinkedHashMap<Integer, LinkedHashMap<Receipt.ReceiptBlock, List<Receipt>>> outer_receipts = transactionBlock.getOutbound().getMap_receipts();
        if (transactionBlock.getOutbound().getMap_receipts().isEmpty())
            return;

        if (outer_receipts.size() > 3) {
            LOG.info("Size of zone is invalid");
            transactionBlock.setStatustype(StatusType.ABORT);
            return;
        }
        for (Integer key : outer_receipts.keySet()) {
            if (key == CachedZoneIndex.getInstance().getZoneIndex()) {
                LOG.info("Sender zone is invalid");
                transactionBlock.setStatustype(StatusType.ABORT);
                return;
            }
        }

        Collections.sort(transactionBlock.getTransactionList());
        ExecutorService service = Executors.newFixedThreadPool(3);
        Set<Integer> keyset = outer_receipts.keySet();


        AtomicInteger atomicInteger = new AtomicInteger(3);
        CountDownLatch latch = new CountDownLatch(3);
        try {
            Map<Receipt.ReceiptBlock, List<Receipt>> zone_1 = outer_receipts.get(keyset.toArray()[0]);
            service.submit(() -> {
                for (Map.Entry<Receipt.ReceiptBlock, List<Receipt>> entry : zone_1.entrySet()) {
                    entry.getValue().stream().forEach(receipt -> {
                        int index = Collections.binarySearch(transactionBlock.getTransactionList(), receipt.getTransaction());
                        Transaction transaction = transactionBlock.getTransactionList().get(index);
                        boolean check = PreconditionsChecks(receipt, entry.getKey(), transactionBlock, transaction, index);
                        if (!check)
                            atomicInteger.decrementAndGet();
                    });
                }

            });
        } catch (Exception e) {
        } finally {
            latch.countDown();
        }
        try {
            Map<Receipt.ReceiptBlock, List<Receipt>> zone_2 = outer_receipts.get(keyset.toArray()[1]);
            service.submit(() -> {
                for (Map.Entry<Receipt.ReceiptBlock, List<Receipt>> entry : zone_2.entrySet()) {
                    entry.getValue().stream().forEach(receipt -> {
                        int index = Collections.binarySearch(transactionBlock.getTransactionList(), receipt.getTransaction());
                        Transaction transaction = transactionBlock.getTransactionList().get(index);
                        boolean check = PreconditionsChecks(receipt, entry.getKey(), transactionBlock, transaction, index);
                        if (!check)
                            atomicInteger.decrementAndGet();
                    });
                }
            });
        } catch (Exception e) {
        } finally {
            latch.countDown();
        }
        try {
            Map<Receipt.ReceiptBlock, List<Receipt>> zone_3 = outer_receipts.get(keyset.toArray()[2]);
            service.submit(() -> {
                for (Map.Entry<Receipt.ReceiptBlock, List<Receipt>> entry : zone_3.entrySet()) {
                    entry.getValue().stream().forEach(receipt -> {
                        int index = Collections.binarySearch(transactionBlock.getTransactionList(), receipt.getTransaction());
                        Transaction transaction = transactionBlock.getTransactionList().get(index);
                        boolean check = PreconditionsChecks(receipt, entry.getKey(), transactionBlock, transaction, index);
                        if (!check)
                            atomicInteger.decrementAndGet();
                    });
                }
            });
        } catch (Exception e) {
        } finally {
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
      /*  for (int i = 0; i < transactionBlock.getOutbound().getReceipt_list().size(); i++) {
            //int index = Collections.binarySearch(transactionBlock.getTransactionList(), transactionBlock.getOutbound().getOutbound().get(i).);
        }
        ExecutorService service = Executors.newFixedThreadPool(2);
        CountDownLatch latch=new CountDownLatch(1);
        service.submit(()->{
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        service.submit(()->{
            LinkedHashMap<BLSPublicKey,String> map= CachedLatestBlocks
                    .getInstance()
                    .getCommitteeBlock()
                    .getStructureMap()
                    .get(CachedZoneIndex.getInstance().getZoneIndex());
            latch.countDown();
        });*/
    }

    public boolean PreconditionsChecks(final Receipt receipt, final Receipt.ReceiptBlock receiptBlock, final TransactionBlock transactionBlock, Transaction transaction, int index) {
        final MerkleTreeImp outer_tree = new MerkleTreeImp();
        final ArrayList<MerkleNode> merkleNodeArrayList = new ArrayList<>();
        transactionBlock.getTransactionList().forEach(val -> merkleNodeArrayList.add(new MerkleNode(val.getHash())));
        outer_tree.my_generate2(merkleNodeArrayList);
        boolean bool1 = StringUtils.equals(transaction.getHash(), receipt.getTransaction().getHash());
        boolean bool2 = StringUtils.equals(transaction.getTo(), receipt.getAddress());
        boolean bool3 = StringUtils.equals(transactionBlock.getMerkleRoot(), outer_tree.GenerateRoot(receipt.getProofs()));
        boolean bool4 = StringUtils.equals(transactionBlock.getHash(), receiptBlock.getBlock_hash());
        boolean bool5 = StringUtils.equals(receiptBlock.getOutboundMerkleRoot(), outer_tree.getRootHash());
        double val2 = Double.compare(transaction.getAmount(), receipt.getAmount());
        int val3 = Integer.compare(index, receipt.getPosition());
        int val4 = Integer.compare(transactionBlock.getHeight(), receiptBlock.getHeight());
        int val5 = Integer.compare(transactionBlock.getGeneration(), receiptBlock.getGeneration());
        int val6 = Integer.compare(receipt.getZoneFrom(), CachedZoneIndex.getInstance().getZoneIndex());

        if (val2 == 0.0 || val3 == 0 || val4 == 0.0 || val5 == 0 || val6 == 0 || bool1 || bool2 || bool3 || bool4 || bool5)
            return true;
        return false;
    }
}
