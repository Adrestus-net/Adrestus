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
            LOG.info("State trie is empty we add getTo address");
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(regularTransaction.getTo(), new PatriciaTreeNode(0, 0));
        } catch (NullPointerException ex) {
            LOG.info("RegularTransaction To is empty");
            regularTransaction.setStatus(StatusType.ABORT);
            return;
        }
        //from
        PatriciaTreeNode patriciaTreeNode = null;
        try {
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(regularTransaction.getFrom()).get();

        } catch (NoSuchElementException ex) {
            LOG.info("State trie is empty we add getFrom address");
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(regularTransaction.getFrom(), new PatriciaTreeNode(0, 0));
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(regularTransaction.getFrom()).get();
        } catch (NullPointerException ex) {
            LOG.info("RegularTransaction from is empty");
            regularTransaction.setStatus(StatusType.ABORT);
            return;
        }

        if (regularTransaction.getAmount() > patriciaTreeNode.getAmount()) {
            LOG.info("RegularTransaction amount is not sufficient");
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
            LOG.info("State trie is empty we add RecipientAddress address");
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(rewardsTransaction.getRecipientAddress(), new PatriciaTreeNode(0, 0));
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(rewardsTransaction.getRecipientAddress()).get();
        } catch (NullPointerException ex) {
            LOG.info("RewardsTransaction is empty");
            rewardsTransaction.setStatus(StatusType.ABORT);
            return;
        }

        if (rewardsTransaction.getAmount() > patriciaTreeNode.getUnclaimed_reward()) {
            LOG.info("RewardsTransaction Claimed reward is not sufficient");
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
            LOG.info("State trie is empty we add ValidatorAddress address");
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(stakingTransaction.getValidatorAddress(), new PatriciaTreeNode(0, 0));
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(stakingTransaction.getValidatorAddress()).get();
        } catch (NullPointerException ex) {
            LOG.info("StakingTransaction is empty");
            stakingTransaction.setStatus(StatusType.ABORT);
            return;
        }

        if (stakingTransaction.getAmount() > patriciaTreeNode.getAmount()) {
            LOG.info("StakingTransaction amount is not sufficient");
            stakingTransaction.setStatus(StatusType.ABORT);
            return;
        }
    }

    @Override
    public void visit(DelegateTransaction delegateTransaction) {
        //getValidatorAddress()
        try {
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(delegateTransaction.getValidatorAddress()).get();

        } catch (NoSuchElementException ex) {
            LOG.info("State trie is empty we add getValidatorAddress address");
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(delegateTransaction.getValidatorAddress(), new PatriciaTreeNode(0, 0));
        } catch (NullPointerException ex) {
            LOG.info("DelegateTransaction is empty");
            delegateTransaction.setStatus(StatusType.ABORT);
            return;
        }
        //getDelegatorAddress()
        PatriciaTreeNode patriciaTreeNode = null;
        try {
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(delegateTransaction.getDelegatorAddress()).get();

        } catch (NoSuchElementException ex) {
            LOG.info("State trie is empty we add get DelegatorAddress");
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(delegateTransaction.getDelegatorAddress(), new PatriciaTreeNode(0, 0));
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(delegateTransaction.getDelegatorAddress()).get();
        } catch (NullPointerException ex) {
            LOG.info("DelegateTransaction DelegatorAddress is empty");
            delegateTransaction.setStatus(StatusType.ABORT);
            return;
        }

        if (delegateTransaction.getAmount() > patriciaTreeNode.getAmount()) {
            LOG.info("Transaction amount is not sufficient");
            delegateTransaction.setStatus(StatusType.ABORT);
            return;
        }
    }

    @Override
    public void visit(UnclaimedFeeRewardTransaction unclaimedFeeRewardTransaction) {
        PatriciaTreeNode patriciaTreeNode = null;
        try {
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(unclaimedFeeRewardTransaction.getRecipientAddress()).get();

        } catch (NoSuchElementException ex) {
            LOG.info("State trie is empty we add getRecipientAddress");
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(unclaimedFeeRewardTransaction.getRecipientAddress(), new PatriciaTreeNode(0, 0));
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(unclaimedFeeRewardTransaction.getRecipientAddress()).get();
        } catch (NullPointerException ex) {
            LOG.info("UnclaimedFeeRewardTransaction is empty");
            unclaimedFeeRewardTransaction.setStatus(StatusType.ABORT);
            return;
        }
    }

    @Override
    public void visit(UnDelegateTransaction unDelegateTransaction) {
        //getValidatorAddress()
        PatriciaTreeNode patriciaTreeNode = null;
        try {
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(unDelegateTransaction.getValidatorAddress()).get();

        } catch (NoSuchElementException ex) {
            LOG.info("UndelegatingTransaction State trie is empty we add getValidatorAddress address");
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(unDelegateTransaction.getValidatorAddress(), new PatriciaTreeNode(0, 0));
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(unDelegateTransaction.getValidatorAddress()).get();
        } catch (NullPointerException ex) {
            LOG.info("UndelegatingTransaction is empty");
            unDelegateTransaction.setStatus(StatusType.ABORT);
            return;
        }
        //getDelegatorAddress()
        try {
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(unDelegateTransaction.getDelegatorAddress()).get();

        } catch (NoSuchElementException ex) {
            LOG.info("UndelegatingTransaction State trie is empty we add get DelegatorAddress");
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(unDelegateTransaction.getDelegatorAddress(), new PatriciaTreeNode(0, 0));
        } catch (NullPointerException ex) {
            LOG.info("UndelegatingTransaction DelegatorAddress is empty");
            unDelegateTransaction.setStatus(StatusType.ABORT);
            return;
        }

        if (unDelegateTransaction.getAmount() > patriciaTreeNode.getStaking_amount()) {
            LOG.info("UndelegatingTransaction amount is not sufficient");
            unDelegateTransaction.setStatus(StatusType.ABORT);
            return;
        }
    }

    @Override
    public void visit(UnstakingTransaction unstakingTransaction) {
        PatriciaTreeNode patriciaTreeNode = null;
        try {
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(unstakingTransaction.getValidatorAddress()).get();

        } catch (NoSuchElementException ex) {
            LOG.info("UnstakingTransaction State trie is empty abort");
            unstakingTransaction.setStatus(StatusType.ABORT);
            return;
        } catch (NullPointerException ex) {
            LOG.info("UnstakingTransaction is empty");
            unstakingTransaction.setStatus(StatusType.ABORT);
            return;
        }

        if (unstakingTransaction.getAmount() > patriciaTreeNode.getStaking_amount()) {
            LOG.info("UnstakingTransaction amount is not sufficient");
            unstakingTransaction.setStatus(StatusType.ABORT);
            return;
        }
    }
}
