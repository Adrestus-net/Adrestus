package io.Adrestus.core.RingBuffer.handler.transactions;

import com.google.common.reflect.TypeToken;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.Transaction;
import io.Adrestus.util.GetTime;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import io.distributedLedger.LevelDBTransactionWrapper;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;

public class TimestampEventHandler extends TransactionEventHandler {
    private static Logger LOG = LoggerFactory.getLogger(TimestampEventHandler.class);
    private final IDatabase<String, LevelDBTransactionWrapper<Transaction>> database;

    public TimestampEventHandler() {
        database = new DatabaseFactory(String.class, Transaction.class, new TypeToken<LevelDBTransactionWrapper<Transaction>>() {
        }.getType()).getDatabase(DatabaseType.LEVEL_DB);
    }

    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        try {
            Transaction transaction = transactionEvent.getTransaction();

            if (transaction.getStatus().equals(StatusType.BUFFERED)|| transaction.getStatus().equals(StatusType.ABORT))
                return;


            Optional<LevelDBTransactionWrapper<Transaction>> wrapper = database.findByKey(transaction.getFrom());

            if (!wrapper.isPresent())
                return;
            if (wrapper.get().getFrom().isEmpty())
                return;

            Collections.sort(wrapper.get().getFrom(), new Comparator<Transaction>() {
                @SneakyThrows
                @Override
                public int compare(Transaction u1, Transaction u2) {
                    return GetTime.GetTimestampFromString(u2.getTimestamp()).compareTo(GetTime.GetTimestampFromString(u1.getTimestamp()));
                }
            });
            Timestamp old = GetTime.GetTimestampFromString(wrapper.get().getFrom().get(0).getTimestamp());
            Timestamp current = GetTime.GetTimestampFromString(transaction.getTimestamp());
            Timestamp check = GetTime.GetTimeStampWithDelay();
            if (current.before(old)) {
                LOG.info("Transaction abort: Transaction timestamp is not a valid timestamp");
                transaction.setStatus(StatusType.ABORT);
            }
            if (check.before(old)) {
                LOG.info("Transaction abort: Transaction timestamp is not older than one minute delay");
                transaction.setStatus(StatusType.ABORT);
            }
        } catch (NullPointerException ex) {
            LOG.info("Transaction is empty");
        }
    }
}
