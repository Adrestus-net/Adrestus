package io.Adrestus.core;

import com.google.common.base.Objects;
import io.activej.serializer.annotations.Serialize;

import java.util.ArrayList;
import java.util.List;

public class OutBoundRelay {

    private List<Receipt> Outbound;
    private String OutboundMerkleRoot;



    public OutBoundRelay(List<Receipt> outbound, String outboundMerkleRoot) {
        Outbound = outbound;
        OutboundMerkleRoot = outboundMerkleRoot;
    }

    public OutBoundRelay() {
        this.Outbound=new ArrayList<>();
        this.OutboundMerkleRoot="";
    }

    @Serialize
    public List<Receipt> getOutbound() {
        return Outbound;
    }

    public void setOutbound(List<Receipt> outbound) {
        Outbound = outbound;
    }

    @Serialize
    public String getOutboundMerkleRoot() {
        return OutboundMerkleRoot;
    }

    public void setOutboundMerkleRoot(String outboundMerkleRoot) {
        OutboundMerkleRoot = outboundMerkleRoot;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OutBoundRelay that = (OutBoundRelay) o;
        return Objects.equal(Outbound, that.Outbound) && Objects.equal(OutboundMerkleRoot, that.OutboundMerkleRoot);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(Outbound, OutboundMerkleRoot);
    }
}
