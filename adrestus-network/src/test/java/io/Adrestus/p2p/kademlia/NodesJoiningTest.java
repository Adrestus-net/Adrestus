package io.Adrestus.p2p.kademlia;

import io.Adrestus.config.KademliaConfiguration;
import io.Adrestus.config.NodeSettings;
import io.Adrestus.p2p.kademlia.helpers.EmptyConnectionInfo;
import io.Adrestus.p2p.kademlia.helpers.TestMessageSenderAPI;
import io.Adrestus.p2p.kademlia.node.KademliaNode;
import io.Adrestus.p2p.kademlia.node.KademliaNodeAPI;
import io.Adrestus.p2p.kademlia.node.Node;
import io.Adrestus.p2p.kademlia.node.srategies.ClosestPerBucketReferencedNodeStrategy;
import io.Adrestus.p2p.kademlia.table.Bucket;
import io.Adrestus.p2p.kademlia.table.DefaultRoutingTableFactory;
import io.Adrestus.p2p.kademlia.table.RoutingTableFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class NodesJoiningTest {
    @Test
    public void canPeersJoinNetwork() throws InterruptedException, ExecutionException {
        TestMessageSenderAPI<Integer, EmptyConnectionInfo> messageSenderAPI = new TestMessageSenderAPI<>();

        KademliaConfiguration.IDENTIFIER_SIZE = 4;
        KademliaConfiguration.BUCKET_SIZE = 100;
        KademliaConfiguration.PING_SCHEDULE_TIME_VALUE = 5;
        Thread.sleep(2000);
        NodeSettings.clean();
        NodeSettings.getInstance();
        RoutingTableFactory<Integer, EmptyConnectionInfo, Bucket<Integer, EmptyConnectionInfo>> routingTableFactory = new DefaultRoutingTableFactory<>(NodeSettings.getInstance());
        ClosestPerBucketReferencedNodeStrategy closestPerBucketReferencedNodeStrategy = new ClosestPerBucketReferencedNodeStrategy();

        // Bootstrap Node
        KademliaNodeAPI<Integer, EmptyConnectionInfo> bootstrapNode = new KademliaNode<>(0, new EmptyConnectionInfo(), routingTableFactory.getRoutingTable(0), messageSenderAPI, NodeSettings.getInstance());
        messageSenderAPI.registerNode(bootstrapNode);
        bootstrapNode.start();
        System.out.println(NodeSettings.getInstance().getIdentifierSize());
        // Other nodes
        for (int i = 1; i < Math.pow(2, NodeSettings.getInstance().getIdentifierSize()); i++) {
            KademliaNodeAPI<Integer, EmptyConnectionInfo> nextNode = new KademliaNode<>(i, new EmptyConnectionInfo(), routingTableFactory.getRoutingTable(i), messageSenderAPI, NodeSettings.getInstance());
            messageSenderAPI.registerNode(nextNode);
            Assertions.assertTrue(nextNode.start(bootstrapNode).get(), "Failed to bootstrap the node with ID " + i);
        }

        Thread.sleep(2000);

        // Test if nodes know about each other

        Assertions.assertTrue(listContainsAll(closestPerBucketReferencedNodeStrategy.getReferencedNodes(messageSenderAPI.map.get(0)), 1, 2, 4, 8));
        Assertions.assertTrue(listContainsAll(closestPerBucketReferencedNodeStrategy.getReferencedNodes(messageSenderAPI.map.get(1)), 0, 3, 5, 9));
        Assertions.assertTrue(listContainsAll(closestPerBucketReferencedNodeStrategy.getReferencedNodes(messageSenderAPI.map.get(2)), 3, 0, 6, 10));
        Assertions.assertTrue(listContainsAll(closestPerBucketReferencedNodeStrategy.getReferencedNodes(messageSenderAPI.map.get(3)), 2, 1, 7, 11));
        Assertions.assertTrue(listContainsAll(closestPerBucketReferencedNodeStrategy.getReferencedNodes(messageSenderAPI.map.get(15)), 14, 13, 11, 7));
        Assertions.assertTrue(listContainsAll(closestPerBucketReferencedNodeStrategy.getReferencedNodes(messageSenderAPI.map.get(7)), 6, 5, 3, 15));


        // stop all
        messageSenderAPI.stopAll();
    }

    private boolean listContainsAll(List<Node<Integer, EmptyConnectionInfo>> referencedNodes, Integer... nodeIds) {
        List<Integer> nodeIdsToContain = new ArrayList<>(Arrays.asList(nodeIds));
        for (Node<Integer, EmptyConnectionInfo> referencedNode : referencedNodes) {
            nodeIdsToContain.remove(referencedNode.getId());
        }
        return nodeIdsToContain.size() == 0;
    }
}
