package io.Adrestus.core.RingBuffer.handler.transactions;

import io.Adrestus.core.*;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

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
            Optional.of("RegularTransaction type is invalid abort").ifPresent(val -> {
                LOG.info(val);
                regularTransaction.infos(val);
            });
            regularTransaction.setStatus(StatusType.ABORT);
        }
    }

    @Override
    public void visit(RewardsTransaction rewardsTransaction) {
        if (!rewardsTransaction.getType().equals(TransactionType.REWARDS)) {
            Optional.of("RewardsTransaction type is invalid abort").ifPresent(val -> {
                LOG.info(val);
                rewardsTransaction.infos(val);
            });
            rewardsTransaction.setStatus(StatusType.ABORT);
        }
    }

    @Override
    public void visit(StakingTransaction stakingTransaction) {
        if (!stakingTransaction.getType().equals(TransactionType.STAKING)) {
            Optional.of("StakingTransaction type is invalid abort").ifPresent(val -> {
                LOG.info(val);
                stakingTransaction.infos(val);
            });
            stakingTransaction.setStatus(StatusType.ABORT);
        }
    }

    @Override
    public void visit(DelegateTransaction delegateTransaction) {
        if (!delegateTransaction.getType().equals(TransactionType.DELEGATE)) {
            Optional.of("DelegateTransaction type is invalid abort").ifPresent(val -> {
                LOG.info(val);
                delegateTransaction.infos(val);
            });
            delegateTransaction.setStatus(StatusType.ABORT);
        }
    }

    @Override
    public void visit(UnclaimedFeeRewardTransaction unclaimedFeeRewardTransaction) {
        if (!unclaimedFeeRewardTransaction.getType().equals(TransactionType.UNCLAIMED_FEE_REWARD)) {
            Optional.of("UnclaimedFeeRewardTransaction type is invalid abort").ifPresent(val -> {
                LOG.info(val);
                unclaimedFeeRewardTransaction.infos(val);
            });
            unclaimedFeeRewardTransaction.setStatus(StatusType.ABORT);
        }
    }

    @Override
    public void visit(UnDelegateTransaction unDelegateTransaction) {
        if (!unDelegateTransaction.getType().equals(TransactionType.UNDELEGATE)) {
            Optional.of("UndelegatingTransaction type is invalid abort").ifPresent(val -> {
                LOG.info(val);
                unDelegateTransaction.infos(val);
            });
            unDelegateTransaction.setStatus(StatusType.ABORT);
        }
    }

    @Override
    public void visit(UnstakingTransaction unstakingTransaction) {
        if (!unstakingTransaction.getType().equals(TransactionType.UNSTAKING)) {
            Optional.of("UnstakingTransaction type is invalid abort").ifPresent(val -> {
                LOG.info(val);
                unstakingTransaction.infos(val);
            });
            unstakingTransaction.setStatus(StatusType.ABORT);
        }
    }
}
