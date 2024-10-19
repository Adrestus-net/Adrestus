package io.Adrestus.Trie;

import io.Adrestus.Trie.optimize64_trie.ProfKey;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeNullable;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.TreeMap;

public class MerkleProofs implements Serializable, Cloneable {

    private TreeMap<ProfKey, MerkleNode> proofs;

    public MerkleProofs() {
        this.proofs = new TreeMap<>(new CustomComparator());
    }

    @SerializeNullable
    @Serialize
    public TreeMap<ProfKey, MerkleNode> getProofs() {
        return proofs;
    }

    public void setProofs(TreeMap<ProfKey, MerkleNode> proofs) {
        this.proofs = proofs;
    }

    public void clear() {
        this.proofs.values().forEach(MerkleProofs::clearMerkleNode);
        this.proofs.clear();
    }

    public static void clearMerkleNode(MerkleNode node) {
        if (node == null) {
            return;
        }
        clearMerkleNode(node.getLeft());
        clearMerkleNode(node.getRight());
        node.setLeft(null);
        node.setRight(null);
        node.setTransactionHash(null);
    }

    @Override
    public MerkleProofs clone() {
        try {
            MerkleProofs cloned = (MerkleProofs) super.clone();
            cloned.proofs = new TreeMap<>(this.proofs);
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // Should never happen
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MerkleProofs that = (MerkleProofs) o;
        return Objects.equals(proofs, that.proofs);
    }

    public int getLength() {
        if(proofs == null) {
            return 0;
        }
        if(proofs.isEmpty()) {
            return 0;
        }
        return proofs.values().stream()
                .mapToInt(MerkleNode::getLength)
                .sum();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(proofs);
    }
    private static class CustomComparator implements Comparator<ProfKey>, Serializable {
        @Override
        public int compare(ProfKey o1, ProfKey o2) {
            return Integer.compare(o2.getPosition(), o1.getPosition());
        }
    }
}
