package io.Adrestus.core.Resourses;


import io.Adrestus.core.Transaction;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MemoryTransactionPool implements IMemoryPool<Transaction> {
    private static Logger LOG = LoggerFactory.getLogger(MemoryTransactionPool.class);


    private static volatile IMemoryPool instance;

    private final List<Transaction> memorypool;
    private final List<Transaction> addressmemorypool;
    private final TransactionHashComparator hashComparator;
    private final TransactionAddressComparator transactionAddressComparator;
    private final SerializationUtil<Transaction> wrapper;
    private final ReentrantReadWriteLock rwl;
    private final Lock r;
    private final Lock w;

    private MemoryTransactionPool() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        } else {
            List<SerializationUtil.Mapping> list = new ArrayList<>();
            list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
            this.memorypool = new ArrayList<Transaction>();
            this.addressmemorypool = new ArrayList<Transaction>();
            this.wrapper = new SerializationUtil<>(Transaction.class, list);
            this.hashComparator = new TransactionHashComparator();
            this.transactionAddressComparator = new TransactionAddressComparator();
            this.rwl = new ReentrantReadWriteLock();
            this.r = rwl.readLock();
            this.w = rwl.writeLock();
        }
    }

    public static IMemoryPool getInstance() {
        var result = instance;
        if (result == null) {
            synchronized (MemoryTransactionPool.class) {
                result = instance;
                if (result == null) {
                    instance = result = new MemoryTransactionPool();
                }
            }
        }
        return result;
    }

    @Override
    public Stream<Transaction> getAllStream() throws Exception {
        r.lock();
        try {
            return memorypool.stream();
        } finally {
            r.unlock();
        }
    }

    @Override
    public List<Transaction> getAll() throws Exception {
        r.lock();
        try {
            return memorypool;
        } finally {
            r.unlock();
        }
    }

    @Override
    public int getSize() {
        r.lock();
        try {
            return memorypool.size();
        } finally {
            r.unlock();
        }
    }


    @Override
    public Optional<Transaction> getObjectByHash(String hash) throws Exception {
        r.lock();
        try {
            Optional<Transaction> result = memorypool.stream().filter(val -> val.getHash().equals(hash)).findFirst();
            return result;
        } finally {
            r.unlock();
        }
    }

    @Override
    public List<Transaction> getListByZone(int zone) throws Exception {
        r.lock();
        try {
            List<Transaction> result = memorypool.stream().filter(val -> val.getZoneFrom() == zone).collect(Collectors.toList());
            return result;
        } finally {
            r.unlock();
        }
    }

    @Override
    public List<Transaction> getListNotByZone(int zone) throws Exception {
        r.lock();
        try {
            List<Transaction> result = memorypool.stream().filter(val -> val.getZoneFrom() != zone).collect(Collectors.toList());
            return result;
        } finally {
            r.unlock();
        }
    }

    @Override
    public boolean add(Transaction transaction) {
        w.lock();
        try {
            boolean val = memorypool_add(transaction);
            boolean val1 = addressmemorypool(transaction);

            return (val && val1) ? true : false;
        } finally {
            w.unlock();
        }
    }


    @Override
    public void delete(List<Transaction> list_transaction) {
        w.lock();
        try {
            memorypool.removeAll(list_transaction);
            addressmemorypool.removeAll(list_transaction);
        } finally {
            w.unlock();
        }
    }

    @Override
    public void clear() {
        this.memorypool.clear();
        this.addressmemorypool.clear();
    }

    @Override
    public void delete(Transaction transaction) {
        w.lock();
        try {
            int index = Collections.binarySearch(memorypool, transaction, hashComparator);
            int index1 = Collections.binarySearch(addressmemorypool, transaction, transactionAddressComparator);
            if (index >= 0 && index1 >= 0) {
                memorypool.remove(index);
                addressmemorypool.remove(index1);
            }
        } finally {
            w.unlock();
        }
    }

    @Override
    public boolean checkAdressExists(Transaction transaction) throws Exception {
        r.lock();
        try {
            int index = Collections.binarySearch(addressmemorypool, transaction, transactionAddressComparator);
            if (index >= 0)
                return true;
            else
                return false;
        } finally {
            r.unlock();
        }
    }

    @Override
    public boolean checkHashExists(Transaction transaction) throws Exception {
        r.lock();
        try {
            int index = Collections.binarySearch(memorypool, transaction, hashComparator);
            if (index >= 0)
                return true;
            else
                return false;
        } finally {
            r.unlock();
        }
    }

    @Override
    public boolean checkTimestamp(Transaction t2) throws Exception {
        r.lock();
        try {
            final int[] index = {-1};
            memorypool.stream().forEach(t1 -> {
                if (t1.getFrom().equals(t2.getFrom()) && !t1.getHash().equals(t2.getHash())) {
                    int val2 = Integer.valueOf(t1.getZoneFrom()).compareTo(Integer.valueOf(t2.getZoneFrom()));

                    Timestamp time1 = GetTime.GetTimestampFromString(t1.getTimestamp());
                    Timestamp time2 = GetTime.GetTimestampFromString(t2.getTimestamp());

                    int val3 = time1.compareTo(time2);

                    index[0] = (val2 == 0 && val3 < 0) ? 0 : -1;
                }
            });
            return index[0] >= 0;
        } finally {
            r.unlock();
        }
    }

    @Override
    public void printAll() throws Exception {
        r.lock();
        try {
            for (int i = 0; i < memorypool.size(); i++) {
                LOG.info(memorypool.get(i).toString());
            }
        } finally {
            r.unlock();
        }
    }

    public static Logger getLOG() {
        return LOG;
    }


    public TransactionHashComparator getHashComparator() {
        return hashComparator;
    }

    public SerializationUtil<Transaction> getWrapper() {
        return wrapper;
    }

    public ReentrantReadWriteLock getRwl() {
        return rwl;
    }

    public Lock getR() {
        return r;
    }

    public Lock getW() {
        return w;
    }


    private boolean memorypool_add(Transaction transaction) {
        int index = Collections.binarySearch(memorypool, transaction, hashComparator);
        if (index >= 0)
            return true;
        else if (index < 0) index = ~index;
        memorypool.add(index, transaction);
        return false;
    }

    private boolean addressmemorypool(Transaction transaction) {
        int index = Collections.binarySearch(addressmemorypool, transaction, transactionAddressComparator);
        if (index >= 0)
            return true;
        else if (index < 0) index = ~index;
        addressmemorypool.add(index, transaction);
        return false;
    }


    private final class TransactionHashComparator implements Comparator<Transaction> {
        @Override
        public int compare(Transaction t1, Transaction t2) {
            return t1.getHash().compareTo(t2.getHash());
        }
    }


    private final class TransactionAddressComparator implements Comparator<Transaction> {
        @SneakyThrows
        @Override
        public int compare(Transaction t1, Transaction t2) {
            try {
                return t1.getFrom().compareTo(t2.getFrom());
            } catch (Exception e) {
                LOG.info(e.toString());
                return 0;
            }
        }
    }
}
