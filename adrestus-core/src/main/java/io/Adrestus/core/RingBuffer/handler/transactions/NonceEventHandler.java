package io.Adrestus.core.RingBuffer.handler.transactions;

import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.core.*;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;
import java.util.Optional;

public class NonceEventHandler extends TransactionEventHandler implements TransactionUnitVisitor {
    private static Logger LOG = LoggerFactory.getLogger(NonceEventHandler.class);

    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        Transaction transaction = transactionEvent.getTransaction();
        if (transaction.getStatus().equals(StatusType.BUFFERED) || transaction.getStatus().equals(StatusType.ABORT))
            return;
        transaction.accept(this);

    }

    @Override
    public void visit(RegularTransaction regularTransaction) {
        PatriciaTreeNode patriciaTreeNode = null;
        try {
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(regularTransaction.getFrom()).get();

        } catch (NoSuchElementException ex) {
            LOG.info("State trie is empty we add From address");
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(regularTransaction.getFrom(), new PatriciaTreeNode(0, 0));
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(regularTransaction.getFrom()).get();
        } catch (NullPointerException ex) {
            Optional.of("RegularTransaction is empty").ifPresent(val -> {
                LOG.info(val);
                regularTransaction.infos(val);
            });
            regularTransaction.setStatus(StatusType.ABORT);
            return;
        }

        if (patriciaTreeNode.getNonce() + 1 != regularTransaction.getNonce()) {
            Optional.of("RegularTransaction nonce is not valid").ifPresent(val -> {
                LOG.info(val);
                regularTransaction.infos(val);
            });
            regularTransaction.setStatus(StatusType.ABORT);
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
            Optional.of("RewardsTransaction is empty").ifPresent(val -> {
                LOG.info(val);
                rewardsTransaction.infos(val);
            });
            rewardsTransaction.setStatus(StatusType.ABORT);
            return;
        }

        if (patriciaTreeNode.getNonce() + 1 != rewardsTransaction.getNonce()) {
            Optional.of("RewardsTransaction nonce is not valid").ifPresent(val -> {
                LOG.info(val);
                rewardsTransaction.infos(val);
            });
            rewardsTransaction.setStatus(StatusType.ABORT);
        }
    }

    @Override
    public void visit(StakingTransaction stakingTransaction) {
        PatriciaTreeNode patriciaTreeNode = null;
        try {

            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(stakingTransaction.getValidatorAddress()).get();

        } catch (NoSuchElementException ex) {
            LOG.info("State trie is empty we add ValidatorAddress");
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

        if (patriciaTreeNode.getNonce() + 1 != stakingTransaction.getNonce()) {
            Optional.of("StakingTransaction nonce is not valid").ifPresent(val -> {
                LOG.info(val);
                stakingTransaction.infos(val);
            });
            stakingTransaction.setStatus(StatusType.ABORT);
        }
    }

    @Override
    public void visit(DelegateTransaction delegateTransaction) {
        PatriciaTreeNode patriciaTreeNode = null;
        try {

            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(delegateTransaction.getDelegatorAddress()).get();

        } catch (NoSuchElementException ex) {
            LOG.info("State trie is empty we add DelegatorAddress");
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(delegateTransaction.getDelegatorAddress(), new PatriciaTreeNode(0, 0));
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(delegateTransaction.getDelegatorAddress()).get();
        } catch (NullPointerException ex) {
            Optional.of("DelegateTransaction is empty").ifPresent(val -> {
                LOG.info(val);
                delegateTransaction.infos(val);
            });
            delegateTransaction.setStatus(StatusType.ABORT);
            return;
        }

        if (patriciaTreeNode.getNonce() + 1 != delegateTransaction.getNonce()) {
            Optional.of("DelegateTransaction nonce is not valid").ifPresent(val -> {
                LOG.info(val);
                delegateTransaction.infos(val);
            });
            delegateTransaction.setStatus(StatusType.ABORT);
        }
    }

    @Override
    public void visit(UnclaimedFeeRewardTransaction unclaimedFeeRewardTransaction) {
        PatriciaTreeNode patriciaTreeNode = null;
        try {

            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(unclaimedFeeRewardTransaction.getRecipientAddress()).get();

        } catch (NoSuchElementException ex) {
            LOG.info("State trie is empty we add UnclaimedFeeRewardTransaction");
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(unclaimedFeeRewardTransaction.getRecipientAddress(), new PatriciaTreeNode(0, 0));
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
        PatriciaTreeNode patriciaTreeNode = null;
        try {

            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(unDelegateTransaction.getDelegatorAddress()).get();

        } catch (NoSuchElementException ex) {
            Optional.of("State trie UndelegatingTransaction is empty abort").ifPresent(val -> {
                LOG.info(val);
                unDelegateTransaction.infos(val);
            });
            unDelegateTransaction.setStatus(StatusType.ABORT);
            return;
        } catch (NullPointerException ex) {
            Optional.of("UndelegatingTransaction is empty").ifPresent(val -> {
                LOG.info(val);
                unDelegateTransaction.infos(val);
            });
            unDelegateTransaction.setStatus(StatusType.ABORT);
            return;
        }

        if (patriciaTreeNode.getNonce() + 1 != unDelegateTransaction.getNonce()) {
            Optional.of("DelegateTransaction nonce is not valid").ifPresent(val -> {
                LOG.info(val);
                unDelegateTransaction.infos(val);
            });
            unDelegateTransaction.setStatus(StatusType.ABORT);
        }
    }

    @Override
    public void visit(UnstakingTransaction unstakingTransaction) {
        PatriciaTreeNode patriciaTreeNode = null;
        try {

            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(unstakingTransaction.getValidatorAddress()).get();

        } catch (NoSuchElementException ex) {
            Optional.of("State trie UnstakingTransaction is empty abort").ifPresent(val -> {
                LOG.info(val);
                unstakingTransaction.infos(val);
            });
            unstakingTransaction.setStatus(StatusType.ABORT);
        } catch (NullPointerException ex) {
            Optional.of("UnstakingTransaction is empty").ifPresent(val -> {
                LOG.info(val);
                unstakingTransaction.infos(val);
            });
            unstakingTransaction.setStatus(StatusType.ABORT);
            return;
        }

        if (patriciaTreeNode.getNonce() + 1 != unstakingTransaction.getNonce()) {
            Optional.of("UnstakingTransaction nonce is not valid").ifPresent(val -> {
                LOG.info(val);
                unstakingTransaction.infos(val);
            });
            unstakingTransaction.setStatus(StatusType.ABORT);
        }
    }
}
