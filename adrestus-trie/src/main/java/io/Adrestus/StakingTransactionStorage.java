package io.Adrestus;

import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.Trie.optimize64_trie.MerklePatriciaTrie;
import io.Adrestus.util.bytes.Bytes;
import io.vavr.control.Option;

import java.nio.charset.StandardCharsets;

public class StakingTransactionStorage implements TransactionStorage {
    @Override
    public void deposit(MerklePatriciaTrie<Bytes, PatriciaTreeNode> patriciaTreeImp, String address, double amount, double fees) throws CloneNotSupportedException {
        Bytes key = Bytes.wrap(address.getBytes(StandardCharsets.UTF_8));
        Option<PatriciaTreeNode> prev = patriciaTreeImp.get(key);
        if (prev.isEmpty()) {
            PatriciaTreeNode next = new PatriciaTreeNode(amount, 0, 0, 0);
            patriciaTreeImp.put(key, next);
        } else {
            PatriciaTreeNode patriciaTreeNode = (PatriciaTreeNode) prev.get().clone();
            double new_cash = patriciaTreeNode.getStaking_amount() + (amount - fees);
            patriciaTreeNode.setStaking_amount(new_cash);
            patriciaTreeImp.put(key, patriciaTreeNode);
        }
    }

    @Override
    public void withdraw(MerklePatriciaTrie<Bytes, PatriciaTreeNode> patriciaTreeImp, String address, double amount, double fees) throws CloneNotSupportedException {
        Bytes key = Bytes.wrap(address.getBytes(StandardCharsets.UTF_8));
        Option<PatriciaTreeNode> prev = patriciaTreeImp.get(key);
        if (prev.isEmpty()) {
            PatriciaTreeNode next = new PatriciaTreeNode(amount, 0, 0, 0);
            patriciaTreeImp.put(key, next);
        } else {
            PatriciaTreeNode patriciaTreeNode = (PatriciaTreeNode) prev.get().clone();
            double new_cash = prev.get().getAmount() - (amount + fees);
            patriciaTreeNode.setAmount(new_cash);
            patriciaTreeNode.setNonce(patriciaTreeNode.getNonce() + 1);
            patriciaTreeImp.put(key, patriciaTreeNode);
        }
    }
}
