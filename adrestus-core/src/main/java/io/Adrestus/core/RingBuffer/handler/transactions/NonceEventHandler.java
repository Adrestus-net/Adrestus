package io.Adrestus.core.RingBuffer.handler.transactions;

import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;

public class NonceEventHandler extends TransactionEventHandler {
    private static Logger LOG = LoggerFactory.getLogger(NonceEventHandler.class);

    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        Transaction transaction = null;
        PatriciaTreeNode patriciaTreeNode = null;
        try {
            transaction = transactionEvent.getTransaction();

            if (transaction.getStatus().equals(StatusType.BUFFERED) || transaction.getStatus().equals(StatusType.ABORT))
                return;

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
           /* List<Transaction> list= MemoryTransactionPool.getInstance().getAll();
            Transaction finalTransaction = transaction;
            List<Transaction> gf=list.stream().filter(tr->tr.getFrom().equals(finalTransaction.getFrom())).collect(Collectors.toList());
            IDatabase<String, LevelDBTransactionWrapper<Transaction>> transaction_database = new DatabaseFactory(String.class, Transaction.class, new TypeToken<LevelDBTransactionWrapper<Transaction>>() {
            }.getType()).getDatabase(DatabaseType.LEVEL_DB);
            LevelDBTransactionWrapper<Transaction> tosearch;
            Map<String,List<Transaction>> map=CacheTemporalTransactionPool.getInstance().getAsMap();
            List<Transaction>tra=map.get(transaction.getFrom());
            try {
                tosearch = transaction_database.findByKey(transaction.getFrom()).get();
            } catch (NoSuchElementException e) {
                return;
            }
            if(!gf.isEmpty()) {
                boolean val=MemoryTransactionPool.getInstance().checkAdressExists(finalTransaction);
                System.out.println("previoussssssssssssssssss");
                int h = 3;
            }*/
            LOG.info("Transaction nonce is not valid");
            transaction.setStatus(StatusType.ABORT);
        }
    }
}
