package io.Adrestus.Trie;

import com.google.common.base.Objects;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

import java.io.Serializable;

public class PatriciaTreeNode implements Serializable,Cloneable {

    private double amount;
    private double staking_amount;
    private int nonce;


    public PatriciaTreeNode(double amount, int nonce) {
        this.amount = amount;
        this.nonce = nonce;
    }

    public PatriciaTreeNode(@Deserialize("amount") double amount, @Deserialize("nonce") int nonce, @Deserialize("staking_amount") double staking_amount) {
        this.amount = amount;
        this.nonce = nonce;
        this.staking_amount = staking_amount;
    }

    public PatriciaTreeNode(double amount) {
        this.amount = amount;
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
    public Object clone()throws CloneNotSupportedException{
        return super.clone();
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
