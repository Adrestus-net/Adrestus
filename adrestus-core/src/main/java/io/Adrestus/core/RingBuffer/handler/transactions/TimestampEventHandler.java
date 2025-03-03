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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;

public class TimestampEventHandler extends TransactionEventHandler implements TransactionUnitVisitor {
    private static Logger LOG = LoggerFactory.getLogger(TimestampEventHandler.class);
    private ArrayList<Transaction> results;

    public TimestampEventHandler() {
    }

    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        Transaction transaction = transactionEvent.getTransaction();

        if (transaction.getStatus().equals(StatusType.BUFFERED) || transaction.getStatus().equals(StatusType.ABORT))
            return;


        this.results = new ArrayList<>();

        transaction.accept(this);

        if (transaction.getType().equals(TransactionType.UNCLAIMED_FEE_REWARD))
            return;

        if (results.isEmpty())
            return;

        try {
            results.sort((u1, u2) -> GetTime.GetTimestampFromString(u2.getTimestamp()).compareTo(GetTime.GetTimestampFromString(u1.getTimestamp())));
            Instant old = GetTime.GetTimestampFromString(results.getFirst().getTimestamp());
            Instant current = GetTime.GetTimestampFromString(transaction.getTimestamp());
            Instant check = GetTime.GetTimeStampWithDelay();
            if (current.isBefore(old) || current.equals(old)) {
                Optional.of("Transaction abort: Transaction timestamp is not a valid timestamp").ifPresent(val -> {
                    LOG.info(val);
                    transaction.infos(val);
                });
                transaction.setStatus(StatusType.ABORT);
            }
            if (check.isBefore(old) || check.equals(old)) {
                Optional.of("Transaction abort: Transaction timestamp is not older than one minute delay").ifPresent(val -> {
                    LOG.info(val);
                    transaction.infos(val);
                });
                transaction.setStatus(StatusType.ABORT);
            }
        } catch (Exception e) {
            Optional.of("Transaction abort: Transaction timestamp is not set abort").ifPresent(val -> {
                LOG.info(val);
                transaction.infos(val);
            });
            transaction.setStatus(StatusType.ABORT);
        } finally {
            this.results.clear();
            this.results = null;
        }

    }

    @Override
    public void visit(RegularTransaction regularTransaction) {
        HashMap<Integer, HashSet<Integer>> mapsearch;
        try {
            mapsearch = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(regularTransaction.getFrom()).get().retrieveAllTransactionsByOriginZone(PatriciaTreeTransactionType.REGULAR, CachedZoneIndex.getInstance().getZoneIndex());
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

    }

    @Override
    public void visit(RewardsTransaction rewardsTransaction) {
        HashMap<Integer, HashSet<Integer>> mapsearch;
        try {
            mapsearch = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(rewardsTransaction.getRecipientAddress()).get().retrieveAllTransactionsByOriginZone(PatriciaTreeTransactionType.REWARDS, CachedZoneIndex.getInstance().getZoneIndex());
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

        IDatabase<String, TransactionBlock> transactionBlockIDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
        mapsearch.get(max).forEach(value -> {
            try {
                RewardsTransaction trx = (RewardsTransaction) transactionBlockIDatabase.findByKey(String.valueOf(max)).get().getTransactionList().get(value);
                if (trx.getRecipientAddress().equals(rewardsTransaction.getRecipientAddress())) {
                    results.add(trx);
                }
            } catch (NoSuchElementException e) {
            } catch (IndexOutOfBoundsException e) {
            } catch (ClassCastException e) {
            }
        });

    }

    @Override
    public void visit(StakingTransaction stakingTransaction) {
        HashMap<Integer, HashSet<Integer>> mapsearch;
        try {
            mapsearch = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(stakingTransaction.getValidatorAddress()).get().retrieveAllTransactionsByOriginZone(PatriciaTreeTransactionType.STAKING, CachedZoneIndex.getInstance().getZoneIndex());
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

        IDatabase<String, TransactionBlock> transactionBlockIDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
        mapsearch.get(max).forEach(value -> {
            try {
                StakingTransaction trx = (StakingTransaction) transactionBlockIDatabase.findByKey(String.valueOf(max)).get().getTransactionList().get(value);
                if (trx.getValidatorAddress().equals(stakingTransaction.getValidatorAddress())) {
                    results.add(trx);
                }
            } catch (NoSuchElementException e) {
            } catch (IndexOutOfBoundsException e) {
            }
        });
    }

    @Override
    public void visit(DelegateTransaction delegateTransaction) {
        HashMap<Integer, HashSet<Integer>> mapsearch;
        try {
            mapsearch = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(delegateTransaction.getDelegatorAddress()).get().retrieveAllTransactionsByOriginZone(PatriciaTreeTransactionType.DELEGATE, CachedZoneIndex.getInstance().getZoneIndex());
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

        IDatabase<String, TransactionBlock> transactionBlockIDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
        mapsearch.get(max).forEach(value -> {
            try {
                DelegateTransaction trx = (DelegateTransaction) transactionBlockIDatabase.findByKey(String.valueOf(max)).get().getTransactionList().get(value);
                if (trx.getDelegatorAddress().equals(delegateTransaction.getDelegatorAddress())) {
                    results.add(trx);
                }
            } catch (NoSuchElementException e) {
            } catch (IndexOutOfBoundsException e) {
            }
        });
    }

    @Override
    public void visit(UnclaimedFeeRewardTransaction unclaimedFeeRewardTransaction) {

    }

    @Override
    public void visit(UnDelegateTransaction unDelegateTransaction) {
        HashMap<Integer, HashSet<Integer>> mapsearch;
        try {
            mapsearch = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(unDelegateTransaction.getDelegatorAddress()).get().retrieveAllTransactionsByOriginZone(PatriciaTreeTransactionType.UNDELEGATE, CachedZoneIndex.getInstance().getZoneIndex());
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

        IDatabase<String, TransactionBlock> transactionBlockIDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
        mapsearch.get(max).forEach(value -> {
            try {
                UnDelegateTransaction trx = (UnDelegateTransaction) transactionBlockIDatabase.findByKey(String.valueOf(max)).get().getTransactionList().get(value);
                if (trx.getDelegatorAddress().equals(unDelegateTransaction.getDelegatorAddress())) {
                    results.add(trx);
                }
            } catch (NoSuchElementException e) {
            } catch (IndexOutOfBoundsException e) {
            }
        });
    }

    @Override
    public void visit(UnstakingTransaction unstakingTransaction) {
        HashMap<Integer, HashSet<Integer>> mapsearch;
        try {
            mapsearch = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(unstakingTransaction.getValidatorAddress()).get().retrieveAllTransactionsByOriginZone(PatriciaTreeTransactionType.UNSTAKING, CachedZoneIndex.getInstance().getZoneIndex());
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

        IDatabase<String, TransactionBlock> transactionBlockIDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
        mapsearch.get(max).forEach(value -> {
            try {
                StakingTransaction trx = (StakingTransaction) transactionBlockIDatabase.findByKey(String.valueOf(max)).get().getTransactionList().get(value);
                if (trx.getValidatorAddress().equals(unstakingTransaction.getValidatorAddress())) {
                    results.add(trx);
                }
            } catch (NoSuchElementException e) {
            } catch (IndexOutOfBoundsException e) {
            }
        });
    }
}
