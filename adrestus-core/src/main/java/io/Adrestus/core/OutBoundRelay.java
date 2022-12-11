package io.Adrestus.core;

import com.google.common.base.Objects;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

import java.util.ArrayList;
import java.util.List;

public class OutBoundRelay {

    private List<Receipt> outbound;
    private String outboundMerkleRoot;


    public OutBoundRelay(@Deserialize("outbound") List<Receipt> outbound, @Deserialize("outboundMerkleRoot") String outboundMerkleRoot) {
        this.outbound = outbound;
        this.outboundMerkleRoot = outboundMerkleRoot;
    }

    public OutBoundRelay() {
        this.outbound = new ArrayList<>();
        this.outboundMerkleRoot = "";
    }

    @Serialize
    public List<Receipt> getOutbound() {
        return outbound;
    }

    public void setOutbound(List<Receipt> outbound) {
        this.outbound = outbound;
    }

    @Serialize
    public String getOutboundMerkleRoot() {
        return outboundMerkleRoot;
    }

    public void setOutboundMerkleRoot(String outboundMerkleRoot) {
        this.outboundMerkleRoot = outboundMerkleRoot;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OutBoundRelay that = (OutBoundRelay) o;
        return Objects.equal(outbound, that.outbound) && Objects.equal(outboundMerkleRoot, that.outboundMerkleRoot);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(outbound, outboundMerkleRoot);
    }
}
