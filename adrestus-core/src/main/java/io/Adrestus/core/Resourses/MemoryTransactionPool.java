package io.Adrestus.core.Resourses;


import io.Adrestus.core.Transaction;
import io.Adrestus.crypto.elliptic.mapper.BigDecimalSerializer;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
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

    private final HashMap<String, LinkedHashMap<String, Transaction>> memorypool;
    private final SerializationUtil<Transaction> wrapper;
    private final ReentrantReadWriteLock rwl;
    private final Lock r;
    private final Lock w;

    private MemoryTransactionPool() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        } else {
            List<SerializationUtil.Mapping> list = new ArrayList<>();
            list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
            list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
            this.memorypool = new HashMap<String, LinkedHashMap<String, Transaction>>();
            this.wrapper = new SerializationUtil<>(Transaction.class, list);
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
            return memorypool.values().stream().map(LinkedHashMap::values).collect(Collectors.toList()).stream().flatMap(Collection::stream);
        } finally {
            r.unlock();
        }
    }

    @Override
    public List<Transaction> getAll() throws Exception {
        r.lock();
        try {
            return memorypool.values().stream().map(LinkedHashMap::values).collect(Collectors.toList()).stream().flatMap(Collection::stream).collect(Collectors.toList());
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
    public int getFromSize(String from) {
        r.lock();
        try {
            if (memorypool.containsKey(from))
                return memorypool.get(from).size();
            return 0;
        } finally {
            r.unlock();
        }
    }


    @Override
    public Optional<Transaction> getObjectByHash(String hash) throws Exception {
        r.lock();
        try {
            for (Map.Entry<String, LinkedHashMap<String, Transaction>> from_entry : memorypool.entrySet()) {
                return Optional.of(from_entry.getValue().get(hash));
            }
        } finally {
            r.unlock();
        }
        return Optional.empty();
    }

    @Override
    public List<Transaction> getListByZone(int zone) throws Exception {
        r.lock();
        try {
            return memorypool.values().stream().map(LinkedHashMap::values).collect(Collectors.toList()).stream().flatMap(Collection::stream).filter(val -> val.getZoneFrom() == zone).collect(Collectors.toList());
        } finally {
            r.unlock();
        }
    }

    @Override
    public List<Transaction> getOutBoundList(int zone) throws Exception {
        r.lock();
        try {
            return memorypool.values().stream().map(LinkedHashMap::values).collect(Collectors.toList()).stream().flatMap(Collection::stream).filter(val -> val.getZoneFrom() == zone && val.getZoneTo() != zone).collect(Collectors.toList());
        } finally {
            r.unlock();
        }
    }

    @Override
    public List<Transaction> getInboundList(int zone) throws Exception {
        r.lock();
        try {
            return memorypool.values().stream().map(LinkedHashMap::values).collect(Collectors.toList()).stream().flatMap(Collection::stream).filter(val -> val.getZoneFrom() != zone && val.getZoneTo() == zone).collect(Collectors.toList());
        } finally {
            r.unlock();
        }
    }

    @Override
    public List<Transaction> getListToDelete(int zone) throws Exception {
        r.lock();
        try {
            return memorypool.values().stream().map(LinkedHashMap::values).collect(Collectors.toList()).stream().flatMap(Collection::stream).filter(val -> val.getZoneFrom() != zone && val.getZoneTo() != zone).collect(Collectors.toList());
        } finally {
            r.unlock();
        }
    }

    @Override
    public boolean add(Transaction transaction) {
        w.lock();
        try {
            if (memorypool.containsKey(transaction.getFrom())) {
                LinkedHashMap<String, Transaction> hashMap = memorypool.get(transaction.getFrom());
                if (hashMap.containsKey(transaction.getHash())) {
                    return true;
                } else {
                    hashMap.put(transaction.getHash(), transaction);
                }
            } else {
                LinkedHashMap<String, Transaction> hashMap = new LinkedHashMap<String, Transaction>();
                hashMap.put(transaction.getHash(), transaction);
                memorypool.put(transaction.getFrom(), hashMap);
            }

            return false;
        } finally {
            w.unlock();
        }
    }


    @Override
    public void delete(List<Transaction> list_transaction) {
        w.lock();
        try {
            memorypool.keySet().removeAll(list_transaction.stream().map(Transaction::getFrom).collect(Collectors.toList()));
        } finally {
            w.unlock();
        }
    }

    @Override
    public void clear() {
        this.memorypool.clear();
    }

    @Override
    public void delete(Transaction transaction) {
        w.lock();
        try {
            if (memorypool.containsKey(transaction.getFrom())) {
                LinkedHashMap<String, Transaction> hashMap = memorypool.get(transaction.getFrom());
                if (hashMap.containsKey(transaction.getHash())) {
                    hashMap.remove(transaction.getHash());
                }
            }
        } finally {
            w.unlock();
        }
    }

    @Override
    public boolean checkAdressExists(Transaction transaction) throws Exception {
        r.lock();
        try {
            if (memorypool.containsKey(transaction.getFrom()))
                return true;

            return false;
        } finally {
            r.unlock();
        }
    }

    @Override
    public boolean checkHashExists(Transaction transaction) throws Exception {
        r.lock();
        try {
            if (memorypool.containsKey(transaction.getFrom())) {
                LinkedHashMap<String, Transaction> hashMap = memorypool.get(transaction.getFrom());
                if (hashMap.containsKey(transaction.getHash())) {
                    return true;
                }
            }
            return false;
        } finally {
            r.unlock();
        }
    }

    @Override
    public boolean checkTimestamp(Transaction t2) throws Exception {
        r.lock();
        try {
            if (memorypool.containsKey(t2.getFrom())) {
                LinkedHashMap<String, Transaction> hashMap = memorypool.get(t2.getFrom());
                List<Transaction> list = hashMap.values().stream().collect(Collectors.toList());
                Transaction transaction = list.get(list.size() - 1);
                int val2 = Integer.valueOf(transaction.getZoneFrom()).compareTo(Integer.valueOf(t2.getZoneFrom()));
                Timestamp time1 = GetTime.GetTimestampFromString(transaction.getTimestamp());
                Timestamp time2 = GetTime.GetTimestampFromString(t2.getTimestamp());

                int val3 = time1.compareTo(time2);
                if (val2 == 0 && val3 < 0)
                    return true;
            }
            return false;
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


}
