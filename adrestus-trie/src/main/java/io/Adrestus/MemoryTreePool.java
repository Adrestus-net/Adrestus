package io.Adrestus;

import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.Trie.optimize64_trie.IMerklePatriciaTrie;
import io.Adrestus.Trie.optimize64_trie.MerklePatriciaTrie;
import org.apache.tuweni.bytes.Bytes;

import java.nio.charset.StandardCharsets;
import io.vavr.control.Option;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MemoryTreePool implements IMemoryTreePool {


    private IMerklePatriciaTrie<Bytes, PatriciaTreeNode> patriciaTreeImp;
    private SerializableFunction<PatriciaTreeNode, Bytes> valueSerializer;
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();

    public MemoryTreePool() {
        this.valueSerializer = value -> (value != null) ? Bytes.wrap("".getBytes(StandardCharsets.UTF_8)) : null;
        //this.valueSerializer=null;
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


    //be aware that print functionality is  different
    @Override
    public void deposit(String address, double amount, IMemoryTreePool instance) {
        w.lock();
        try {
            Bytes key = Bytes.wrap(address.getBytes(StandardCharsets.UTF_8));
            Option<PatriciaTreeNode> prev = instance.getByaddress(address);
            if (prev.isEmpty()) {
                PatriciaTreeNode next = new PatriciaTreeNode(amount, 0, 0);
                patriciaTreeImp.put(key, next);
            } else {
                Double new_cash = prev.get().getAmount() + amount;
                // System.out.println("Deposit "+address+ " "+prev.get().getAmount()+" + "+patriciaTreeNode.getAmount()+" = "+amount);
                prev.get().setAmount(new_cash);
                patriciaTreeImp.put(key, prev.get());
            }
        } finally {
            w.unlock();
        }
    }

    //be aware that print functionality is  different
    @Override
    public void withdraw(String address, double amount, IMemoryTreePool instance) {
        w.lock();
        try {
            Bytes key = Bytes.wrap(address.getBytes(StandardCharsets.UTF_8));
            Option<PatriciaTreeNode> prev = instance.getByaddress(address);
            if (prev.isEmpty()) {
                PatriciaTreeNode next = new PatriciaTreeNode(amount, 0, 0);
                patriciaTreeImp.put(key, next);
            } else {
                PatriciaTreeNode patriciaTreeNode = prev.get();
                Double new_cash = prev.get().getAmount() - amount;
                //System.out.println("Widraw "+address+ " "+prev.get().getAmount()+" - "+patriciaTreeNode.getAmount()+" = "+amount);
                patriciaTreeNode.setAmount(new_cash);
                patriciaTreeNode.setNonce(patriciaTreeNode.getNonce() + 1);
                patriciaTreeImp.put(key, patriciaTreeNode);
            }
        } finally {
            w.unlock();
        }
    }


    @Override
    public Option<PatriciaTreeNode> getByaddress(String address) {
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
