package io.Adrestus.core.RingBuffer.handler.blocks;

import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.TransactionBlock;

public interface DisruptorBlockVisitor {
    public void visit(CommitteeBlock committeeBlock);
    public void visit(TransactionBlock transactionBlock);
}
