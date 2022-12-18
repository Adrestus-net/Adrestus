package io.Adrestus.core.RingBuffer.handler.transactions;

import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZoneEventHandler extends TransactionEventHandler {
    private static Logger LOG = LoggerFactory.getLogger(TimestampEventHandler.class);

    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        try {
            Transaction transaction = transactionEvent.getTransaction();
            if (transaction.getZoneFrom() != CachedZoneIndex.getInstance().getZoneIndex()) {
                LOG.info("Transaction abort: Transaction zone is not the same");
                transaction.setStatus(StatusType.ABORT);
            }
        } catch (NullPointerException ex) {
            LOG.info("Transaction is empty");
        }
    }
}
