package io.Adrestus.p2p.kademlia.repository;

import com.google.common.base.Objects;
import io.Adrestus.crypto.SecurityAuditProofs;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

import java.io.Serializable;

public class KademliaData implements Serializable, Cloneable {
    private String Hash;
    private SecurityAuditProofs addressData;
    private NettyConnectionInfo nettyConnectionInfo;

    public KademliaData() {
    }

    public KademliaData(@Deserialize("hash") String hash,
                        @Deserialize("nettyConnectionInfo") NettyConnectionInfo nettyConnectionInfo) {
        this.Hash = hash;
        this.nettyConnectionInfo = nettyConnectionInfo;
    }


    public KademliaData(@Deserialize("hash") String hash,
                        @Deserialize("addressData") SecurityAuditProofs addressData,
                        @Deserialize("nettyConnectionInfo") NettyConnectionInfo nettyConnectionInfo) {
        this.Hash = hash;
        this.addressData = addressData;
        this.nettyConnectionInfo = nettyConnectionInfo;
    }

    public KademliaData(@Deserialize("addressData") SecurityAuditProofs addressData,
                        @Deserialize("nettyConnectionInfo") NettyConnectionInfo nettyConnectionInfo) {
        this.Hash = "";
        this.addressData = addressData;
        this.nettyConnectionInfo = nettyConnectionInfo;
    }

    public KademliaData(@Deserialize("addressData") SecurityAuditProofs addressData) {
        this.Hash = "";
        this.addressData = addressData;
        this.nettyConnectionInfo = new NettyConnectionInfo("", 0);
    }

    @Serialize
    public String getHash() {
        return Hash;
    }

    public void setHash(String hash) {
        this.Hash = hash;
    }

    @Serialize
    public SecurityAuditProofs getAddressData() {
        return addressData;
    }

    public void setAddressData(SecurityAuditProofs addressData) {
        this.addressData = addressData;
    }

    @Serialize
    public NettyConnectionInfo getNettyConnectionInfo() {
        return nettyConnectionInfo;
    }

    public void setNettyConnectionInfo(NettyConnectionInfo nettyConnectionInfo) {
        this.nettyConnectionInfo = nettyConnectionInfo;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KademliaData that = (KademliaData) o;
        return Hash.equals(that.Hash) && Objects.equal(addressData, that.addressData) && Objects.equal(nettyConnectionInfo, that.nettyConnectionInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(Hash, addressData);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "KademliaData{" +
                "Hash='" + Hash + '\'' +
                ", addressData=" + addressData +
                ", nettyConnectionInfo=" + nettyConnectionInfo +
                '}';
    }
}
