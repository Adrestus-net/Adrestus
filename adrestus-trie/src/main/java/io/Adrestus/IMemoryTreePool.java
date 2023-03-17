package io.Adrestus;

import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.Trie.optimize64_trie.MerklePatriciaTrie;
import io.Adrestus.util.bytes.Bytes;
import io.vavr.control.Option;

import java.io.Serializable;

public interface IMemoryTreePool extends Serializable {
    void store(String address, PatriciaTreeNode patriciaTreeNode) throws Exception;

    void deposit(String address, double amount, IMemoryTreePool instance);

    void withdraw(String address, double amount, IMemoryTreePool instance);

    void depositReplica(String address, double amount, IMemoryTreePool instance);

    void withdrawReplica(String address, double amount, IMemoryTreePool instance);

    Option<PatriciaTreeNode> getByaddress(String address);

    String getRootHash() throws Exception;

    MerklePatriciaTrie<Bytes, PatriciaTreeNode> getTrie();

    void setTrie(MerklePatriciaTrie<Bytes, PatriciaTreeNode> patriciaTrie);

    MerklePatriciaTrie<Bytes, PatriciaTreeNode> getPatriciaTreeImp();

    void setPatriciaTreeImp(MerklePatriciaTrie<Bytes, PatriciaTreeNode> patriciaTreeImp);
}
