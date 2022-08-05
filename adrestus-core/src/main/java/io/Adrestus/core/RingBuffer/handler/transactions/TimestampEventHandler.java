package io.Adrestus.core.RingBuffer.handler.transactions;

import com.lmax.disruptor.EventHandler;
import io.Adrestus.core.Resourses.MemoryPool;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import io.Adrestus.core.Transaction;
import io.Adrestus.core.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimestampEventHandler implements EventHandler<TransactionEvent> {
    private static Logger LOG = LoggerFactory.getLogger(TimestampEventHandler.class);
    private final MemoryPool memoryPool;

    public TimestampEventHandler(MemoryPool memoryPool) {
        this.memoryPool = memoryPool;
    }

    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        try {
            Transaction transaction = transactionEvent.getTransaction();
            if (memoryPool.checkTimestamp(transaction)) {
                LOG.info("Transaction abort Transaction with older timestamp exists for this source address");
                transaction.setStatus(TransactionStatus.ABORT);
            }
        } catch (NullPointerException ex) {
            LOG.info("Transaction is empty");
        }
    }
}
