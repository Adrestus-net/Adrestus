package io.Adrestus.core.RingBuffer.handler.transactions;

import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.config.StakingConfiguration;
import io.Adrestus.core.*;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;
import java.util.Optional;

public class MinimumStakingEventHandler extends TransactionEventHandler implements TransactionUnitVisitor {
    private static Logger LOG = LoggerFactory.getLogger(StakingEventHandler.class);

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

    }

    @Override
    public void visit(RewardsTransaction rewardsTransaction) {

    }

    @Override
    public void visit(StakingTransaction stakingTransaction) {
        PatriciaTreeNode patriciaTreeNode = null;
        try {
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(stakingTransaction.getValidatorAddress()).get();

        } catch (NoSuchElementException ex) {
            LOG.info("State trie is empty we add ValidatorAddress address");
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(stakingTransaction.getValidatorAddress(), new PatriciaTreeNode(0, 0));
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(stakingTransaction.getValidatorAddress()).get();
        } catch (NullPointerException ex) {
            Optional.of("StakingTransaction is empty").ifPresent(val -> {
                LOG.info(val);
                stakingTransaction.infos(val);
            });
            stakingTransaction.setStatus(StatusType.ABORT);
            return;
        }

        if (patriciaTreeNode.getStaking_amount() < StakingConfiguration.MINIMUM_STAKING) {
            if (stakingTransaction.getAmount() < StakingConfiguration.MINIMUM_STAKING) {
                patriciaTreeNode.setStaking_amount(stakingTransaction.getAmount());
                Optional.of("StakingTransaction does not meet minimum requirements").ifPresent(val -> {
                    LOG.info(val);
                    stakingTransaction.infos(val);
                });
                stakingTransaction.setStatus(StatusType.ABORT);
            }
        } else {
            if (stakingTransaction.getAmount() <= 0) {
                Optional.of("StakingTransaction is amount is not sufficient").ifPresent(val -> {
                    LOG.info(val);
                    stakingTransaction.infos(val);
                });
                stakingTransaction.setStatus(StatusType.ABORT);
                return;
            }
        }
    }

    @Override
    public void visit(DelegateTransaction delegateTransaction) {

    }

    @Override
    public void visit(UnclaimedFeeRewardTransaction unclaimedFeeRewardTransaction) {

    }

    @Override
    public void visit(UnDelegateTransaction unDelegateTransaction) {

    }

    @Override
    public void visit(UnstakingTransaction unstakingTransaction) {

    }
}
