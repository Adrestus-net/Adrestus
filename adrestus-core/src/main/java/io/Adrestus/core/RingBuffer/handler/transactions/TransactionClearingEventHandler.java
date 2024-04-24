package io.Adrestus.core.RingBuffer.handler.transactions;

import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionClearingEventHandler extends TransactionEventHandler {
    private static Logger LOG = LoggerFactory.getLogger(TransactionClearingEventHandler.class);

    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        transactionEvent.clear();
    }
}
