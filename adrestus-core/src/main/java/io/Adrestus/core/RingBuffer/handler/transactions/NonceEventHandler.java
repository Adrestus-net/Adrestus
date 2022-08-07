package io.Adrestus.core.RingBuffer.handler.transactions;

import com.lmax.disruptor.EventHandler;
import io.Adrestus.core.Resourses.MemoryTreePool;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import io.Adrestus.core.Transaction;
import io.Adrestus.core.TransactionStatus;
import io.Adrestus.core.Trie.PatriciaTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;

public class NonceEventHandler implements EventHandler<TransactionEvent> {
    private static Logger LOG = LoggerFactory.getLogger(NonceEventHandler.class);

    private MemoryTreePool memoryTreePool;

    public NonceEventHandler(MemoryTreePool memoryTreePool) {
        this.memoryTreePool = memoryTreePool;
    }

    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        try {
            Transaction transaction = transactionEvent.getTransaction();
            PatriciaTreeNode patriciaTreeNode = memoryTreePool.getByaddress(transaction.getFrom()).get();
            if (patriciaTreeNode.getNonce() + 1 != transaction.getNonce()) {
                LOG.info("Transaction nonce is not valid");
                transaction.setStatus(TransactionStatus.ABORT);
            }
        } catch (NoSuchElementException ex) {
            LOG.info("Transaction fields are empty");
        } catch (NullPointerException ex) {
            LOG.info("Transaction is empty");
        }
    }
}
