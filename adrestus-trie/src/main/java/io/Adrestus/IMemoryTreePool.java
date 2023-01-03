package io.Adrestus;

import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.Trie.optimize64_trie.IMerklePatriciaTrie;
import org.apache.tuweni.bytes.Bytes;

import java.io.Serializable;
import java.util.Optional;

public interface IMemoryTreePool extends Serializable {
    void store(String address, PatriciaTreeNode patriciaTreeNode) throws Exception;

    void deposit(String address, double amount,IMemoryTreePool instance);

    void withdraw(String address, double amount,IMemoryTreePool instance);

    Optional<PatriciaTreeNode> getByaddress(String address);

    String getRootHash() throws Exception;

    IMerklePatriciaTrie<Bytes, PatriciaTreeNode> getTrie();
}
