package io.Adrestus.core.Trie;

import java.util.List;

public interface MerkleTree {

    public MerkleProofs getMerkleeproofs();
    public void build_proofs(List<MekleNode> list, MekleNode target);
    public String getRootHash();
    public void my_generate(List<MekleNode> list);
    public boolean isMekleeNodeExisted(List<MekleNode> list, String roothash, MekleNode node);
}
