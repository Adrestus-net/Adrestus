package io.Adrestus.core;

import com.google.common.base.Objects;

import java.util.ArrayList;
import java.util.List;

public class InboundRelay {
    private List<Receipt> Receipt;
    private String InboundMerkleRoot;

    public InboundRelay(List<Receipt> receipt, String inboundMerkleRoot) {
        this.Receipt = receipt;
        this.InboundMerkleRoot = inboundMerkleRoot;
    }

    public InboundRelay() {
        this.Receipt=new ArrayList<>();
        this.InboundMerkleRoot="";
    }

    public List<Receipt> getReceipt() {
        return Receipt;
    }

    public void setReceipt(List<Receipt> receipt) {
        Receipt = receipt;
    }

    public String getInboundMerkleRoot() {
        return InboundMerkleRoot;
    }

    public void setInboundMerkleRoot(String inboundMerkleRoot) {
        InboundMerkleRoot = inboundMerkleRoot;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InboundRelay that = (InboundRelay) o;
        return Objects.equal(Receipt, that.Receipt) && Objects.equal(InboundMerkleRoot, that.InboundMerkleRoot);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(Receipt, InboundMerkleRoot);
    }

    @Override
    public String toString() {
        return "InboundRelay{" +
                "Receipt=" + Receipt +
                ", InboundMerkleRoot='" + InboundMerkleRoot + '\'' +
                '}';
    }
}
