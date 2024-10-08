package io.Adrestus.core.RingBuffer.handler.transactions;

import io.Adrestus.config.RewardConfiguration;
import io.Adrestus.core.*;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Optional;

public class TransactionFeeEventHandler extends TransactionEventHandler implements TransactionUnitVisitor {
    private static Logger LOG = LoggerFactory.getLogger(TransactionFeeEventHandler.class);
    private static final BigDecimal FEES = BigDecimal.valueOf(10.0);
    private static final BigDecimal PERCENT = BigDecimal.valueOf(100.0);

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
        if (regularTransaction.getAmountWithTransactionFee().compareTo(FEES.multiply(regularTransaction.getAmount()).divide(PERCENT, RewardConfiguration.DECIMAL_PRECISION,RewardConfiguration.ROUNDING))!=0) {
            Optional.of("RegularTransaction fee calculator is incorrect").ifPresent(val -> {
                LOG.info(val);
                regularTransaction.infos(val);
            });
            regularTransaction.setStatus(StatusType.ABORT);
        }
    }

    @Override
    public void visit(RewardsTransaction rewardsTransaction) {

    }

    @Override
    public void visit(StakingTransaction stakingTransaction) {
        if (stakingTransaction.getAmountWithTransactionFee().compareTo(FEES.multiply(stakingTransaction.getAmount()).divide(PERCENT, RewardConfiguration.DECIMAL_PRECISION,RewardConfiguration.ROUNDING))!=0) {
            Optional.of("StakingTransaction fee calculator is incorrect").ifPresent(val -> {
                LOG.info(val);
                stakingTransaction.infos(val);
            });
            stakingTransaction.setStatus(StatusType.ABORT);
        }
    }

    @Override
    public void visit(DelegateTransaction delegateTransaction) {
        if (delegateTransaction.getAmountWithTransactionFee().compareTo(FEES.multiply(delegateTransaction.getAmount()).divide(PERCENT, RewardConfiguration.DECIMAL_PRECISION,RewardConfiguration.ROUNDING))!=0) {
            Optional.of("DelegateTransaction fee calculator is incorrect").ifPresent(val -> {
                LOG.info(val);
                delegateTransaction.infos(val);
            });
            delegateTransaction.setStatus(StatusType.ABORT);
        }
    }

    @Override
    public void visit(UnclaimedFeeRewardTransaction unclaimedFeeRewardTransaction) {

    }

    @Override
    public void visit(UnDelegateTransaction unDelegateTransaction) {
        if (unDelegateTransaction.getAmountWithTransactionFee().compareTo(FEES.multiply(unDelegateTransaction.getAmount()).divide(PERCENT, RewardConfiguration.DECIMAL_PRECISION,RewardConfiguration.ROUNDING))!=0) {
            Optional.of("UnDelegateTransaction fee calculator is incorrect").ifPresent(val -> {
                LOG.info(val);
                unDelegateTransaction.infos(val);
            });
            unDelegateTransaction.setStatus(StatusType.ABORT);
        }
    }

    @Override
    public void visit(UnstakingTransaction unstakingTransaction) {
        if (unstakingTransaction.getAmountWithTransactionFee().compareTo(FEES.multiply(unstakingTransaction.getAmount()).divide(PERCENT, RewardConfiguration.DECIMAL_PRECISION,RewardConfiguration.ROUNDING))!=0) {
            Optional.of("Unstaking Transaction fee calculator is incorrect").ifPresent(val -> {
                LOG.info(val);
                unstakingTransaction.infos(val);
            });
            unstakingTransaction.setStatus(StatusType.ABORT);
        }
    }
}
