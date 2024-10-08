package io.Adrestus;

import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.Trie.optimize64_trie.MerklePatriciaTrie;
import io.Adrestus.util.bytes.Bytes;

import java.io.Serializable;
import java.math.BigDecimal;

public interface TransactionStorage extends Serializable {
    void deposit(MerklePatriciaTrie<Bytes, PatriciaTreeNode> patriciaTreeImp, String address, BigDecimal amount, BigDecimal fees) throws CloneNotSupportedException;

    void withdraw(MerklePatriciaTrie<Bytes, PatriciaTreeNode> patriciaTreeImp, String address, BigDecimal amount,BigDecimal fees) throws CloneNotSupportedException;
}
