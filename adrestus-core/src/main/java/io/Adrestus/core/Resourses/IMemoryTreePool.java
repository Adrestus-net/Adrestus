package io.Adrestus.core.Resourses;

import io.Adrestus.core.Trie.PatriciaTreeNode;

import java.util.Optional;
import java.util.stream.Stream;

public interface IMemoryTreePool {
    void store(String address, PatriciaTreeNode patriciaTreeNode)throws Exception;

    void update(String address, PatriciaTreeNode patriciaTreeNode)throws Exception;

    Optional<PatriciaTreeNode> getByaddress(String address) throws Exception;

    String getRootHash() throws Exception;
}
