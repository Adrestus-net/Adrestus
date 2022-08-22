package io.Adrestus.core.RingBuffer.handler.transactions;

import io.Adrestus.core.Resourses.MemoryTreePool;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.Transaction;
import io.Adrestus.core.Trie.PatriciaTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;

public class AmountEventHandler extends TransactionEventHandler {
    private static Logger LOG = LoggerFactory.getLogger(AmountEventHandler.class);


    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        Transaction transaction=null;
        PatriciaTreeNode patriciaTreeNode=null;
        try {
            transaction = transactionEvent.getTransaction();
            patriciaTreeNode = MemoryTreePool.getInstance().getByaddress(transaction.getFrom()).get();

        } catch (NoSuchElementException ex) {
            LOG.info("State trie is empty we add address");
            MemoryTreePool.getInstance().store(transaction.getFrom(),new PatriciaTreeNode(0,0));
            patriciaTreeNode = MemoryTreePool.getInstance().getByaddress(transaction.getFrom()).get();
        } catch (NullPointerException ex) {
            LOG.info("Transaction is empty");
        }

        if (transaction.getAmount() >= patriciaTreeNode.getAmount()) {
            LOG.info("Transaction amount is not sufficient");
            transaction.setStatus(StatusType.ABORT);
            return;
        }
    }
}
