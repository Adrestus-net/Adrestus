package io.Adrestus.p2p.kademlia;

import io.Adrestus.config.KademliaConfiguration;
import io.Adrestus.config.NodeSettings;
import io.Adrestus.p2p.kademlia.exception.FullBucketException;
import io.Adrestus.p2p.kademlia.helpers.EmptyConnectionInfo;
import io.Adrestus.p2p.kademlia.helpers.TestMessageSenderAPI;
import io.Adrestus.p2p.kademlia.node.KademliaNode;
import io.Adrestus.p2p.kademlia.node.KademliaNodeAPI;
import io.Adrestus.p2p.kademlia.table.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class RoutingTableTest {

    @Test
    public void test() throws FullBucketException {
        TestMessageSenderAPI<Integer, EmptyConnectionInfo> messageSenderAPI = new TestMessageSenderAPI<>();

        KademliaConfiguration.IDENTIFIER_SIZE = 3;
        KademliaConfiguration.BUCKET_SIZE = 100;
        KademliaConfiguration.PING_SCHEDULE_TIME_VALUE = 5;

        NodeSettings.getInstance();
        RoutingTableFactory<Integer, EmptyConnectionInfo, Bucket<Integer, EmptyConnectionInfo>> routingTableFactory = new DefaultRoutingTableFactory<>(NodeSettings.getInstance());


        // Bootstrap Node
        KademliaNodeAPI<Integer, EmptyConnectionInfo> bootstrapNode = new KademliaNode<>(0, new EmptyConnectionInfo(), routingTableFactory.getRoutingTable(0), messageSenderAPI, NodeSettings.getInstance());
        messageSenderAPI.registerNode(bootstrapNode);
        bootstrapNode.start();

        RoutingTable<Integer, EmptyConnectionInfo, Bucket<Integer, EmptyConnectionInfo>> routingTable=routingTableFactory.getRoutingTable(4);
        ArrayList< KademliaNodeAPI<Integer, EmptyConnectionInfo>>list =new ArrayList<>();
        // Other nodes
        KademliaNodeAPI<Integer, EmptyConnectionInfo> nextNode=null;
        for(int i = 1; i < Math.pow(2, NodeSettings.getInstance().getIdentifierSize()); i++){
            nextNode =
                    new KademliaNode<>(i, new EmptyConnectionInfo(), routingTableFactory.getRoutingTable(i), messageSenderAPI, NodeSettings.getInstance());
            messageSenderAPI.registerNode(nextNode);
            routingTable.update(nextNode);
            list.add(nextNode);
        }
    }
}
