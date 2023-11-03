package io.Adrestus.core.RingBuffer.handler.receipts;

import io.Adrestus.core.ReceiptBlock;
import io.Adrestus.core.Resourses.MemoryReceiptPool;
import io.Adrestus.core.RingBuffer.event.ReceiptBlockEvent;
import io.Adrestus.core.StatusType;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReceiptInsertEventHandler implements ReceiptEventHandler<ReceiptBlockEvent> {

    private static Logger LOG = LoggerFactory.getLogger(ReceiptInsertEventHandler.class);

    @SneakyThrows
    @Override
    public void onEvent(ReceiptBlockEvent receiptBlockEvent, long l, boolean b) throws Exception {
        ReceiptBlock receiptBlock = receiptBlockEvent.getReceiptBlock();
        if (receiptBlock.getStatusType() != StatusType.ABORT) {
            MemoryReceiptPool.getInstance().add(receiptBlock.getReceipt());
        }
    }
}
