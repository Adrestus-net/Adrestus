package io.Adrestus.core.RingBuffer.handler.transactions;

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
            if (MemoryTransactionPool.getInstance().checkAdressExists(transaction)) {
                LOG.info("Transaction abort Address already submit a pending transaction in MemoryPool");
                transaction.setStatus(StatusType.ABORT);
            }
        } catch (NullPointerException ex) {
            LOG.info("Transaction is empty");
        }
    }


}
