package io.Adrestus.core.RingBuffer.handler.blocks;

import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomizedEventHandler implements BlockEventHandler<AbstractBlockEvent> {

    private static Logger LOG = LoggerFactory.getLogger(RandomizedEventHandler.class);

    @Override
    public void onEvent(AbstractBlockEvent blockEvent, long l, boolean b) throws Exception {
        CommitteeBlock block = (CommitteeBlock) blockEvent.getBlock();
    }
}
