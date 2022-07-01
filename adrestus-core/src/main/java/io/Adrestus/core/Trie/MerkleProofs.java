package io.Adrestus.core.Trie;

import java.util.List;

public class MerkleProofs {
    private String roothash;
    private List<MerkleNode> list_builder;
    private MerkleNode target;

    public MerkleProofs(String roothash, List<MerkleNode> list_builder, MerkleNode target) {
        this.roothash = roothash;
        this.list_builder = list_builder;
        this.target = target;
    }

    public MerkleProofs(List<MerkleNode> list_builder) {
        this.list_builder = list_builder;
    }

    public MerkleProofs() {
    }

    public String getRoothash() {
        return roothash;
    }

    public void setRoothash(String roothash) {
        this.roothash = roothash;
    }

    public List<MerkleNode> getList_builder() {
        return list_builder;
    }

    public void setList_builder(List<MerkleNode> list_builder) {
        this.list_builder = list_builder;
    }

    public MerkleNode getTarget() {
        return target;
    }

    public void setTarget(MerkleNode target) {
        this.target = target;
    }
    
}
