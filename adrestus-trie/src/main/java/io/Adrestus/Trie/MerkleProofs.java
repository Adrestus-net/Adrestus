package io.Adrestus.Trie;

import java.util.HashMap;
import java.util.Objects;

public class MerkleProofs implements Cloneable{

    private HashMap<Integer, MerkleNode> proofs;

    public MerkleProofs() {
        this.proofs = new HashMap<>();
    }

    public HashMap<Integer, MerkleNode> getProofs() {
        return proofs;
    }

    public void setProofs(HashMap<Integer, MerkleNode> proofs) {
        this.proofs = proofs;
    }

    public void clear() {
        this.proofs.clear();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MerkleProofs that = (MerkleProofs) o;
        return Objects.equals(proofs, that.proofs);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(proofs);
    }
}
