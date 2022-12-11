package io.Adrestus.core;

import com.google.common.base.Objects;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

import java.util.ArrayList;
import java.util.List;

public class InboundRelay {
    private List<Receipt> receipt;
    private String inboundMerkleRoot;

    public InboundRelay(@Deserialize("receipt") List<Receipt> receipt, @Deserialize("inboundMerkleRoot") String inboundMerkleRoot) {
        this.receipt = receipt;
        this.inboundMerkleRoot = inboundMerkleRoot;
    }

    public InboundRelay() {
        this.receipt = new ArrayList<>();
        this.inboundMerkleRoot = "";
    }

    @Serialize
    public List<Receipt> getReceipt() {
        return receipt;
    }

    public void setReceipt(List<Receipt> receipt) {
        this.receipt = receipt;
    }

    @Serialize
    public String getInboundMerkleRoot() {
        return inboundMerkleRoot;
    }

    public void setInboundMerkleRoot(String inboundMerkleRoot) {
        this.inboundMerkleRoot = inboundMerkleRoot;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InboundRelay that = (InboundRelay) o;
        return Objects.equal(receipt, that.receipt) && Objects.equal(inboundMerkleRoot, that.inboundMerkleRoot);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(receipt, inboundMerkleRoot);
    }

    @Override
    public String toString() {
        return "InboundRelay{" +
                "receipt=" + receipt +
                ", inboundMerkleRoot='" + inboundMerkleRoot + '\'' +
                '}';
    }
}
