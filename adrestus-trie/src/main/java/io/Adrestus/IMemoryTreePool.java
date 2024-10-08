package io.Adrestus;

import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.Trie.PatriciaTreeTransactionType;
import io.Adrestus.Trie.StakingInfo;
import io.Adrestus.Trie.optimize64_trie.MerklePatriciaTrie;
import io.Adrestus.util.bytes.Bytes;
import io.Adrestus.util.bytes.Bytes53;
import io.vavr.control.Option;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;

public interface IMemoryTreePool extends Serializable {
    void store(String address, PatriciaTreeNode patriciaTreeNode);

    void deposit(PatriciaTreeTransactionType type, String address, BigDecimal amount, BigDecimal fees);

    void withdraw(PatriciaTreeTransactionType type, String address, BigDecimal amount, BigDecimal fees);


    void setStakingInfos(String address, StakingInfo stakingInfo);

    Option<PatriciaTreeNode> getByaddress(String address);

    String BytesToHexStringAddress(Bytes bytes);

    String getRootHash() throws Exception;

    Set<String> Keyset(final Bytes53 startKeyHash, final int limit);

    MerklePatriciaTrie<Bytes, PatriciaTreeNode> getTrie();

    void setTrie(MerklePatriciaTrie<Bytes, PatriciaTreeNode> patriciaTrie);

    MerklePatriciaTrie<Bytes, PatriciaTreeNode> getPatriciaTreeImp();

    void setPatriciaTreeImp(MerklePatriciaTrie<Bytes, PatriciaTreeNode> patriciaTreeImp);

    Object clone();
}
