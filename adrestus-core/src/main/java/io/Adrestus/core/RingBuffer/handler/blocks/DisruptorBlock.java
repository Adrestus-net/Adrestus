package io.Adrestus.core.RingBuffer.handler.blocks;

public interface DisruptorBlock {
    public void accept(DisruptorBlockVisitor disruptorBlockVisitor);
}
