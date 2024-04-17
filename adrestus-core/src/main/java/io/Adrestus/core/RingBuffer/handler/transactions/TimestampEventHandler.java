package io.Adrestus.core.RingBuffer.handler.transactions;

import io.Adrestus.TreeFactory;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.Transaction;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.util.GetTime;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import io.distributedLedger.ZoneDatabaseFactory;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.*;

public class TimestampEventHandler extends TransactionEventHandler {
    private static Logger LOG = LoggerFactory.getLogger(TimestampEventHandler.class);

    public TimestampEventHandler() {
    }

    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        try {
            Transaction transaction = transactionEvent.getTransaction();

            if (transaction.getStatus().equals(StatusType.BUFFERED) || transaction.getStatus().equals(StatusType.ABORT))
                return;


            HashMap<Integer, HashSet<Integer>> mapsearch;
            try {
                mapsearch = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(transaction.getFrom()).get().retrieveAllTransactionsByOriginZone(CachedZoneIndex.getInstance().getZoneIndex());
            } catch (NoSuchElementException e) {
                return;
            }

            if (mapsearch.isEmpty())
                return;
            Integer max = Collections.max(mapsearch.keySet(), new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return Integer.compare(o1, o2);
                }
            });

            ArrayList<Transaction> results = new ArrayList<>();
            IDatabase<String, TransactionBlock> transactionBlockIDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
            mapsearch.get(max).forEach(value -> {
                try {
                    Transaction trx = transactionBlockIDatabase.findByKey(String.valueOf(max)).get().getTransactionList().get(value);
                    if (trx.getFrom().equals(transaction.getFrom())) {
                        results.add(trx);
                    }
                } catch (NoSuchElementException e) {
                } catch (IndexOutOfBoundsException e) {
                }
            });

            if (results.isEmpty())
                return;

            Collections.sort(results, new Comparator<Transaction>() {
                @SneakyThrows
                @Override
                public int compare(Transaction u1, Transaction u2) {
                    return GetTime.GetTimestampFromString(u2.getTimestamp()).compareTo(GetTime.GetTimestampFromString(u1.getTimestamp()));
                }
            });
            Timestamp old = GetTime.GetTimestampFromString(results.get(0).getTimestamp());
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
