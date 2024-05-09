package io.Adrestus;

import com.google.common.base.Objects;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.Trie.optimize64_trie.MerklePatriciaTrie;
import io.Adrestus.util.bytes.Bytes;
import io.Adrestus.util.bytes.Bytes53;
import io.activej.serializer.annotations.Serialize;
import io.vavr.control.Option;
import lombok.SneakyThrows;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.SerializationUtils;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class MemoryTreePool implements IMemoryTreePool, Cloneable {


    private MerklePatriciaTrie<Bytes, PatriciaTreeNode> patriciaTreeImp;
    private SerializableFunction<PatriciaTreeNode, Bytes> valueSerializer;
    private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private Lock r = rwl.readLock();
    private Lock w = rwl.writeLock();

    private String height;

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
    @SneakyThrows
    @Override
    public void deposit(String address, double amount) {
        w.lock();
        try {
            Bytes key = Bytes.wrap(address.getBytes(StandardCharsets.UTF_8));
            Option<PatriciaTreeNode> prev = getByaddress(address);
            if (prev.isEmpty()) {
                PatriciaTreeNode next = new PatriciaTreeNode(amount, 0, 0);
                patriciaTreeImp.put(key, next);
            } else {
                PatriciaTreeNode patriciaTreeNode = (PatriciaTreeNode) prev.get().clone();
                Double new_cash = patriciaTreeNode.getAmount() + amount;
                patriciaTreeNode.setAmount(new_cash);
                patriciaTreeImp.put(key, patriciaTreeNode);
            }
        } finally {
            w.unlock();
        }
    }
    @SneakyThrows
    @Override
    public void depositUnclaimedReward(String address, double amount) {
        w.lock();
        try {
            Bytes key = Bytes.wrap(address.getBytes(StandardCharsets.UTF_8));
            Option<PatriciaTreeNode> prev = getByaddress(address);
            if (prev.isEmpty()) {
                PatriciaTreeNode next = new PatriciaTreeNode(amount, 0, 0);
                patriciaTreeImp.put(key, next);
            } else {
                PatriciaTreeNode patriciaTreeNode = (PatriciaTreeNode) prev.get().clone();
                Double new_cash = patriciaTreeNode.getUnclaimed_reward() + amount;
                patriciaTreeNode.setUnclaimed_reward(new_cash);
                patriciaTreeImp.put(key, patriciaTreeNode);
            }
        } finally {
            w.unlock();
        }
    }


    //be aware that print functionality is  different
    @SneakyThrows
    @Override
    public void withdraw(String address, double amount) {
        w.lock();
        try {
            Bytes key = Bytes.wrap(address.getBytes(StandardCharsets.UTF_8));
            Option<PatriciaTreeNode> prev = getByaddress(address);
            if (prev.isEmpty()) {
                PatriciaTreeNode next = new PatriciaTreeNode(amount, 0, 0);
                patriciaTreeImp.put(key, next);
            } else {
                PatriciaTreeNode patriciaTreeNode = (PatriciaTreeNode) prev.get().clone();
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
    public void withdrawUnclaimedReward(String address, double amount) {
        w.lock();
        try {
            Bytes key = Bytes.wrap(address.getBytes(StandardCharsets.UTF_8));
            Option<PatriciaTreeNode> prev = getByaddress(address);
            if (prev.isEmpty()) {
                PatriciaTreeNode next = new PatriciaTreeNode(amount, 0, 0);
                patriciaTreeImp.put(key, next);
            } else {
                PatriciaTreeNode patriciaTreeNode = (PatriciaTreeNode) prev.get().clone();
                Double new_cash = prev.get().getUnclaimed_reward() - amount;
                patriciaTreeNode.setUnclaimed_reward(new_cash);
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
        r.lock();
        try {
            return patriciaTreeImp.getRootHash().toHexString();
        } finally {
            r.unlock();
        }
    }

    @Override
    public MerklePatriciaTrie<Bytes, PatriciaTreeNode> getTrie() {
        return this.patriciaTreeImp;
    }

    @Override
    public void setTrie(MerklePatriciaTrie<Bytes, PatriciaTreeNode> patriciaTreeImp) {
        w.lock();
        try {
            this.patriciaTreeImp = patriciaTreeImp;
        } finally {
            w.unlock();
        }
    }


    public void setPatriciaTreeImp(MerklePatriciaTrie<Bytes, PatriciaTreeNode> patriciaTreeImp) {
        w.lock();
        try {
            this.patriciaTreeImp = patriciaTreeImp;
        } finally {
            w.unlock();
        }
    }

    @Override
    @Serialize
    public MerklePatriciaTrie<Bytes, PatriciaTreeNode> getPatriciaTreeImp() {
        return patriciaTreeImp;
    }

    @Override
    public Set<String> Keyset(final Bytes53 startKeyHash, final int limit) {
        w.lock();
        try {
            return patriciaTreeImp.entriesFrom(startKeyHash, limit)
                    .keySet()
                    .stream()
                    .map(val -> {
                        try {
                            return new String(Hex.decodeHex(val.toString().substring(2)), StandardCharsets.UTF_8);
                        } catch (DecoderException e) {
                            e.printStackTrace();
                            return "";
                        }
                    }).collect(Collectors.toSet());
        } finally {
            w.unlock();
        }
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

    @SneakyThrows
    @Override
    public Object clone() {
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
