package io.Adrestus.core.RingBuffer.handler.blocks;

import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.core.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SumFeeRewardEventHandler implements BlockEventHandler<AbstractBlockEvent> {
    private static Logger LOG = LoggerFactory.getLogger(SumFeeRewardEventHandler.class);

    @Override
    public void onEvent(AbstractBlockEvent blockEvent, long l, boolean b) throws Exception {
        TransactionBlock transactionBlock = (TransactionBlock) blockEvent.getBlock();
        if (transactionBlock.getTransactionList().isEmpty())
            return;
        long count = transactionBlock.getTransactionList().stream().filter(val -> val.getType().equals(TransactionType.UNCLAIMED_FEE_REWARD)).count();
        if (count != 1) {
            LOG.info("Possible FeeRewardTransaction replay attack abort");
            transactionBlock.setStatustype(StatusType.ABORT);
            return;
        }
    }
}
