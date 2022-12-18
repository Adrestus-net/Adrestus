package io.Adrestus;

import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.Trie.optimize64_trie.IMerklePatriciaTrie;
import io.Adrestus.Trie.optimize64_trie.MerklePatriciaTrie;
import org.apache.tuweni.bytes.Bytes;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

public class MemoryTreePool implements IMemoryTreePool {


    private final IMerklePatriciaTrie<Bytes, PatriciaTreeNode> patriciaTreeImp;
    private final Function<PatriciaTreeNode, Bytes> valueSerializer;
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();

    public MemoryTreePool() {
        this.valueSerializer = value -> (value != null) ? Bytes.wrap("".getBytes(StandardCharsets.UTF_8)) : null;
        this.patriciaTreeImp = new MerklePatriciaTrie<Bytes, PatriciaTreeNode>(valueSerializer);
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
    public void deposit(String address, PatriciaTreeNode patriciaTreeNode) {
        w.lock();
        try {
            Bytes key = Bytes.wrap(address.getBytes(StandardCharsets.UTF_8));
            Optional<PatriciaTreeNode> prev = getByaddress(address);
            if (prev.isEmpty()) {
                PatriciaTreeNode next = new PatriciaTreeNode(patriciaTreeNode.getAmount(), 0, 0);
                patriciaTreeImp.put(key, next);
            } else {
                Double amount = prev.get().getAmount() + patriciaTreeNode.getAmount();
                patriciaTreeNode.setAmount(amount);
                patriciaTreeNode.setNonce(patriciaTreeNode.getNonce() + 1);
                patriciaTreeImp.put(key, patriciaTreeNode);
            }
        } finally {
            w.unlock();
        }
    }

    @Override
    public void withdraw(String address, PatriciaTreeNode patriciaTreeNode) {
        w.lock();
        try {
            Bytes key = Bytes.wrap(address.getBytes(StandardCharsets.UTF_8));
            Optional<PatriciaTreeNode> prev = getByaddress(address);
            if (prev.isEmpty()) {
                PatriciaTreeNode next = new PatriciaTreeNode(patriciaTreeNode.getAmount(), 0, 0);
                patriciaTreeImp.put(key, patriciaTreeNode);
            } else {
                Double amount = prev.get().getAmount() - patriciaTreeNode.getAmount();
                patriciaTreeNode.setAmount(amount);
                patriciaTreeNode.setNonce(patriciaTreeNode.getNonce() + 1);
                patriciaTreeImp.put(key, patriciaTreeNode);
            }
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

    @Override
    public IMerklePatriciaTrie<Bytes, PatriciaTreeNode> getTrie() {
        return this.patriciaTreeImp;
    }

}
