package io.Adrestus.core.RingBuffer.handler.transactions;

import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeTransactionType;
import io.Adrestus.core.*;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
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

public class TimestampEventHandler extends TransactionEventHandler implements TransactionUnitVisitor {
    private static Logger LOG = LoggerFactory.getLogger(TimestampEventHandler.class);

    public TimestampEventHandler() {
    }

    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        try {
            Transaction transaction = transactionEvent.getTransaction();

            if (transaction.getStatus().equals(StatusType.BUFFERED) || transaction.getStatus().equals(StatusType.ABORT))
                return;


            transaction.accept(this);

        } catch (NullPointerException ex) {
            LOG.info("Transaction is empty");
        }
    }

    @Override
    public void visit(RegularTransaction regularTransaction) {
        HashMap<Integer, HashSet<Integer>> mapsearch;
        try {
            mapsearch = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(regularTransaction.getFrom()).get().retrieveAllTransactionsByOriginZone(PatriciaTreeTransactionType.REGULAR,CachedZoneIndex.getInstance().getZoneIndex());
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
                if (trx.getFrom().equals(regularTransaction.getFrom())) {
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
        Timestamp current = GetTime.GetTimestampFromString(regularTransaction.getTimestamp());
        Timestamp check = GetTime.GetTimeStampWithDelay();
        if (current.before(old)) {
            LOG.info("Transaction abort: Transaction timestamp is not a valid timestamp");
            regularTransaction.setStatus(StatusType.ABORT);
        }
        if (check.before(old)) {
            LOG.info("Transaction abort: Transaction timestamp is not older than one minute delay");
            regularTransaction.setStatus(StatusType.ABORT);
        }
    }

    @Override
    public void visit(RewardsTransaction rewardsTransaction) {
        HashMap<Integer, HashSet<Integer>> mapsearch;
        try {
            mapsearch = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(rewardsTransaction.getRecipientAddress()).get().retrieveAllTransactionsByOriginZone(PatriciaTreeTransactionType.REWARDS,CachedZoneIndex.getInstance().getZoneIndex());
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
                if (trx.getFrom().equals(rewardsTransaction.getRecipientAddress())) {
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
        Timestamp current = GetTime.GetTimestampFromString(rewardsTransaction.getTimestamp());
        Timestamp check = GetTime.GetTimeStampWithDelay();
        if (current.before(old)) {
            LOG.info("Transaction abort: Transaction timestamp is not a valid timestamp");
            rewardsTransaction.setStatus(StatusType.ABORT);
        }
        if (check.before(old)) {
            LOG.info("Transaction abort: Transaction timestamp is not older than one minute delay");
            rewardsTransaction.setStatus(StatusType.ABORT);
        }
    }

    @Override
    public void visit(StakingTransaction stakingTransaction) {
        HashMap<Integer, HashSet<Integer>> mapsearch;
        try {
            mapsearch = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(stakingTransaction.getValidatorAddress()).get().retrieveAllTransactionsByOriginZone(PatriciaTreeTransactionType.STAKING,CachedZoneIndex.getInstance().getZoneIndex());
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
                if (trx.getFrom().equals(stakingTransaction.getValidatorAddress())) {
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
        Timestamp current = GetTime.GetTimestampFromString(stakingTransaction.getTimestamp());
        Timestamp check = GetTime.GetTimeStampWithDelay();
        if (current.before(old)) {
            LOG.info("Transaction abort: Transaction timestamp is not a valid timestamp");
            stakingTransaction.setStatus(StatusType.ABORT);
        }
        if (check.before(old)) {
            LOG.info("Transaction abort: Transaction timestamp is not older than one minute delay");
            stakingTransaction.setStatus(StatusType.ABORT);
        }
    }

    @Override
    public void visit(DelegateTransaction delegateTransaction) {

    }

    @Override
    public void visit(UnclaimedFeeRewardTransaction unclaimedFeeRewardTransaction) {

    }
}
