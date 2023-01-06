package io.Adrestus.core.RingBuffer.handler.blocks;

import io.Adrestus.MemoryTreePool;
import io.Adrestus.TreeFactory;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.Transaction;
import io.Adrestus.core.TransactionBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatriciaTreeEventHandler implements BlockEventHandler<AbstractBlockEvent> {
    private static Logger LOG = LoggerFactory.getLogger(PatriciaTreeEventHandler.class);

    @Override
    public void onEvent(AbstractBlockEvent blockEvent, long l, boolean b) throws Exception {
        TransactionBlock transactionBlock = (TransactionBlock) blockEvent.getBlock();
        MemoryTreePool replica= (MemoryTreePool) ((MemoryTreePool) TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex())).clone();

        if (!transactionBlock.getTransactionList().isEmpty()) {
            for (int i = 0; i < transactionBlock.getTransactionList().size(); i++) {
                Transaction transaction = transactionBlock.getTransactionList().get(i);
                if ((transaction.getZoneFrom() == CachedZoneIndex.getInstance().getZoneIndex()) && (transaction.getZoneTo() == CachedZoneIndex.getInstance().getZoneIndex())) {
                    replica.withdrawReplica(transaction.getFrom(), transaction.getAmount(), replica);
                    replica.depositReplica(transaction.getTo(), transaction.getAmount(), replica);
                } else {
                    replica.withdrawReplica(transaction.getFrom(), transaction.getAmount(), replica);
                }
            }
        }

        if (!transactionBlock.getInbound().getMap_receipts().isEmpty())
            transactionBlock
                    .getInbound()
                    .getMap_receipts()
                    .get(transactionBlock.getInbound().getMap_receipts().keySet().toArray()[0])
                    .entrySet()
                    .stream()
                    .forEach(entry -> {
                        entry.getValue().stream().forEach(receipt -> {
                            replica.depositReplica(receipt.getAddress(), receipt.getAmount(), replica);
                        });

                    });

        if (!replica.getRootHash().equals(transactionBlock.getPatriciaMerkleRoot())) {
            LOG.info("Patricia Merkle root is invalid abort");
            transactionBlock.setStatustype(StatusType.ABORT);
            return;
        }
    }
}
