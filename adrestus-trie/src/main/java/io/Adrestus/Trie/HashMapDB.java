package io.Adrestus.Trie;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class HashMapDB<V> {
    protected final Map<byte[], byte[]> storage;
    protected ReadWriteLock rwLock = new ReentrantReadWriteLock();
    protected final Lock readLock = rwLock.readLock();
    protected final Lock writeLock = rwLock.writeLock();

    public HashMapDB() {
        this.storage = new HashMap<byte[], byte[]>();
    }


    public void put(byte[] key, byte[] val) {
        writeLock.lock();
        try {
            if (val == null) {
                delete(key);
            } else {
                storage.put(key, val);
            }
        } finally {
            writeLock.unlock();
        }


    }


    public byte[] get(byte[] key) {
        readLock.lock();
        try {
            return storage.get(key);
        } finally {
            readLock.unlock();
        }
    }


    public void delete(byte[] key) {
        writeLock.lock();
        try {
            storage.remove(key);
        } finally {
            writeLock.unlock();
        }
    }


    public boolean flush() {
        return true;
    }


    public void setName(String name) {
    }


    public String getName() {
        return "in-memory";
    }


    public void init() {
    }


    public boolean isAlive() {
        return true;
    }


    public void close() {
    }


    public void reset() {
        writeLock.lock();
        try {
            storage.clear();
        } finally {
            writeLock.unlock();
        }
    }


    public Map<byte[], byte[]> getStorage() {
        return storage;
    }
}
