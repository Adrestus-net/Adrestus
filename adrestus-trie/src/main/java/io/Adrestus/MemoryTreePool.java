package io.Adrestus;

import com.google.common.base.Objects;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.Trie.optimize64_trie.MerklePatriciaTrie;
import io.Adrestus.util.bytes.Bytes;
import io.activej.serializer.annotations.Serialize;
import io.vavr.control.Option;
import lombok.SneakyThrows;
import org.apache.commons.lang3.SerializationUtils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MemoryTreePool implements IMemoryTreePool, Cloneable {


    private MerklePatriciaTrie<Bytes, PatriciaTreeNode> patriciaTreeImp;
    private SerializableFunction<PatriciaTreeNode, Bytes> valueSerializer;
    private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private Lock r = rwl.readLock();
    private Lock w = rwl.writeLock();

    public MemoryTreePool() {
        this.valueSerializer = value -> (value != null) ? Bytes.wrap(SerializationUtils.serialize(value)) : null;
        //this.valueSerializer=null;
        this.patriciaTreeImp = new MerklePatriciaTrie<Bytes, PatriciaTreeNode>(valueSerializer);
    }

    public MemoryTreePool(MemoryTreePool memoryTreePool) throws CloneNotSupportedException {
        this.patriciaTreeImp = (MerklePatriciaTrie<Bytes, PatriciaTreeNode>) memoryTreePool.patriciaTreeImp.clone();
        this.valueSerializer = memoryTreePool.valueSerializer;
        this.rwl = memoryTreePool.rwl;
        this.r = memoryTreePool.r;
        this.w = memoryTreePool.w;
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

    @SneakyThrows
    @Override
    public void depositReplica(String address, double amount, IMemoryTreePool instance) {
        Bytes key = Bytes.wrap(address.getBytes(StandardCharsets.UTF_8));
        Option<PatriciaTreeNode> prev = instance.getByaddress(address);
        if (prev.isEmpty()) {
            PatriciaTreeNode next = new PatriciaTreeNode(amount, 0, 0);
            instance.getTrie().put(key, next);
        } else {
            PatriciaTreeNode patriciaTreeNode = (PatriciaTreeNode) prev.get().clone();
            Double new_cash = prev.get().getAmount() + amount;
            // System.out.println("Deposit "+address+ " "+prev.get().getAmount()+" + "+patriciaTreeNode.getAmount()+" = "+amount);
            patriciaTreeNode.setAmount(new_cash);
            patriciaTreeNode.setNonce(prev.get().getNonce());
            instance.getTrie().put(key, patriciaTreeNode);
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


    @SneakyThrows
    @Override
    public void withdrawReplica(String address, double amount, IMemoryTreePool instance) {
        Bytes key = Bytes.wrap(address.getBytes(StandardCharsets.UTF_8));
        Option<PatriciaTreeNode> prev = instance.getByaddress(address);
        if (prev.isEmpty()) {
            PatriciaTreeNode next = new PatriciaTreeNode(amount, 0, 0);
            instance.getTrie().put(key, next);
        } else {
            PatriciaTreeNode patriciaTreeNode = (PatriciaTreeNode) prev.get().clone();
            Double new_cash = prev.get().getAmount() - amount;
            //System.out.println("Widraw "+address+ " "+prev.get().getAmount()+" - "+patriciaTreeNode.getAmount()+" = "+amount);
            patriciaTreeNode.setAmount(new_cash);
            patriciaTreeNode.setNonce(patriciaTreeNode.getNonce() + 1);
            instance.getTrie().put(key, patriciaTreeNode);
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
    public MerklePatriciaTrie<Bytes, PatriciaTreeNode> getTrie() {
        return this.patriciaTreeImp;
    }


    public void setPatriciaTreeImp(MerklePatriciaTrie<Bytes, PatriciaTreeNode> patriciaTreeImp) {
        this.patriciaTreeImp = patriciaTreeImp;
    }

    @Override
    @Serialize
    public MerklePatriciaTrie<Bytes, PatriciaTreeNode> getPatriciaTreeImp() {
        return patriciaTreeImp;
    }


    public SerializableFunction<PatriciaTreeNode, Bytes> getValueSerializer() {
        return valueSerializer;
    }

    public void setValueSerializer(SerializableFunction<PatriciaTreeNode, Bytes> valueSerializer) {
        this.valueSerializer = valueSerializer;
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

    public void setRwl(ReentrantReadWriteLock rwl) {
        this.rwl = rwl;
    }

    public void setR(Lock r) {
        this.r = r;
    }

    public void setW(Lock w) {
        this.w = w;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemoryTreePool that = (MemoryTreePool) o;
        return Objects.equal(patriciaTreeImp.getRootHash(), that.patriciaTreeImp.getRootHash());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(patriciaTreeImp, valueSerializer, rwl, r, w);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        MemoryTreePool t = new MemoryTreePool();
        t.setPatriciaTreeImp((MerklePatriciaTrie<Bytes, PatriciaTreeNode>) this.patriciaTreeImp.clone());
        t.setValueSerializer(this.valueSerializer);
        t.setRwl(this.rwl);
        t.setR(this.r);
        t.setW(this.w);
        return t;
    }


    @Override
    public String toString() {
        return "MemoryTreePool{" +
                "patriciaTreeImp=" + patriciaTreeImp +
                ", valueSerializer=" + valueSerializer +
                ", rwl=" + rwl +
                ", r=" + r +
                ", w=" + w +
                '}';
    }
}
