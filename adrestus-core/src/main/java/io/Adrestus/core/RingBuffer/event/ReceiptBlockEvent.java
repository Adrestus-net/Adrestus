package io.Adrestus.core.RingBuffer.event;

import io.Adrestus.core.ReceiptBlock;

import java.util.Objects;

public class ReceiptBlockEvent implements Cloneable {
    private ReceiptBlock receiptBlock;

    public ReceiptBlockEvent(ReceiptBlock receiptBlock) {
        this.receiptBlock = receiptBlock;
    }

    public ReceiptBlockEvent() {
    }

    public ReceiptBlock getReceiptBlock() {
        return receiptBlock;
    }

    public void setReceiptBlock(ReceiptBlock receiptBlock) {
        this.receiptBlock = receiptBlock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReceiptBlockEvent that = (ReceiptBlockEvent) o;
        return Objects.equals(receiptBlock, that.receiptBlock);
    }

    @Override
    public int hashCode() {
        return Objects.hash(receiptBlock);
    }

    @Override
    public ReceiptBlockEvent clone() {
        try {
            ReceiptBlockEvent clone = (ReceiptBlockEvent) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
