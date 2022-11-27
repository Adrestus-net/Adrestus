package io.Adrestus.core.Trie;

import com.google.common.base.Objects;
import io.activej.serializer.annotations.Serialize;

public class PatriciaTreeNode {

    private double amount;
    private double staking_amount;
    private int nonce;

    public PatriciaTreeNode(double amount, int nonce) {
        this.amount = amount;
        this.nonce = nonce;
    }

    public PatriciaTreeNode(double amount, int nonce,double staking_amount) {
        this.amount = amount;
        this.nonce = nonce;
        this.staking_amount = staking_amount;
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

    @Serialize
    public double getStaking_amount() {
        return staking_amount;
    }

    public void setStaking_amount(double staking_amount) {
        this.staking_amount = staking_amount;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatriciaTreeNode that = (PatriciaTreeNode) o;
        return Double.compare(that.amount, amount) == 0 && Double.compare(that.staking_amount, staking_amount) == 0 && nonce == that.nonce;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(amount, staking_amount, nonce);
    }


    @Override
    public String toString() {
        return "PatriciaTreeNode{" +
                "amount=" + amount +
                ", staking_amount=" + staking_amount +
                ", nonce=" + nonce +
                '}';
    }
}
