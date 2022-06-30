package io.Adrestus.core.Trie;

public class MekleNode {
    private String TransactionHash;
    private MekleNode root;
    private MekleNode left;
    private MekleNode right;

    public MekleNode(String TransactionHash) {
        this.TransactionHash = TransactionHash;
        this.left=null;
        this.right=null;
    }

    public MekleNode() {
    }

    public MekleNode(MekleNode left, MekleNode right) {
        this.left = left;
        this.right = right;
    }

    public String getTransactionHash() {
        return TransactionHash;
    }

    public void setTransactionHash(String TransactionHash) {
        this.TransactionHash = TransactionHash;
    }

    public MekleNode getLeft() {
        return left;
    }

    public void setLeft(MekleNode left) {
        this.left = left;
    }

    public MekleNode getRight() {
        return right;
    }

    public void setRight(MekleNode right) {
        this.right = right;
    }

    public MekleNode getRoot() {
        return root;
    }

    public void setRoot(MekleNode root) {
        this.root = root;
    }

  
}
