package io.Adrestus.core.RingBuffer.handler.transactions;

import com.lmax.disruptor.EventHandler;
import io.Adrestus.core.RewardsTransaction;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import io.Adrestus.core.Transaction;
import io.Adrestus.core.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RewardEventHandler implements EventHandler<TransactionEvent> {
    private static Logger LOG = LoggerFactory.getLogger(RewardEventHandler.class);

    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        try {
            Transaction transaction = transactionEvent.getTransaction();
            if (transaction instanceof RewardsTransaction) {
                RewardsTransaction rewardsTransaction = (RewardsTransaction) transaction;
                if (rewardsTransaction.getRecipientAddress().length() != 53) {
                    LOG.info("RecipientAddress addresses is invalid please check again");
                    transaction.setStatus(TransactionStatus.ABORT);
                }
            }

        } catch (NullPointerException ex) {
            LOG.info("Transaction is empty");
        }
    }

}
