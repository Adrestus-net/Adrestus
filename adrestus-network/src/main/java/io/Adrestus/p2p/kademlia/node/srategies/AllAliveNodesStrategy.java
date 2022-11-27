package io.Adrestus.p2p.kademlia.node.srategies;

import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;
import io.Adrestus.p2p.kademlia.node.KademliaNodeAPI;
import io.Adrestus.p2p.kademlia.node.Node;
import io.Adrestus.p2p.kademlia.node.external.ExternalNode;
import io.Adrestus.p2p.kademlia.util.DateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AllAliveNodesStrategy implements ReferencedNodesStrategy {
    @Override
    public <ID extends Number, C extends ConnectionInfo> List<Node<ID, C>> getReferencedNodes(KademliaNodeAPI<ID, C> kademliaNode) {
        Date date = DateUtil.getDateOfSecondsAgo(kademliaNode.getNodeSettings().getMaximumLastSeenAgeToConsiderAlive());
        List<Node<ID, C>> referencedNodes = new ArrayList<>();

        kademliaNode.getRoutingTable().getBuckets().forEach(bucket -> {
            bucket.getNodeIds().forEach(id -> {
                ExternalNode<ID, C> node = bucket.getNode(id);
                if (node.getLastSeen().after(date)) {
                    referencedNodes.add(node);
                }
            });
        });

        return referencedNodes;
    }
}
