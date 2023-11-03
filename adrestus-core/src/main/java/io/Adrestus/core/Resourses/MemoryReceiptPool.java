package io.Adrestus.core.Resourses;

import io.Adrestus.core.Receipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MemoryReceiptPool implements IMemoryPool<Receipt> {
    private static Logger LOG = LoggerFactory.getLogger(MemoryReceiptPool.class);


    private static volatile IMemoryPool instance;

    private final List<Receipt> memorypool;
    private final ReentrantReadWriteLock rwl;
    private final Lock r;
    private final Lock w;
    private final TransactionHashComparator hashComparator;

    private MemoryReceiptPool() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        } else {
            this.memorypool = new ArrayList<>();
            this.hashComparator = new TransactionHashComparator();
            this.rwl = new ReentrantReadWriteLock();
            this.r = rwl.readLock();
            this.w = rwl.writeLock();
        }
    }

    public static IMemoryPool getInstance() {
        var result = instance;
        if (result == null) {
            synchronized (MemoryReceiptPool.class) {
                result = instance;
                if (result == null) {
                    instance = result = new MemoryReceiptPool();
                }
            }
        }
        return result;
    }

    @Override
    public Stream<Receipt> getAllStream() throws Exception {
        r.lock();
        try {
            return memorypool.stream();
        } finally {
            r.unlock();
        }
    }

    @Override
    public List<Receipt> getAll() throws Exception {
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
    public Lock getR() {
        return null;
    }

    @Override
    public Lock getW() {
        return null;
    }

    @Override
    public Optional<Receipt> getObjectByHash(String hash) throws Exception {
        r.lock();
        try {
            Optional<Receipt> result = memorypool.stream().filter(val -> val.getTransaction().getHash().equals(hash)).findFirst();
            return result;
        } finally {
            r.unlock();
        }
    }

    @Override
    public List<Receipt> getListByZone(int zone) throws Exception {
        r.lock();
        try {
            List<Receipt> result = memorypool.stream().filter(val -> val.getTransaction().getZoneTo() == zone).collect(Collectors.toList());
            return result;
        } finally {
            r.unlock();
        }
    }

    @Override
    public List<Receipt> getListNotByZone(int zone) throws Exception {
        r.lock();
        try {
            List<Receipt> result = memorypool.stream().filter(val -> val.getTransaction().getZoneTo() != zone).collect(Collectors.toList());
            return result;
        } finally {
            r.unlock();
        }
    }

    @Override
    public boolean add(Receipt transaction) {
        w.lock();
        try {
            if (transaction.getZoneTo() != CachedZoneIndex.getInstance().getZoneIndex())
                return false;
            return check_add(transaction);
        } finally {
            w.unlock();
        }
    }

    @Override
    public void delete(List<Receipt> list_transaction) {
        w.lock();
        try {
            memorypool.removeAll(list_transaction);
        } finally {
            w.unlock();
        }
    }

    @Override
    public void delete(Receipt transaction) {
        w.lock();
        try {
            int index = Collections.binarySearch(memorypool, transaction, hashComparator);
            if (index >= 0)
                memorypool.remove(index);
        } finally {
            w.unlock();
        }
    }


    @Override
    public boolean checkAdressExists(Receipt transaction) throws Exception {
        return false;
    }

    @Override
    public boolean checkHashExists(Receipt transaction) throws Exception {
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
    public boolean checkTimestamp(Receipt transaction) throws Exception {
        return false;
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

    @Override
    public void clear() {

        memorypool.clear();
    }

    private boolean check_add(Receipt transaction) {
        int index = Collections.binarySearch(memorypool, transaction, hashComparator);
        if (index >= 0)
            return true;
        else if (index < 0) index = ~index;
        memorypool.add(index, transaction);
        return false;
    }

    private final class TransactionHashComparator implements Comparator<Receipt> {
        @Override
        public int compare(Receipt t1, Receipt t2) {
            return t1.getTransaction().getHash().compareTo(t2.getTransaction().getHash());
        }
    }
}
