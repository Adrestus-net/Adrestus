package io.Adrestus.core.RingBuffer.handler.receipts;

import io.Adrestus.core.ReceiptBlock;
import io.Adrestus.core.RingBuffer.event.ReceiptBlockEvent;
import io.Adrestus.core.StatusType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PublicKeyEventHandler implements ReceiptEventHandler<ReceiptBlockEvent> {

    private static Logger LOG = LoggerFactory.getLogger(PublicKeyEventHandler.class);

    @Override
    public void onEvent(ReceiptBlockEvent receiptBlockEvent, long l, boolean b) throws InterruptedException {
        ReceiptBlock receiptBlock = receiptBlockEvent.getReceiptBlock();
        boolean bool1 = StringUtils.equals(receiptBlock.getTransaction().getXAxis().toString(), receiptBlock.getReceipt().getTransaction().getXAxis().toString());
        boolean bool2 = StringUtils.equals(receiptBlock.getTransaction().getYAxis().toString(), receiptBlock.getReceipt().getTransaction().getYAxis().toString());
        if (!bool1 || !bool2) {
            LOG.info("Public keys are not equal abort");
            receiptBlockEvent.getReceiptBlock().setStatusType(StatusType.ABORT);
            return;
        }
    }
}
