package io.Adrestus.core;

import gnu.trove.map.hash.TLongLongHashMap;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class HashMapDB<V> {
    protected final TLongLongHashMap storage;
    protected ReadWriteLock rwLock = new ReentrantReadWriteLock();
    protected final Lock readLock = rwLock.readLock();
    protected final Lock writeLock = rwLock.writeLock();

    public HashMapDB() {
        this.storage = new TLongLongHashMap();
    }


    public void put(byte[] key,byte[]  val) {
        writeLock.lock();
        try {
            if (val == null) {
                delete(key);
            } else {
                ByteBuffer keybuff = ByteBuffer.allocate(key.length);
                ByteBuffer valuebuff = ByteBuffer.allocate(val.length);
                keybuff.put(key);
                valuebuff.put(val);
                keybuff.flip();//need flip
                valuebuff.flip();
                storage.put(keybuff.getLong(), valuebuff.getLong());
            }
        } finally {
            writeLock.unlock();
        }


    }


    public long get(byte[] key) {
        readLock.lock();
        try {
            ByteBuffer keybuff = ByteBuffer.allocate(key.length);
            keybuff.put(key);
            return storage.get(keybuff.getLong());
        } finally {
            readLock.unlock();
        }
    }


    public void delete(byte[] key) {
        writeLock.lock();
        try {
            ByteBuffer keybuff = ByteBuffer.allocate(key.length);
            keybuff.put(key);
            storage.remove(keybuff.getLong());
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




    public TLongLongHashMap getStorage() {
        return storage;
    }
}
