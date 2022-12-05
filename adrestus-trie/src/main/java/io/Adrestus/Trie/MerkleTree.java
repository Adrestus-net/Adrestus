package io.Adrestus.Trie;

import java.util.List;

public interface MerkleTree {

    MerkleProofs getMerkleeproofs();

    void build_proofs(List<MerkleNode> list, MerkleNode target);

    String getRootHash();

    void my_generate(List<MerkleNode> list);

    void my_generate2(List<MerkleNode> dataBlocks);

    boolean isMekleeNodeExisted(List<MerkleNode> list, String roothash, MerkleNode node);
}
