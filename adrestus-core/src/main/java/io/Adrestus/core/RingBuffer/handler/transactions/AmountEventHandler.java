package io.Adrestus.core.RingBuffer.handler.transactions;

import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.core.*;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;

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
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(regularTransaction.getTo(), new PatriciaTreeNode(BigDecimal.ZERO, 0));
        } catch (NullPointerException ex) {
            Optional.of("RegularTransaction To is empty").ifPresent(val -> {
                LOG.info(val);
                regularTransaction.infos(val);
            });
            regularTransaction.setStatus(StatusType.ABORT);
            return;
        }
        //from
        PatriciaTreeNode patriciaTreeNode = null;
        try {
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(regularTransaction.getFrom()).get();

        } catch (NoSuchElementException ex) {
            LOG.info("State trie is empty we add getFrom address");
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(regularTransaction.getFrom(), new PatriciaTreeNode(BigDecimal.ZERO, 0));
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(regularTransaction.getFrom()).get();
        } catch (NullPointerException ex) {
            Optional.of("RegularTransaction from is empty").ifPresent(val -> {
                LOG.info(val);
                regularTransaction.infos(val);
            });
            regularTransaction.setStatus(StatusType.ABORT);
            return;
        }

        if (regularTransaction.getAmount().compareTo(patriciaTreeNode.getAmount()) > 0) {
            Optional.of("RegularTransaction amount is not sufficient").ifPresent(val -> {
                LOG.info(val);
                regularTransaction.infos(val);
            });
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
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(rewardsTransaction.getRecipientAddress(), new PatriciaTreeNode(BigDecimal.ZERO, 0));
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(rewardsTransaction.getRecipientAddress()).get();
        } catch (NullPointerException ex) {
            Optional.of("RewardsTransaction is empty").ifPresent(val -> {
                LOG.info(val);
                rewardsTransaction.infos(val);
            });
            rewardsTransaction.setStatus(StatusType.ABORT);
            return;
        }

        if (rewardsTransaction.getAmount().compareTo(patriciaTreeNode.getUnclaimed_reward()) > 0) {
            Optional.of("RewardsTransaction Claimed reward is not sufficient").ifPresent(val -> {
                LOG.info(val);
                rewardsTransaction.infos(val);
            });
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
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(stakingTransaction.getValidatorAddress(), new PatriciaTreeNode(BigDecimal.ZERO, 0));
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(stakingTransaction.getValidatorAddress()).get();
        } catch (NullPointerException ex) {
            Optional.of("StakingTransaction is empty").ifPresent(val -> {
                LOG.info(val);
                stakingTransaction.infos(val);
            });
            stakingTransaction.setStatus(StatusType.ABORT);
            return;
        }

        if (stakingTransaction.getAmount().compareTo(patriciaTreeNode.getAmount()) > 0) {
            Optional.of("StakingTransaction amount is not sufficient").ifPresent(val -> {
                LOG.info(val);
                stakingTransaction.infos(val);
            });
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
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(delegateTransaction.getValidatorAddress(), new PatriciaTreeNode(BigDecimal.ZERO, 0));
        } catch (NullPointerException ex) {
            Optional.of("DelegateTransaction ValidatorAddress is empty").ifPresent(val -> {
                LOG.info(val);
                delegateTransaction.infos(val);
            });
            delegateTransaction.setStatus(StatusType.ABORT);
            return;
        }
        //getDelegatorAddress()
        PatriciaTreeNode patriciaTreeNode = null;
        try {
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(delegateTransaction.getDelegatorAddress()).get();

        } catch (NoSuchElementException ex) {
            LOG.info("State trie is empty we add get DelegatorAddress");
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(delegateTransaction.getDelegatorAddress(), new PatriciaTreeNode(BigDecimal.ZERO, 0));
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(delegateTransaction.getDelegatorAddress()).get();
        } catch (NullPointerException ex) {
            Optional.of("DelegateTransaction DelegatorAddress is empty").ifPresent(val -> {
                LOG.info(val);
                delegateTransaction.infos(val);
            });
            delegateTransaction.setStatus(StatusType.ABORT);
            return;
        }

        if (delegateTransaction.getAmount().compareTo(patriciaTreeNode.getAmount()) > 0) {
            Optional.of("DelegateTransaction amount is not sufficient").ifPresent(val -> {
                LOG.info(val);
                delegateTransaction.infos(val);
            });
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
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(unclaimedFeeRewardTransaction.getRecipientAddress(), new PatriciaTreeNode(BigDecimal.ZERO, 0));
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(unclaimedFeeRewardTransaction.getRecipientAddress()).get();
        } catch (NullPointerException ex) {
            Optional.of("UnclaimedFeeRewardTransaction is empty").ifPresent(val -> {
                LOG.info(val);
                unclaimedFeeRewardTransaction.infos(val);
            });
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
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(unDelegateTransaction.getValidatorAddress(), new PatriciaTreeNode(BigDecimal.ZERO, 0));
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(unDelegateTransaction.getValidatorAddress()).get();
        } catch (NullPointerException ex) {
            Optional.of("UndelegatingTransaction ValidatorAddress is empty").ifPresent(val -> {
                LOG.info(val);
                unDelegateTransaction.infos(val);
            });
            unDelegateTransaction.setStatus(StatusType.ABORT);
            return;
        }
        //getDelegatorAddress()
        try {
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(unDelegateTransaction.getDelegatorAddress()).get();

        } catch (NoSuchElementException ex) {
            LOG.info("UndelegatingTransaction State trie is empty we add get DelegatorAddress");
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(unDelegateTransaction.getDelegatorAddress(), new PatriciaTreeNode(BigDecimal.ZERO, 0));
        } catch (NullPointerException ex) {
            Optional.of("UndelegatingTransaction DelegatorAddress is empty").ifPresent(val -> {
                LOG.info(val);
                unDelegateTransaction.infos(val);
            });
            unDelegateTransaction.setStatus(StatusType.ABORT);
            return;
        }

        if (unDelegateTransaction.getAmount().compareTo(patriciaTreeNode.getStaking_amount()) > 0) {
            Optional.of("UndelegatingTransaction amount is not sufficient").ifPresent(val -> {
                LOG.info(val);
                unDelegateTransaction.infos(val);
            });
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
            Optional.of("UnstakingTransaction State trie is empty abort").ifPresent(val -> {
                LOG.info(val);
                unstakingTransaction.infos(val);
            });
            unstakingTransaction.setStatus(StatusType.ABORT);
            return;
        } catch (NullPointerException ex) {
            Optional.of("UnstakingTransaction is empty").ifPresent(val -> {
                LOG.info(val);
                unstakingTransaction.infos(val);
            });
            unstakingTransaction.setStatus(StatusType.ABORT);
            return;
        }

        if (unstakingTransaction.getAmount().compareTo(patriciaTreeNode.getStaking_amount()) > 0) {
            Optional.of("UnstakingTransaction amount is not sufficient").ifPresent(val -> {
                LOG.info(val);
                unstakingTransaction.infos(val);
            });
            unstakingTransaction.setStatus(StatusType.ABORT);
            return;
        }
    }
}
