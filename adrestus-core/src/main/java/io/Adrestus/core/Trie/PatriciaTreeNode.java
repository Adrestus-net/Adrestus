package io.Adrestus.core.Trie;

import com.google.common.base.Objects;

public class PatriciaTreeNode {

    private double amount;
    private int nonce;
    private String timestamp;

    public PatriciaTreeNode(double amount, int nonce, String timestamp) {
        this.amount = amount;
        this.nonce = nonce;
        this.timestamp = timestamp;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatriciaTreeNode that = (PatriciaTreeNode) o;
        return Double.compare(that.amount, amount) == 0 && nonce == that.nonce && Objects.equal(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(amount, nonce, timestamp);
    }

    @Override
    public String toString() {
        return "PatriciaTreeNode{" +
                "amount=" + amount +
                ", nonce=" + nonce +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
