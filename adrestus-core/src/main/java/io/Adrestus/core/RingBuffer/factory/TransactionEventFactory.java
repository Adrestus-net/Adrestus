package io.Adrestus.core.RingBuffer.factory;

import com.lmax.disruptor.EventFactory;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;

public class TransactionEventFactory implements EventFactory<TransactionEvent> {
    public TransactionEvent newInstance() {
        return new TransactionEvent();
    }
}
