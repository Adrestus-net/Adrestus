package io.Adrestus.core.RingBuffer.event;

import io.Adrestus.core.AbstractBlock;

import java.util.Objects;

public class AbstractBlockEvent {
    private AbstractBlock block;


    public AbstractBlockEvent() {
    }

    public AbstractBlockEvent(AbstractBlock block) {
        this.block = block;
    }

    public void setBlock(AbstractBlock block) {
        this.block = block;
    }

    public AbstractBlock getBlock() {
        return block;
    }


    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        AbstractBlockEvent that = (AbstractBlockEvent) object;
        return Objects.equals(block, that.block);
    }

    @Override
    public int hashCode() {
        return Objects.hash(block);
    }

    @Override
    public String toString() {
        return "AbstractBlockEvent{" +
                "block=" + block +
                '}';
    }


    public void clear() {
        this.block = null;
    }
}
