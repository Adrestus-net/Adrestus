package io.Adrestus.p2p.kademlia.common;

import io.Adrestus.p2p.kademlia.node.Node;
import lombok.*;


@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NettyExternalNode implements Node<Long, NettyConnectionInfo> {
    private NettyConnectionInfo connectionInfo;
    private Long id;

}
