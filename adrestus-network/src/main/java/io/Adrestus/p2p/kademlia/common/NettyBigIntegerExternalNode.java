package io.Adrestus.p2p.kademlia.common;

import com.google.common.base.Objects;
import io.Adrestus.p2p.kademlia.node.Node;
import lombok.*;

import java.math.BigInteger;
import java.util.Date;


@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NettyBigIntegerExternalNode implements Node<BigInteger, NettyConnectionInfo> {
    @Getter
    @Setter
    private NettyConnectionInfo connectionInfo;
    @Getter
    @Setter
    private BigInteger id;
    @Getter
    @Setter
    private Date lastSeen;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NettyBigIntegerExternalNode that = (NettyBigIntegerExternalNode) o;
        return Objects.equal(getConnectionInfo(), that.getConnectionInfo()) && Objects.equal(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getConnectionInfo(), getId());
    }
}
