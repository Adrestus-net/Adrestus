package io.Adrestus.core.RingBuffer.handler.receipts;

import io.Adrestus.core.RingBuffer.event.ReceiptBlockEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReceiptClearingEventHandler implements ReceiptEventHandler<ReceiptBlockEvent> {

    private static Logger LOG = LoggerFactory.getLogger(ReceiptClearingEventHandler.class);

    @Override
    public void onEvent(ReceiptBlockEvent receiptBlockEvent, long l, boolean b) throws InterruptedException {
        receiptBlockEvent.clear();
    }
}
