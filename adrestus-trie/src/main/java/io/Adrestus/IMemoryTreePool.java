package io.Adrestus;

import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.Trie.optimize64_trie.IMerklePatriciaTrie;
import org.apache.tuweni.bytes.Bytes;

import java.util.Optional;

public interface IMemoryTreePool {
    void store(String address, PatriciaTreeNode patriciaTreeNode) throws Exception;

    void deposit(String address, PatriciaTreeNode patriciaTreeNode);

    void withdraw(String address, PatriciaTreeNode patriciaTreeNode);

    Optional<PatriciaTreeNode> getByaddress(String address);

    String getRootHash() throws Exception;

    IMerklePatriciaTrie<Bytes, PatriciaTreeNode> getTrie();
}
