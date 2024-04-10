package io.Adrestus.core.RingBuffer.handler.receipts;

import io.Adrestus.core.ReceiptBlock;
import io.Adrestus.core.RingBuffer.event.ReceiptBlockEvent;
import io.Adrestus.core.StatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerationEventHandler implements ReceiptEventHandler<ReceiptBlockEvent> {

    private static Logger LOG = LoggerFactory.getLogger(GenerationEventHandler.class);

    @Override
    public void onEvent(ReceiptBlockEvent receiptBlockEvent, long l, boolean b) throws InterruptedException {
        ReceiptBlock receiptBlock = receiptBlockEvent.getReceiptBlock();
        if (receiptBlock.getTransactionBlock().getGeneration() != receiptBlock.getReceipt().getReceiptBlock().getGeneration()) {
            LOG.info("Receipt Generation are not valid with origin block abort");
            receiptBlockEvent.getReceiptBlock().setStatusType(StatusType.ABORT);
            return;
        }
    }
}
