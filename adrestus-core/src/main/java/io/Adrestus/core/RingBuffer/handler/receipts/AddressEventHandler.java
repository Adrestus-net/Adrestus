package io.Adrestus.core.RingBuffer.handler.receipts;

import io.Adrestus.core.ReceiptBlock;
import io.Adrestus.core.RingBuffer.event.ReceiptBlockEvent;
import io.Adrestus.core.StatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddressEventHandler implements ReceiptEventHandler<ReceiptBlockEvent> {

    private static Logger LOG = LoggerFactory.getLogger(AddressEventHandler.class);

    @Override
    public void onEvent(ReceiptBlockEvent receiptBlockEvent, long l, boolean b) throws InterruptedException {
        ReceiptBlock receiptBlock = receiptBlockEvent.getReceiptBlock();
        if (!receiptBlock.getTransaction().getTo().equals(receiptBlock.getReceipt().getAddress()) || !receiptBlock.getTransaction().getFrom().equals(receiptBlock.getReceipt().getTransaction().getFrom()) || !receiptBlock.getTransaction().getTo().equals(receiptBlock.getReceipt().getTransaction().getTo())) {
            LOG.info("Receipt Address is not valid with transaction abort");
            receiptBlockEvent.getReceiptBlock().setStatusType(StatusType.ABORT);
            return;
        }
    }
}
