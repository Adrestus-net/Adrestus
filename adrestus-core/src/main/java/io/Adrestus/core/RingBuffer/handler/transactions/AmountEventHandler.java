package io.Adrestus.core.RingBuffer.handler.transactions;

import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.RewardsTransaction;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;

public class AmountEventHandler extends TransactionEventHandler {
    private static Logger LOG = LoggerFactory.getLogger(AmountEventHandler.class);


    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        Transaction transaction = transactionEvent.getTransaction();
        if (transaction.getStatus().equals(StatusType.BUFFERED) || transaction.getStatus().equals(StatusType.ABORT))
            return;
        if (transaction instanceof RewardsTransaction) {
            //from
            PatriciaTreeNode patriciaTreeNode = null;
            try {
                patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(((RewardsTransaction) transaction).getRecipientAddress()).get();

            } catch (NoSuchElementException ex) {
                LOG.info("State trie is empty we add address");
                TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(((RewardsTransaction) transaction).getRecipientAddress(), new PatriciaTreeNode(0, 0));
                patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(((RewardsTransaction) transaction).getRecipientAddress()).get();
            } catch (NullPointerException ex) {
                LOG.info("Transaction is empty");
                transaction.setStatus(StatusType.ABORT);
                return;
            }

            if (transaction.getAmount() > patriciaTreeNode.getUnclaimed_reward()) {
                LOG.info("Transaction Claimed reword is not sufficient");
                transaction.setStatus(StatusType.ABORT);
                return;
            }
            return;
        }
        //to
        try {
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(transaction.getTo()).get();

        } catch (NoSuchElementException ex) {
            LOG.info("State trie is empty we add address");
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(transaction.getTo(), new PatriciaTreeNode(0, 0));
        } catch (NullPointerException ex) {
            LOG.info("Transaction is empty");
            transaction.setStatus(StatusType.ABORT);
            return;
        }
        //from
        PatriciaTreeNode patriciaTreeNode = null;
        try {
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(transaction.getFrom()).get();

        } catch (NoSuchElementException ex) {
            LOG.info("State trie is empty we add address");
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(transaction.getFrom(), new PatriciaTreeNode(0, 0));
            patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(transaction.getFrom()).get();
        } catch (NullPointerException ex) {
            LOG.info("Transaction is empty");
            transaction.setStatus(StatusType.ABORT);
            return;
        }

        if (transaction.getAmount() >= patriciaTreeNode.getAmount()) {
            LOG.info("Transaction amount is not sufficient");
            transaction.setStatus(StatusType.ABORT);
            return;
        }
    }
}
