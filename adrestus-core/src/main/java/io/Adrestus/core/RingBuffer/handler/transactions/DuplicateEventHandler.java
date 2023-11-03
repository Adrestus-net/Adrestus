package io.Adrestus.core.RingBuffer.handler.transactions;

import com.google.common.reflect.TypeToken;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.Transaction;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import io.distributedLedger.LevelDBTransactionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Optional;

public class DuplicateEventHandler extends TransactionEventHandler {
    private static Logger LOG = LoggerFactory.getLogger(DuplicateEventHandler.class);

    private final IDatabase<String, LevelDBTransactionWrapper<Transaction>> transaction_database;
    public DuplicateEventHandler() {
        this.transaction_database = new DatabaseFactory(String.class, Transaction.class, new TypeToken<LevelDBTransactionWrapper<Transaction>>() {
        }.getType()).getDatabase(DatabaseType.LEVEL_DB);
    }

    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        try {
            Transaction transaction = transactionEvent.getTransaction();

            ArrayList<Transaction> tosearch;
            try {
                tosearch = transaction_database.findByKey(transaction.getFrom()).get().getFrom();
            } catch (NoSuchElementException e) {
                return;
            }
            Optional<Transaction> transaction_hint = tosearch.stream().filter(tr -> tr.getHash().equals(transaction.getHash())).findFirst();

            if (transaction_hint.isPresent()) {
                transaction.setStatus(StatusType.BUFFERED);
            }

        } catch (NullPointerException ex) {
            LOG.info("Transaction is empty");
        }
    }
}
