package io.Adrestus.p2p.kademlia.node.srategies;

import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;
import io.Adrestus.p2p.kademlia.node.KademliaNodeAPI;
import io.Adrestus.p2p.kademlia.node.Node;
import lombok.Getter;

import java.util.List;

public interface ReferencedNodesStrategy {
    <ID extends Number, C extends ConnectionInfo> List<Node<ID, C>> getReferencedNodes(KademliaNodeAPI<ID, C> kademliaNode);

    enum Strategies {
        CLOSEST_PER_BUCKET(new ClosestPerBucketReferencedNodeStrategy()), EMPTY(new EmptyReferencedNodeStrategy()), ALL_ALIVE(new AllAliveNodesStrategy());

        @Getter
        private final ReferencedNodesStrategy referencedNodesStrategy;

        Strategies(ReferencedNodesStrategy referencedNodesStrategy) {
            this.referencedNodesStrategy = referencedNodesStrategy;
        }
    }
}
