package io.Adrestus.core.RingBuffer.handler.transactions;

import io.Adrestus.core.*;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TypeEventHandler extends TransactionEventHandler implements TransactionUnitVisitor {
    private static Logger LOG = LoggerFactory.getLogger(TypeEventHandler.class);

    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        Transaction transaction = transactionEvent.getTransaction();
        transaction.accept(this);
    }

    @Override
    public void visit(RegularTransaction regularTransaction) {
        if (!regularTransaction.getType().equals(TransactionType.REGULAR)) {
            LOG.info("RegularTransaction type is invalid abort");
            regularTransaction.setStatus(StatusType.ABORT);
        }
    }

    @Override
    public void visit(RewardsTransaction rewardsTransaction) {
        if (!rewardsTransaction.getType().equals(TransactionType.REWARDS)) {
            LOG.info("RewardsTransaction type is invalid abort");
            rewardsTransaction.setStatus(StatusType.ABORT);
        }
    }

    @Override
    public void visit(StakingTransaction stakingTransaction) {
        if (!stakingTransaction.getType().equals(TransactionType.STAKING)) {
            LOG.info("StakingTransaction type is invalid abort");
            stakingTransaction.setStatus(StatusType.ABORT);
        }
    }

    @Override
    public void visit(DelegateTransaction delegateTransaction) {
        if (!delegateTransaction.getType().equals(TransactionType.DELEGATE)) {
            LOG.info("DelegateTransaction type is invalid abort");
            delegateTransaction.setStatus(StatusType.ABORT);
        }
    }

    @Override
    public void visit(UnclaimedFeeRewardTransaction unclaimedFeeRewardTransaction) {
        if (!unclaimedFeeRewardTransaction.getType().equals(TransactionType.UNCLAIMED_FEE_REWARD)) {
            LOG.info("UnclaimedFeeRewardTransaction type is invalid abort");
            unclaimedFeeRewardTransaction.setStatus(StatusType.ABORT);
        }
    }

    @Override
    public void visit(UnDelegateTransaction unDelegateTransaction) {
        if (!unDelegateTransaction.getType().equals(TransactionType.UNDELEGATE)) {
            LOG.info("UndelegatingTransaction type is invalid abort");
            unDelegateTransaction.setStatus(StatusType.ABORT);
        }
    }

    @Override
    public void visit(UnstakingTransaction unstakingTransaction) {
        if (!unstakingTransaction.getType().equals(TransactionType.UNSTAKING)) {
            LOG.info("UnstakingTransaction type is invalid abort");
            unstakingTransaction.setStatus(StatusType.ABORT);
        }
    }
}
