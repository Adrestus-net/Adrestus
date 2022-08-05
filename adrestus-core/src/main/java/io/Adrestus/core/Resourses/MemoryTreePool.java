package io.Adrestus.core.Resourses;

import io.Adrestus.core.Trie.PatriciaTreeNode;

import java.util.Optional;
import java.util.stream.Stream;

public interface MemoryTreePool {
    boolean store(String address, PatriciaTreeNode patriciaTreeNode);

    boolean update(String address, PatriciaTreeNode patriciaTreeNode);

    Stream<PatriciaTreeNode> getAll() throws Exception;

    Optional<PatriciaTreeNode> getById(String address) throws Exception;
}
