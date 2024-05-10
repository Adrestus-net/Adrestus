package io.Adrestus.core.RingBuffer.handler.transactions;

import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.core.*;
import io.Adrestus.core.Resourses.CacheTemporalTransactionPool;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.Resourses.MemoryTransactionPool;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SameOriginEventHandler extends TransactionEventHandler implements TransactionUnitVisitor {
    private static Logger LOG = LoggerFactory.getLogger(SameOriginEventHandler.class);
    private PatriciaTreeNode patriciaTreeNode;

    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        try {
            Transaction transaction = transactionEvent.getTransaction();

            if (transaction.getStatus().equals(StatusType.BUFFERED) || transaction.getStatus().equals(StatusType.ABORT))
                return;

            if (MemoryTransactionPool.getInstance().checkAdressExists(transaction)) {
                transaction.setStatus(StatusType.BUFFERED);
                CacheTemporalTransactionPool.getInstance().add(transaction);
                return;
            }
            transaction.accept(this);

            if (transaction.getNonce() == patriciaTreeNode.getNonce() + 1)
                return;

            transaction.setStatus(StatusType.BUFFERED);
            CacheTemporalTransactionPool.getInstance().add(transaction);

        } catch (NullPointerException ex) {
            LOG.info("Transaction is empty");
        }
    }


    @Override
    public void visit(RegularTransaction regularTransaction) {
        patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(regularTransaction.getFrom()).get();
    }

    @Override
    public void visit(RewardsTransaction rewardsTransaction) {
        patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(rewardsTransaction.getRecipientAddress()).get();
    }

    @Override
    public void visit(StakingTransaction stakingTransaction) {

    }

    @Override
    public void visit(DelegateTransaction delegateTransaction) {

    }

    @Override
    public void visit(UnclaimedFeeRewardTransaction unclaimedFeeRewardTransaction) {
        patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(unclaimedFeeRewardTransaction.getRecipientAddress()).get();
    }
}
