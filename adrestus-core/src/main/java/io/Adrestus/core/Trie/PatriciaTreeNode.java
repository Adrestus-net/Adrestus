package io.Adrestus.core.Trie;

import com.google.common.base.Objects;
import io.activej.serializer.annotations.Serialize;

public class PatriciaTreeNode {

    private double amount;
    private int nonce;

    public PatriciaTreeNode(double amount, int nonce) {
        this.amount = amount;
        this.nonce = nonce;
    }

    public PatriciaTreeNode() {
    }

    @Serialize
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Serialize
    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatriciaTreeNode that = (PatriciaTreeNode) o;
        return Double.compare(that.amount, amount) == 0 && nonce == that.nonce;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(amount, nonce);
    }

    @Override
    public String toString() {
        return "PatriciaTreeNode{" +
                "amount=" + amount +
                ", nonce=" + nonce +
                '}';
    }
}
