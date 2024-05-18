package io.Adrestus.core.RingBuffer.handler.transactions;

import io.Adrestus.core.Resourses.MemoryTransactionPool;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class ReplayEventHandler extends TransactionEventHandler {
    private static Logger LOG = LoggerFactory.getLogger(ReplayEventHandler.class);

    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        try {
            Transaction transaction = transactionEvent.getTransaction();

            if (transaction.getStatus().equals(StatusType.BUFFERED) || transaction.getStatus().equals(StatusType.ABORT))
                return;


            if (MemoryTransactionPool.getInstance().checkTimestamp(transaction)) {
                Optional.of("Transaction abort Transaction with older timestamp exists for this source address").ifPresent(val -> {
                    LOG.info(val);
                    transaction.infos(val);
                });
                transaction.setStatus(StatusType.ABORT);
            }
        } catch (NullPointerException ex) {
            LOG.info("Transaction is empty");
        }
    }
}
