package io.Adrestus.p2p.kademlia.protocol.handler;

import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;
import io.Adrestus.p2p.kademlia.exception.FullBucketException;
import io.Adrestus.p2p.kademlia.node.KademliaNodeAPI;
import io.Adrestus.p2p.kademlia.protocol.message.KademliaMessage;
import io.Adrestus.p2p.kademlia.protocol.message.PingKademliaMessage;
import io.Adrestus.p2p.kademlia.protocol.message.PongKademliaMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingMessageHandler<ID extends Number, C extends ConnectionInfo> extends GeneralResponseMessageHandler<ID, C> {

    private static final Logger logger = LoggerFactory.getLogger(PingMessageHandler.class);

    @Override
    @SuppressWarnings("unchecked")
    public <I extends KademliaMessage<ID, C, ?>, O extends KademliaMessage<ID, C, ?>> O doHandle(KademliaNodeAPI<ID, C> kademliaNode, I message) {
        return (O) doHandle(kademliaNode, (PingKademliaMessage<ID, C>) message);
    }

    protected PongKademliaMessage<ID, C> doHandle(KademliaNodeAPI<ID, C> kademliaNode, PingKademliaMessage<ID, C> message) {
        if (kademliaNode.isRunning()) {
            try {
                kademliaNode.getRoutingTable().update(message.getNode());
            } catch (FullBucketException e) {
                logger.error(e.getMessage(), e);
            }
        }
        PongKademliaMessage<ID, C> pongKademliaMessage = new PongKademliaMessage<>();
        pongKademliaMessage.setAlive(kademliaNode.isRunning());
        return pongKademliaMessage;
    }
}
