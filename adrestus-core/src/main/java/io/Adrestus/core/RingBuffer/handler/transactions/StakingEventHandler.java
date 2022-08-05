package io.Adrestus.core.RingBuffer.handler.transactions;

import com.lmax.disruptor.EventHandler;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import io.Adrestus.core.StakingTransaction;
import io.Adrestus.core.Transaction;
import io.Adrestus.core.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StakingEventHandler implements EventHandler<TransactionEvent> {
    private static Logger LOG = LoggerFactory.getLogger(StakingEventHandler.class);

    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        try {
            Transaction transaction = transactionEvent.getTransaction();
            if (transaction instanceof StakingTransaction) {
                StakingTransaction stakingTransaction = (StakingTransaction) transaction;
                if (stakingTransaction.getValidatorAddress().length() != 53) {
                    LOG.info("ValidatorAddress addresses is invalid please check again");
                    transaction.setStatus(TransactionStatus.ABORT);
                }
            }
        } catch (NullPointerException ex) {
            LOG.info("Transaction is empty");
        }
    }
}
