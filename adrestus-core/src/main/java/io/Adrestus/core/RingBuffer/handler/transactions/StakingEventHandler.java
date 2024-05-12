package io.Adrestus.core.RingBuffer.handler.transactions;

import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.Trie.StakingInfo;
import io.Adrestus.core.*;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;

public class StakingEventHandler extends TransactionEventHandler implements TransactionUnitVisitor {
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
        StakingInfo stakingInfo = null;
        try {

            stakingInfo = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(stakingTransaction.getValidatorAddress()).get().getStakingInfo();

        } catch (NoSuchElementException ex) {
            LOG.info("State trie is empty we add address");
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(stakingTransaction.getValidatorAddress(), new PatriciaTreeNode(0, 0));
            stakingInfo = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(stakingTransaction.getValidatorAddress()).get().getStakingInfo();
        } catch (NullPointerException ex) {
            LOG.info("Transaction is empty");
            stakingTransaction.setStatus(StatusType.ABORT);
            return;
        }

        if(stakingInfo.getDetails().isEmpty() || stakingInfo.getIdentity().isEmpty() || stakingInfo.getName().isEmpty() ||stakingInfo.getWebsite().isEmpty() || stakingInfo.getCommissionRate()==0) {
            if (stakingTransaction.getDetails().isEmpty() || stakingTransaction.getIdentity().isEmpty() || stakingTransaction.getName().isEmpty() || stakingTransaction.getWebsite().isEmpty() || stakingTransaction.getCommissionRate() == 0) {
                LOG.info("Staking Transaction information is incorrect please add them before make make transaction abort");
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
}
