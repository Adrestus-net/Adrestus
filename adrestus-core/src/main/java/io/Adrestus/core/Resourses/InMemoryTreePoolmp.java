package io.Adrestus.core.Resourses;

import io.Adrestus.core.Trie.MerklePatriciaTreeImp;
import io.Adrestus.core.Trie.PatriciaTreeNode;
import io.Adrestus.util.SerializationUtil;
import org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class InMemoryTreePoolmp implements MemoryTreePool {
    private MerklePatriciaTreeImp patriciaTreeImp;
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();
    private final SerializationUtil<PatriciaTreeNode> wrapper;

    public InMemoryTreePoolmp() {
        this.patriciaTreeImp = new MerklePatriciaTreeImp();
        this.wrapper = new SerializationUtil<>(PatriciaTreeNode.class);
    }

    @Override
    public void store(String address, PatriciaTreeNode patriciaTreeNode) {
        w.lock();
        try {
            byte key[] = address.getBytes(StandardCharsets.UTF_8);
            byte value[] = wrapper.encode(patriciaTreeNode);
            patriciaTreeImp.put(key, value);
        } finally {
            w.unlock();
        }
    }

    @Override
    public void update(String address, PatriciaTreeNode patriciaTreeNode) {
        w.lock();
        try {
            byte key[] = address.getBytes(StandardCharsets.UTF_8);
            byte value[] = wrapper.encode(patriciaTreeNode);
            patriciaTreeImp.put(key, value);
        } finally {
            w.unlock();
        }
    }


    @Override
    public Optional<PatriciaTreeNode> getByaddress(String address) throws Exception {
        r.lock();
        try {
            byte key[] = address.getBytes(StandardCharsets.UTF_8);
            byte value[] = patriciaTreeImp.get(key);
            PatriciaTreeNode patriciaTreeNode = wrapper.decode(value);
            return Optional.of(patriciaTreeNode);
        } finally {
            r.unlock();
        }
    }

    @Override
    public String getRootHash() throws Exception {
        return Hex.encodeHexString(patriciaTreeImp.getRootHash());
    }
}
