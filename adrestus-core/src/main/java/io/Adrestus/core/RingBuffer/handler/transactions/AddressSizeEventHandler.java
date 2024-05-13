package io.Adrestus.core.RingBuffer.handler.transactions;

import io.Adrestus.core.*;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddressSizeEventHandler extends TransactionEventHandler implements TransactionUnitVisitor {
    private static Logger LOG = LoggerFactory.getLogger(AddressSizeEventHandler.class);

    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        try {
            Transaction transaction = transactionEvent.getTransaction();

            if (transaction.getStatus().equals(StatusType.BUFFERED) || transaction.getStatus().equals(StatusType.ABORT))
                return;

            transaction.accept(this);
        } catch (NullPointerException ex) {
            LOG.info("Transaction is empty");
        }
    }

    @Override
    public void visit(RegularTransaction regularTransaction) {
        if (regularTransaction.getFrom().length() != 53 || regularTransaction.getTo().length() != 53) {
            LOG.info("RegularTransaction addresses is invalid please check again");
            regularTransaction.setStatus(StatusType.ABORT);
        }
    }

    @Override
    public void visit(RewardsTransaction rewardsTransaction) {
        if (rewardsTransaction.getRecipientAddress().length() != 53) {
            LOG.info("RewardsTransaction addresses is invalid please check again");
            rewardsTransaction.setStatus(StatusType.ABORT);
        }
    }

    @Override
    public void visit(StakingTransaction stakingTransaction) {
        if (stakingTransaction.getValidatorAddress().length() != 53) {
            LOG.info("StakingTransaction addresses is invalid please check again");
            stakingTransaction.setStatus(StatusType.ABORT);
        }

    }

    @Override
    public void visit(DelegateTransaction delegateTransaction) {
        if (delegateTransaction.getValidatorAddress().length() != 53 || delegateTransaction.getDelegatorAddress().length() != 53) {
            LOG.info("DelegateTransaction addresses is invalid please check again");
            delegateTransaction.setStatus(StatusType.ABORT);
        }
    }

    @Override
    public void visit(UnclaimedFeeRewardTransaction unclaimedFeeRewardTransaction) {
        if (unclaimedFeeRewardTransaction.getRecipientAddress().length() != 53) {
            LOG.info("UnclaimedFeeRewardTransaction addresses is invalid please check again");
            unclaimedFeeRewardTransaction.setStatus(StatusType.ABORT);
        }
    }

    @Override
    public void visit(UnDelegateTransaction unDelegateTransaction) {
        if (unDelegateTransaction.getValidatorAddress().length() != 53 || unDelegateTransaction.getDelegatorAddress().length() != 53) {
            LOG.info("DelegateTransaction addresses is invalid please check again");
            unDelegateTransaction.setStatus(StatusType.ABORT);
        }
    }

    @Override
    public void visit(UnstakingTransaction unstakingTransaction) {
        if (unstakingTransaction.getValidatorAddress().length() != 53) {
            LOG.info("StakingTransaction addresses is invalid please check again");
            unstakingTransaction.setStatus(StatusType.ABORT);
        }
    }
}
