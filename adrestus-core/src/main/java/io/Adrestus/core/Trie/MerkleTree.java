package io.Adrestus.core.Trie;

import java.util.List;

public interface MerkleTree {

    public MerkleProofs getMerkleeproofs();

    public void build_proofs(List<MerkleNode> list, MerkleNode target);

    public String getRootHash();

    public void my_generate(List<MerkleNode> list);

    public boolean isMekleeNodeExisted(List<MerkleNode> list, String roothash, MerkleNode node);
}
