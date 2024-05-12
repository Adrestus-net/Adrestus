package io.Adrestus.core.RingBuffer.handler.transactions;

import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.core.*;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;

public class AmountEventHandler extends TransactionEventHandler implements TransactionUnitVisitor {
    private static Logger LOG = LoggerFactory.getLogger(AmountEventHandler.class);


    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        Transaction transaction = transactionEvent.getTransaction();
        if (transaction.getStatus().equals(StatusType.BUFFERED) || transaction.getStatus().equals(StatusType.ABORT))
            return;
        transaction.accept(this);
    }

    @Override
    public void visit(RegularTransaction regularTransaction) {
        //to
        try {
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(regularTransaction.getTo()).get();

        } catch (NoSuchElementException ex) {
            LOG.info("State trie is empty we add address");
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(regularTransaction.getTo(), new PatriciaTreeNode(0, 0));
        } catch (NullPointerException ex) {
            LOG.info("Transaction is empty");
            regularTransaction.setStatus(StatusType.ABORT);
            return;
        }
        //from
        PatriciaTreeNode patriciaTreeNode = null;
        try {
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(regularTransaction.getFrom()).get();

        } catch (NoSuchElementException ex) {
            LOG.info("State trie is empty we add address");
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(regularTransaction.getFrom(), new PatriciaTreeNode(0, 0));
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(regularTransaction.getFrom()).get();
        } catch (NullPointerException ex) {
            LOG.info("Transaction is empty");
            regularTransaction.setStatus(StatusType.ABORT);
            return;
        }

        if (regularTransaction.getAmount() >= patriciaTreeNode.getAmount()) {
            LOG.info("Transaction amount is not sufficient");
            regularTransaction.setStatus(StatusType.ABORT);
            return;
        }
    }

    @Override
    public void visit(RewardsTransaction rewardsTransaction) {
        PatriciaTreeNode patriciaTreeNode = null;
        try {
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(rewardsTransaction.getRecipientAddress()).get();

        } catch (NoSuchElementException ex) {
            LOG.info("State trie is empty we add address");
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(rewardsTransaction.getRecipientAddress(), new PatriciaTreeNode(0, 0));
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(rewardsTransaction.getRecipientAddress()).get();
        } catch (NullPointerException ex) {
            LOG.info("Transaction is empty");
            rewardsTransaction.setStatus(StatusType.ABORT);
            return;
        }

        if (rewardsTransaction.getAmount() > patriciaTreeNode.getUnclaimed_reward()) {
            LOG.info("Transaction Claimed reward is not sufficient");
            rewardsTransaction.setStatus(StatusType.ABORT);
            return;
        }
    }


    @Override
    public void visit(StakingTransaction stakingTransaction) {
        PatriciaTreeNode patriciaTreeNode = null;
        try {
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(stakingTransaction.getValidatorAddress()).get();

        } catch (NoSuchElementException ex) {
            LOG.info("State trie is empty we add address");
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(stakingTransaction.getValidatorAddress(), new PatriciaTreeNode(0, 0));
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(stakingTransaction.getValidatorAddress()).get();
        } catch (NullPointerException ex) {
            LOG.info("Transaction is empty");
            stakingTransaction.setStatus(StatusType.ABORT);
            return;
        }

        if (stakingTransaction.getAmount() > patriciaTreeNode.getAmount()) {
            LOG.info("Staking Transaction amount is not sufficient");
            stakingTransaction.setStatus(StatusType.ABORT);
            return;
        }
    }

    @Override
    public void visit(DelegateTransaction delegateTransaction) {

    }

    @Override
    public void visit(UnclaimedFeeRewardTransaction unclaimedFeeRewardTransaction) {
        PatriciaTreeNode patriciaTreeNode = null;
        try {
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(unclaimedFeeRewardTransaction.getRecipientAddress()).get();

        } catch (NoSuchElementException ex) {
            LOG.info("State trie is empty we add address");
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(unclaimedFeeRewardTransaction.getRecipientAddress(), new PatriciaTreeNode(0, 0));
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(unclaimedFeeRewardTransaction.getRecipientAddress()).get();
        } catch (NullPointerException ex) {
            LOG.info("Transaction is empty");
            unclaimedFeeRewardTransaction.setStatus(StatusType.ABORT);
            return;
        }
    }
}
