package io.Adrestus.core.RingBuffer.handler.transactions;

import io.Adrestus.core.*;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class TransactionFeeEventHandler extends TransactionEventHandler implements TransactionUnitVisitor {
    private static Logger LOG = LoggerFactory.getLogger(TransactionFeeEventHandler.class);
    private static final double FEES = 10.0;
    private static final double PERCENT = 100.0;

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
        if (regularTransaction.getAmountWithTransactionFee() != ((FEES / PERCENT) * regularTransaction.getAmount())) {
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
        if (stakingTransaction.getAmountWithTransactionFee() != ((FEES / PERCENT) * stakingTransaction.getAmount())) {
            Optional.of("StakingTransaction fee calculator is incorrect").ifPresent(val -> {
                LOG.info(val);
                stakingTransaction.infos(val);
            });
            stakingTransaction.setStatus(StatusType.ABORT);
        }
    }

    @Override
    public void visit(DelegateTransaction delegateTransaction) {
        if (delegateTransaction.getAmountWithTransactionFee() != ((FEES / PERCENT) * delegateTransaction.getAmount())) {
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
        if (unDelegateTransaction.getAmountWithTransactionFee() != ((FEES / PERCENT) * unDelegateTransaction.getAmount())) {
            Optional.of("UnDelegateTransaction fee calculator is incorrect").ifPresent(val -> {
                LOG.info(val);
                unDelegateTransaction.infos(val);
            });
            unDelegateTransaction.setStatus(StatusType.ABORT);
        }
    }

    @Override
    public void visit(UnstakingTransaction unstakingTransaction) {
        if (unstakingTransaction.getAmountWithTransactionFee() != ((FEES / PERCENT) * unstakingTransaction.getAmount())) {
            Optional.of("Unstaking Transaction fee calculator is incorrect").ifPresent(val -> {
                LOG.info(val);
                unstakingTransaction.infos(val);
            });
            unstakingTransaction.setStatus(StatusType.ABORT);
        }
    }
}
