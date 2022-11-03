package io.Adrestus.p2p.kademlia.common;

import io.Adrestus.p2p.kademlia.node.Node;
import lombok.*;

import java.math.BigInteger;


@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NettyExternalNode implements Node<BigInteger, NettyConnectionInfo> {
    private NettyConnectionInfo connectionInfo;
    private BigInteger id;

}
