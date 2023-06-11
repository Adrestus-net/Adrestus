package io.Adrestus.core.RingBuffer.handler.transactions;

import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.Resourses.MemoryTransactionPool;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

public class NonceEventHandler extends TransactionEventHandler {
    private static Logger LOG = LoggerFactory.getLogger(NonceEventHandler.class);

    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        Transaction transaction = null;
        PatriciaTreeNode patriciaTreeNode = null;
        try {
            transaction = transactionEvent.getTransaction();
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

        if (patriciaTreeNode.getNonce() + 1 != transaction.getNonce()) {
            /*List<Transaction> list= MemoryTransactionPool.getInstance().getAll();
            Transaction finalTransaction = transaction;
            List<Transaction> gf=list.stream().filter(tr->tr.getFrom().equals(finalTransaction.getFrom())).collect(Collectors.toList());*/
            LOG.info("Transaction nonce is not valid");
            transaction.setStatus(StatusType.ABORT);
        }
    }
}
