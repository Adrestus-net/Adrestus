package io.Adrestus.core.Trie;

public class MerkleNode {
    private String TransactionHash;
    private MerkleNode root;
    private MerkleNode left;
    private MerkleNode right;

    public MerkleNode(String TransactionHash) {
        this.TransactionHash = TransactionHash;
        this.left=null;
        this.right=null;
    }

    public MerkleNode() {
    }

    public MerkleNode(MerkleNode left, MerkleNode right) {
        this.left = left;
        this.right = right;
    }

    public String getTransactionHash() {
        return TransactionHash;
    }

    public void setTransactionHash(String TransactionHash) {
        this.TransactionHash = TransactionHash;
    }

    public MerkleNode getLeft() {
        return left;
    }

    public void setLeft(MerkleNode left) {
        this.left = left;
    }

    public MerkleNode getRight() {
        return right;
    }

    public void setRight(MerkleNode right) {
        this.right = right;
    }

    public MerkleNode getRoot() {
        return root;
    }

    public void setRoot(MerkleNode root) {
        this.root = root;
    }

  
}
