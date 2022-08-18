package io.Adrestus.core.RingBuffer.event;

import io.Adrestus.core.AbstractBlock;

public  class AbstractBlockEvent {
    private AbstractBlock block;


    public void setBlock(AbstractBlock block) {
        this.block = block;
    }

    public AbstractBlock getBlock() {
        return block;
    }

}
