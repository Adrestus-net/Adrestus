package io.Adrestus.core.RingBuffer.handler.blocks;

import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.Transaction;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.core.UnclaimedFeeRewardTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class ReplayFeeRewardEventHandler implements BlockEventHandler<AbstractBlockEvent> {
    private static Logger LOG = LoggerFactory.getLogger(ReplayFeeRewardEventHandler.class);

    @Override
    public void onEvent(AbstractBlockEvent blockEvent, long l, boolean b) throws Exception {
        TransactionBlock transactionBlock = (TransactionBlock) blockEvent.getBlock();
        if (transactionBlock.getTransactionList().isEmpty())
            return;
        try {
            UnclaimedFeeRewardTransaction transaction = (UnclaimedFeeRewardTransaction) transactionBlock.getTransactionList().get(0);
            BigDecimal sum = transactionBlock.getTransactionList().parallelStream().skip(1).map(Transaction::getAmountWithTransactionFee).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (sum.compareTo(transaction.getAmount()) != 0) {
                LOG.info("Leader FeeRewardTransaction is invalid abort");
                transactionBlock.setStatustype(StatusType.ABORT);
                return;
            }
        } catch (ClassCastException e) {
            LOG.info("First Transaction is invalid not an UnclaimedFeeRewardTransaction abort");
            transactionBlock.setStatustype(StatusType.ABORT);
            return;
        }
    }
}
