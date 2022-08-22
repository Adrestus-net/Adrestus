package io.Adrestus.core.RingBuffer.handler.transactions;

import com.lmax.disruptor.EventHandler;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;

public abstract class TransactionEventHandler implements EventHandler<TransactionEvent> {
    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {

    }
}
