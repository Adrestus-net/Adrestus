package io.Adrestus.core.Resourses;


import io.Adrestus.core.Transaction;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

public class InMemoryDao implements MemoryPool {
    private static Logger LOG = LoggerFactory.getLogger(InMemoryDao.class);

    private final List<Transaction> memorypool;
    private final TransactionHashComparator hashComparator;
    private final TransactionReplayComparator transactionReplayComparator;
    private final SerializationUtil<Transaction> wrapper;
    private final ReentrantReadWriteLock rwl;
    private final Lock r;
    private final Lock w;

    public InMemoryDao() {
        this.memorypool = new ArrayList<Transaction>();
        this.wrapper = new SerializationUtil<>(Transaction.class);
        this.hashComparator = new TransactionHashComparator();
        this.transactionReplayComparator = new TransactionReplayComparator();
        this.rwl = new ReentrantReadWriteLock();
        this.r = rwl.readLock();
        this.w = rwl.writeLock();
    }

    @Override
    public Stream<Transaction> getAll() throws Exception {
        return memorypool.stream();
    }

    @Override
    public Optional<Transaction> getTransactionByHash(String hash) throws Exception {
        Optional<Transaction> result = memorypool.stream().filter(val -> val.getHash().equals(hash)).findFirst();
        return result;
    }

    @Override
    public boolean add(Transaction transaction) throws Exception {
        return check_add(transaction);
    }


    @Override
    public void delete(List<Transaction> list_transaction) throws Exception {
        memorypool.removeAll(list_transaction);
    }

    @Override
    public boolean checkHashExists(Transaction transaction) throws Exception {
        int index = Collections.binarySearch(memorypool, transaction, hashComparator);
        if (index >= 0)
            return true;
        else
            return false;
    }

    @Override
    public boolean checkTimestamp(Transaction transaction) throws Exception {
        int index = Collections.binarySearch(memorypool, transaction, transactionReplayComparator);
        if (index >= 0)
            return true;
        else
            return false;
    }

    @Override
    public void printAll() throws Exception {
        for (int i = 0; i < memorypool.size(); i++) {
            LOG.info(memorypool.get(i).toString());
        }
    }

    private boolean check_add(Transaction transaction) {
        int index = Collections.binarySearch(memorypool, transaction, hashComparator);
        if (index >= 0)
            return true;
        else if (index < 0) index = ~index;
        memorypool.add(index, transaction);
        return false;
    }


    private final class TransactionHashComparator implements Comparator<Transaction> {
        @Override
        public int compare(Transaction t1, Transaction t2) {
            return t1.getHash().compareTo(t2.getHash());
        }
    }

    private final class TransactionReplayComparator implements Comparator<Transaction> {
        @SneakyThrows
        @Override
        public int compare(Transaction t1, Transaction t2) {
            int val1 = t1.getFrom().compareTo(t2.getFrom());
            int val2 = Integer.valueOf(t1.getZoneFrom()).compareTo(Integer.valueOf(t2.getZoneFrom()));

            Timestamp time1 = GetTime.GetTimestampFromString(t1.getTimestamp());
            Timestamp time2 = GetTime.GetTimestampFromString(t2.getTimestamp());

            int val3 = time1.compareTo(time2);

            return (val1 ==0  && val2==0 && val3 < 0) ? val1 : -1;
        }
    }
}
