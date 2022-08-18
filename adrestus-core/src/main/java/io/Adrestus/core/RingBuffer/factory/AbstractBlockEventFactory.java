package io.Adrestus.core.RingBuffer.factory;

import com.lmax.disruptor.EventFactory;
import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;

public class AbstractBlockEventFactory implements EventFactory<AbstractBlockEvent> {
    @Override
    public AbstractBlockEvent newInstance() {
        return new AbstractBlockEvent();
    }
}
