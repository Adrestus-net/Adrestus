package io.Adrestus.core.Resourses;

import io.Adrestus.core.Receipt;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MemoryReceiptPool implements IMemoryPool<Receipt> {
    private static Logger LOG = LoggerFactory.getLogger(MemoryReceiptPool.class);


    private static volatile IMemoryPool instance;

    private static Map<CompositeKey, Receipt> memorypool;
    private static ReentrantReadWriteLock rwl;
    private static Lock r;
    private static Lock w;
    private static ReceiptEqualityComparator equalityComparator;

    private MemoryReceiptPool() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        } else {
            this.memorypool = new HashMap<>();
            this.equalityComparator = new ReceiptEqualityComparator();
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
            return memorypool.values().stream();
        } finally {
            r.unlock();
        }
    }

    @Override
    public ArrayList<Receipt> getAll() throws Exception {
        r.lock();
        try {
            return (ArrayList<Receipt>) memorypool.values().stream().collect(Collectors.toList());
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
        return null;
    }

    @Override
    public List<Receipt> getListByZone(int zone) throws Exception {
        r.lock();
        try {
            List<Receipt> result = memorypool.values().parallelStream().filter(val -> val.getZoneFrom() == zone).collect(Collectors.toList());
            return result;
        } finally {
            r.unlock();
        }
    }

    @Override
    public List<Receipt> getOutBoundList(int zone) throws Exception {
        r.lock();
        try {
            List<Receipt> result = memorypool.values().stream().filter(val -> val.getZoneFrom() == zone && val.getZoneTo() != zone).collect(Collectors.toList());
            return result;
        } finally {
            r.unlock();
        }
    }

    @Override
    public List<Receipt> getInboundList(int zone) throws Exception {
        r.lock();
        try {
            List<Receipt> result = memorypool.values().parallelStream().filter(val -> val.getZoneFrom() != zone && val.getZoneTo() == zone).collect(Collectors.toList());
            return result;
        } finally {
            r.unlock();
        }
    }

    @Override
    public List<Receipt> getListToDelete(int zone) throws Exception {
        r.lock();
        try {
            List<Receipt> result = memorypool.values().parallelStream().filter(val -> val.getZoneFrom() != zone && val.getZoneTo() != zone).collect(Collectors.toList());
            return result;
        } finally {
            r.unlock();
        }
    }

    @Override
    public boolean add(Receipt receipt) {
        w.lock();
        try {
            return check_add(receipt);
        } finally {
            w.unlock();
        }
    }

    @Override
    public void delete(List<Receipt> list_transaction) {
        w.lock();
        try {
            list_transaction.forEach(receipt -> memorypool.remove(new CompositeKey(receipt.getZoneFrom(), receipt.getReceiptBlock().getGeneration(), receipt.getReceiptBlock().getHeight(), receipt.getPosition())));
        } finally {
            w.unlock();
        }
    }

    @Override
    public void delete(Receipt receipt) {
        w.lock();
        try {
            memorypool.remove(new CompositeKey(receipt.getZoneFrom(), receipt.getReceiptBlock().getGeneration(), receipt.getReceiptBlock().getHeight(), receipt.getPosition()));
        } finally {
            w.unlock();
        }
    }


    @Override
    public boolean checkAdressExists(Receipt transaction) throws Exception {
        return false;
    }

    @Override
    public boolean checkHashExists(Receipt receipt) throws Exception {
        r.lock();
        try {
            return memorypool.containsKey(new CompositeKey(receipt.getZoneFrom(), receipt.getReceiptBlock().getGeneration(), receipt.getReceiptBlock().getHeight(), receipt.getPosition()));
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
            memorypool.values().stream().forEach(val -> LOG.info(val.toString()));
        } finally {
            r.unlock();
        }
    }

    @Override
    public void clear() {
        memorypool.clear();
    }

    private boolean check_add(Receipt receipt) {
        CompositeKey compositeKey = new CompositeKey(receipt.getZoneFrom(), receipt.getReceiptBlock().getGeneration(), receipt.getReceiptBlock().getHeight(), receipt.getPosition());
        boolean isExist = memorypool.containsKey(compositeKey);
        if (isExist)
            return true;
        memorypool.put(compositeKey, receipt);
        return false;
    }

    private final class ReceiptEqualityComparator implements Comparator<Receipt> {
        @Override
        public int compare(Receipt t1, Receipt t2) {
            return Comparator.comparing(Receipt::getZoneFrom)
                    .thenComparingInt(val -> val.getReceiptBlock().getGeneration())
                    .thenComparingInt(val -> val.getReceiptBlock().getHeight())
                    .thenComparingInt(Receipt::getPosition)
                    .compare(t1, t2);
        }
    }


    private static final class CompositeKey implements Serializable,Cloneable {
        public final int zoneFrom;
        public final int generation;
        public final int height;
        public final int position;

        public CompositeKey(@Deserialize("zoneFrom") int zoneFrom, @Deserialize("generation") int generation, @Deserialize("height") int height, @Deserialize("position") int position) {
            this.zoneFrom = zoneFrom;
            this.generation = generation;
            this.height = height;
            this.position = position;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CompositeKey that = (CompositeKey) o;
            return zoneFrom == that.zoneFrom && generation == that.generation && height == that.height && position == that.position;
        }

        @Override
        public int hashCode() {
            return Objects.hash(zoneFrom, generation, height, position);
        }

        @Override
        public String toString() {
            return "CompositeKey{" +
                    "zoneFrom=" + zoneFrom +
                    ", generation=" + generation +
                    ", height=" + height +
                    ", position=" + position +
                    '}';
        }
    }
}
