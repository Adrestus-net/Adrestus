package io.Adrestus.Trie;

import java.util.List;

public interface MerkleTree {
    MerkleProofs getMerkleeproofs();

    String generateRoot(MerkleProofs proofs);

    void build_proofs(MerkleNode current);

    String getRootHash();

    void constructTree(List<MerkleNode> list);

    boolean isMekleeNodeExisted(List<MerkleNode> list, String roothash, MerkleNode node);

    void clear();
}
