package io.Adrestus.core.Trie;

import java.util.List;

public interface MerkleTree {

    MerkleProofs getMerkleeproofs();

    void build_proofs(List<MerkleNode> list, MerkleNode target);

    String getRootHash();

    void my_generate(List<MerkleNode> list);

    boolean isMekleeNodeExisted(List<MerkleNode> list, String roothash, MerkleNode node);
}
