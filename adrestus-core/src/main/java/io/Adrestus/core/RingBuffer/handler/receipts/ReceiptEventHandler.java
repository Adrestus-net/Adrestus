package io.Adrestus.core.RingBuffer.handler.receipts;

import com.lmax.disruptor.EventHandler;

public interface ReceiptEventHandler<ReceiptBlockEvent> extends EventHandler<ReceiptBlockEvent> {
    @Override
    default public void onEvent(ReceiptBlockEvent receiptBlockEvent, long l, boolean b) throws Exception {
    }
}
