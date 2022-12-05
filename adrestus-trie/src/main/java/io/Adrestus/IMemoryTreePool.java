package io.Adrestus;

import io.Adrestus.Trie.PatriciaTreeNode;

import java.util.Optional;

public interface IMemoryTreePool {
    void store(String address, PatriciaTreeNode patriciaTreeNode) throws Exception;

    void update(String address, PatriciaTreeNode patriciaTreeNode) throws Exception;

    Optional<PatriciaTreeNode> getByaddress(String address);

    String getRootHash() throws Exception;
}
