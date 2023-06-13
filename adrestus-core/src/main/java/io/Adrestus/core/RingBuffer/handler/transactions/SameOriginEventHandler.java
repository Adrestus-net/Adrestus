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

            if (transaction.getStatus().equals(StatusType.BUFFERED))
                return;

            if (MemoryTransactionPool.getInstance().checkAdressExists(transaction)) {
                CacheTemporalTransactionPool.getInstance().add(transaction);
                transaction.setStatus(StatusType.BUFFERED);
            }
            PatriciaTreeNode patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(transaction.getFrom()).get();

            if (transaction.getNonce() == patriciaTreeNode.getNonce() + 1)
                return;
            else if(transaction.getNonce() == patriciaTreeNode.getNonce()) {
                transaction.setStatus(StatusType.BUFFERED);
                return;
            }

            CacheTemporalTransactionPool.getInstance().add(transaction);
            transaction.setStatus(StatusType.BUFFERED);
            return;
        } catch (NullPointerException ex) {
            LOG.info("Transaction is empty");
        }
    }


}
