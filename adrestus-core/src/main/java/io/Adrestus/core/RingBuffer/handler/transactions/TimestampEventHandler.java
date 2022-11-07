package io.Adrestus.core.RingBuffer.handler.transactions;

import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.Transaction;
import io.Adrestus.util.GetTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;

public class TimestampEventHandler extends TransactionEventHandler {
    private static Logger LOG = LoggerFactory.getLogger(TimestampEventHandler.class);

    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        try {
            Transaction transaction = transactionEvent.getTransaction();
            Timestamp current = GetTime.GetTimeStamp();
            if (current.before(GetTime.GetTimestampFromString(transaction.getTimestamp()))) {
                LOG.info("Transaction abort: Transaction timestamp is not a valid timestamp");
                transaction.setStatus(StatusType.ABORT);
            }
        } catch (NullPointerException ex) {
            LOG.info("Transaction is empty");
        }
    }
}
