package io.Adrestus.p2p.kademlia.protocol.handler;

import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;
import io.Adrestus.p2p.kademlia.exception.FullBucketException;
import io.Adrestus.p2p.kademlia.model.FindNodeAnswer;
import io.Adrestus.p2p.kademlia.node.KademliaNodeAPI;
import io.Adrestus.p2p.kademlia.protocol.message.FindNodeRequestMessage;
import io.Adrestus.p2p.kademlia.protocol.message.FindNodeResponseMessage;
import io.Adrestus.p2p.kademlia.protocol.message.KademliaMessage;
import io.Adrestus.p2p.kademlia.util.RoutingTableUtil;
import lombok.SneakyThrows;

import java.io.Serializable;

public class FindNodeRequestMessageHandler<ID extends Number, C extends ConnectionInfo> extends GeneralResponseMessageHandler<ID, C> {

    public <I extends KademliaMessage<ID, C, ? extends Serializable>, O extends KademliaMessage<ID, C, ? extends Serializable>> O handleRequest(KademliaNodeAPI<ID, C> kademliaNode, I message) {
        return (O) doHandle(kademliaNode, (FindNodeRequestMessage<ID, C>) message);
    }

    @SneakyThrows
    protected FindNodeResponseMessage<ID, C> doHandle(KademliaNodeAPI<ID, C> kademliaNode, FindNodeRequestMessage<ID, C> message) {
        FindNodeAnswer<ID, C> findNodeAnswer = kademliaNode.getRoutingTable().findClosest(message.getDestinationId());

        try {
            kademliaNode.getRoutingTable().update(message.getNode());
        } catch (FullBucketException e) {
            RoutingTableUtil.softUpdate(kademliaNode, message.getNode());
        }

        FindNodeResponseMessage<ID, C> response = new FindNodeResponseMessage<ID, C>();
        response.setData(findNodeAnswer);
        return response;
    }
}