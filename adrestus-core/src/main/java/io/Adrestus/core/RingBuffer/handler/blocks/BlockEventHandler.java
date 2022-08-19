package io.Adrestus.core.RingBuffer.handler.blocks;

import com.lmax.disruptor.EventHandler;

public interface BlockEventHandler<AbstractBlockEvent> extends EventHandler<AbstractBlockEvent> {
    @Override
    default public void onEvent(AbstractBlockEvent abstractblock, long l, boolean b) throws Exception {
    }
}
