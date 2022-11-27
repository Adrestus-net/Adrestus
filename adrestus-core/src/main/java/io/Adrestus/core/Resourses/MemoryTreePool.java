package io.Adrestus.core.Resourses;

import io.Adrestus.core.Trie.PatriciaTreeNode;
import io.Adrestus.core.Trie.optimize64_trie.IMerklePatriciaTrie;
import io.Adrestus.core.Trie.optimize64_trie.MerklePatriciaTrie;
import org.apache.tuweni.bytes.Bytes;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

public class MemoryTreePool implements IMemoryTreePool {

    private static volatile IMemoryTreePool instance;


    private final IMerklePatriciaTrie<Bytes, PatriciaTreeNode> patriciaTreeImp;
    private final Function<PatriciaTreeNode, Bytes> valueSerializer;
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();

    private MemoryTreePool() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        } else {
            this.valueSerializer = value -> (value != null) ? Bytes.wrap("".getBytes(StandardCharsets.UTF_8)) : null;
            this.patriciaTreeImp = new MerklePatriciaTrie<Bytes, PatriciaTreeNode>(valueSerializer);
        }
    }


    public static IMemoryTreePool getInstance() {
        var result = instance;
        if (result == null) {
            synchronized (MemoryTreePool.class) {
                result = instance;
                if (result == null) {
                    instance = result = new MemoryTreePool();
                }
            }
        }
        return result;
    }

    @Override
    public void store(String address, PatriciaTreeNode patriciaTreeNode) {
        w.lock();
        try {
            Bytes key = Bytes.wrap(address.getBytes(StandardCharsets.UTF_8));
            patriciaTreeImp.put(key, patriciaTreeNode);
        } finally {
            w.unlock();
        }
    }

    @Override
    public void update(String address, PatriciaTreeNode patriciaTreeNode) {
        w.lock();
        try {
            Bytes key = Bytes.wrap(address.getBytes(StandardCharsets.UTF_8));
            patriciaTreeImp.put(key, patriciaTreeNode);
        } finally {
            w.unlock();
        }
    }


    @Override
    public Optional<PatriciaTreeNode> getByaddress(String address) {
        r.lock();
        try {
            Bytes key = Bytes.wrap(address.getBytes(StandardCharsets.UTF_8));
            return patriciaTreeImp.get(key);
        } finally {
            r.unlock();
        }
    }

    @Override
    public String getRootHash() throws Exception {
        return patriciaTreeImp.getRootHash().toHexString();
    }
}
