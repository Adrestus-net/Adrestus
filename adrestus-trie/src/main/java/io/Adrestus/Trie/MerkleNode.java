package io.Adrestus.Trie;

import com.google.common.base.Objects;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeNullable;

import java.io.Serializable;

public class MerkleNode implements Serializable, Cloneable {
    private String transactionHash;
    private MerkleNode root;
    private MerkleNode left;
    private MerkleNode right;

    public MerkleNode(String transactionHash) {
        this.transactionHash = transactionHash;
        this.root = null;
        this.left = null;
        this.right = null;
    }

    public MerkleNode() {
        this.transactionHash = "";
        this.root = new MerkleNode("");
        this.left = new MerkleNode("");
        this.right = new MerkleNode("");
    }

    public MerkleNode(MerkleNode left, MerkleNode right) {
        this.transactionHash = "";
        this.root = new MerkleNode("");
        this.left = left;
        this.right = right;
    }

    @Serialize
    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    @Serialize
    @SerializeNullable
    public MerkleNode getLeft() {
        return left;
    }

    public void setLeft(MerkleNode left) {
        this.left = left;
    }

    @Serialize
    @SerializeNullable
    public MerkleNode getRight() {
        return right;
    }

    public void setRight(MerkleNode right) {
        this.right = right;
    }

    @Serialize
    @SerializeNullable
    public MerkleNode getRoot() {
        return root;
    }

    public void setRoot(MerkleNode root) {
        this.root = root;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MerkleNode that = (MerkleNode) o;
        return Objects.equal(transactionHash, that.transactionHash) && Objects.equal(root, that.root) && Objects.equal(left, that.left) && Objects.equal(right, that.right);
    }

    public int getLength() {
        if (this == null) {
            return 0;
        }
        int length = transactionHash.length();
        if (left != null) {
            length += left.getLength();
        }
        if (right != null) {
            length += right.getLength();
        }
        return length;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(transactionHash, root, left, right);
    }

    @Override
    public MerkleNode clone() {
        try {
            MerkleNode cloned = (MerkleNode) super.clone();
            if (this.left != null) {
                cloned.left = this.left.clone();
            }
            if (this.right != null) {
                cloned.right = this.right.clone();
            }
            if (this.root != null) {
                cloned.root = this.root.clone();
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // Should never happen
        }
    }

    //Never Delete it
    @Override
    public String toString() {
        return "MerkleNode{" +
                "transactionHash='" + transactionHash + '\'' +
                ", rootTransactionHash=" + (root != null ? root.transactionHash : "null") +
                ", leftTransactionHash=" + (left != null ? left.transactionHash : "null") +
                ", rightTransactionHash=" + (right != null ? right.transactionHash : "null") +
                '}';
    }
}
