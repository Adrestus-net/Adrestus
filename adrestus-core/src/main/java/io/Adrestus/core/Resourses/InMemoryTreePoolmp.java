package io.Adrestus.core.Resourses;

import io.Adrestus.core.Trie.MerklePatriciaTreeImp;
import io.Adrestus.core.Trie.PatriciaTreeNode;

import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

public class InMemoryTreePoolmp implements MemoryTreePool {
    private MerklePatriciaTreeImp patriciaTreeImp;
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();

    public InMemoryTreePoolmp() {
        patriciaTreeImp = new MerklePatriciaTreeImp();
    }

    @Override
    public boolean store(String address, PatriciaTreeNode patriciaTreeNode) {
        return false;
    }

    @Override
    public boolean update(String address, PatriciaTreeNode patriciaTreeNode) {
        return false;
    }

    @Override
    public Stream<PatriciaTreeNode> getAll() throws Exception {
        return null;
    }

    @Override
    public Optional<PatriciaTreeNode> getById(String address) throws Exception {
        System.out.println("waiting");
        r.lock();
        //try {
        System.out.println("Reading" + Thread.currentThread().getName());
        Thread.sleep(10000);
        r.unlock();
        //  }
        //finally { w.unlock(); }
        return Optional.empty();
    }
}
