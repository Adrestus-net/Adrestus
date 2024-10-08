package io.Adrestus;

import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.Trie.optimize64_trie.MerklePatriciaTrie;
import io.Adrestus.config.RewardConfiguration;
import io.Adrestus.util.bytes.Bytes;
import io.vavr.control.Option;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

public class RegularTransactionStorage implements TransactionStorage {
    @Override
    public void deposit(MerklePatriciaTrie<Bytes, PatriciaTreeNode> patriciaTreeImp, String address, BigDecimal amount, BigDecimal fees) throws CloneNotSupportedException {
        Bytes key = Bytes.wrap(address.getBytes(StandardCharsets.UTF_8));
        Option<PatriciaTreeNode> prev = patriciaTreeImp.get(key);
        if (prev.isEmpty()) {
            PatriciaTreeNode next = new PatriciaTreeNode(amount, 0, BigDecimal.ZERO);
            patriciaTreeImp.put(key, next);
        } else {
            PatriciaTreeNode patriciaTreeNode = (PatriciaTreeNode) prev.get().clone();
            BigDecimal new_cash = patriciaTreeNode.getAmount().add(amount.subtract(fees));
            patriciaTreeNode.setAmount(new_cash);
            patriciaTreeImp.put(key, patriciaTreeNode);
        }
    }

    @Override
    public void withdraw(MerklePatriciaTrie<Bytes, PatriciaTreeNode> patriciaTreeImp, String address, BigDecimal amount, BigDecimal fees) throws CloneNotSupportedException {
        Bytes key = Bytes.wrap(address.getBytes(StandardCharsets.UTF_8));
        Option<PatriciaTreeNode> prev = patriciaTreeImp.get(key);
        if (prev.isEmpty()) {
            PatriciaTreeNode next = new PatriciaTreeNode(amount, 0, BigDecimal.ZERO);
            patriciaTreeImp.put(key, next);
        } else {
            PatriciaTreeNode patriciaTreeNode = (PatriciaTreeNode) prev.get().clone();
            BigDecimal new_cash = prev.get().getAmount().subtract(amount.add(fees));
            patriciaTreeNode.setAmount(new_cash);
            patriciaTreeNode.setNonce(patriciaTreeNode.getNonce() + 1);
            patriciaTreeImp.put(key, patriciaTreeNode);
        }
    }
}
