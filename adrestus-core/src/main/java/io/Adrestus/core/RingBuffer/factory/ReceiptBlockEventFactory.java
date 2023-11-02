package io.Adrestus.core.RingBuffer.factory;

import com.lmax.disruptor.EventFactory;
import io.Adrestus.core.RingBuffer.event.ReceiptBlockEvent;

public class ReceiptBlockEventFactory implements EventFactory<ReceiptBlockEvent> {
    public ReceiptBlockEvent newInstance() {
        return new ReceiptBlockEvent();
    }
}
