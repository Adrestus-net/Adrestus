package io.Adrestus.core.Trie;

import java.util.List;

public class MerkleProofs {
    private String roothash;
    private List<MekleNode> list_builder;
    private MekleNode target;

    public MerkleProofs(String roothash, List<MekleNode> list_builder, MekleNode target) {
        this.roothash = roothash;
        this.list_builder = list_builder;
        this.target = target;
    }

    public MerkleProofs(List<MekleNode> list_builder) {
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

    public List<MekleNode> getList_builder() {
        return list_builder;
    }

    public void setList_builder(List<MekleNode> list_builder) {
        this.list_builder = list_builder;
    }

    public MekleNode getTarget() {
        return target;
    }

    public void setTarget(MekleNode target) {
        this.target = target;
    }
    
}
