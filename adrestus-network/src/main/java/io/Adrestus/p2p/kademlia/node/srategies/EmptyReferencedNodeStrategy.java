package io.Adrestus.p2p.kademlia.node.srategies;

import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;
import io.Adrestus.p2p.kademlia.node.KademliaNodeAPI;
import io.Adrestus.p2p.kademlia.node.Node;

import java.util.ArrayList;
import java.util.List;

public class EmptyReferencedNodeStrategy implements ReferencedNodesStrategy{
    @Override
    public <ID extends Number, C extends ConnectionInfo> List<Node<ID, C>> getReferencedNodes(KademliaNodeAPI<ID, C> kademliaNode) {
        return new ArrayList<>();
    }
}
