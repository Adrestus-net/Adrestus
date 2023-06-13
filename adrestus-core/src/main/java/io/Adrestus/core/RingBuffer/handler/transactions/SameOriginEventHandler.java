package io.Adrestus.core.RingBuffer.handler.transactions;

import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.core.Resourses.CacheTemporalTransactionPool;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.Resourses.MemoryTransactionPool;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SameOriginEventHandler extends TransactionEventHandler {
    private static Logger LOG = LoggerFactory.getLogger(SameOriginEventHandler.class);

    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        try {
            Transaction transaction = transactionEvent.getTransaction();

            if (transaction.getStatus().equals(StatusType.BUFFERED)|| transaction.getStatus().equals(StatusType.ABORT))
                return;

            if (MemoryTransactionPool.getInstance().checkAdressExists(transaction)) {
                transaction.setStatus(StatusType.BUFFERED);
                CacheTemporalTransactionPool.getInstance().add(transaction);
                return;
            }
            PatriciaTreeNode patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(transaction.getFrom()).get();

            if (transaction.getNonce() == patriciaTreeNode.getNonce() + 1)
                return;

            transaction.setStatus(StatusType.BUFFERED);
            CacheTemporalTransactionPool.getInstance().add(transaction);

        } catch (NullPointerException ex) {
            LOG.info("Transaction is empty");
        }
    }


}
