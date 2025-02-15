package io.Adrestus.p2p.kademlia.protocol.handler;

import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;
import io.Adrestus.p2p.kademlia.exception.HandlerNotFoundException;
import io.Adrestus.p2p.kademlia.node.KademliaNodeAPI;
import io.Adrestus.p2p.kademlia.protocol.message.EmptyKademliaMessage;
import io.Adrestus.p2p.kademlia.protocol.message.KademliaMessage;
import io.Adrestus.p2p.kademlia.protocol.message.PongKademliaMessage;
import io.Adrestus.p2p.kademlia.util.RoutingTableUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PongMessageHandler<ID extends Number, C extends ConnectionInfo> extends GeneralResponseMessageHandler<ID, C> {
    private static final Logger logger = LoggerFactory.getLogger(PongMessageHandler.class);

    @Override
    @SuppressWarnings("unchecked")
    public <I extends KademliaMessage<ID, C, ?>, O extends KademliaMessage<ID, C, ?>> O doHandle(KademliaNodeAPI<ID, C> kademliaNode, I message) {
        return (O) doHandle(kademliaNode, (PongKademliaMessage<ID, C>) message);
    }

    protected EmptyKademliaMessage<ID, C> doHandle(KademliaNodeAPI<ID, C> kademliaNode, @NotNull PongKademliaMessage<ID, C> message) {
        try {
            RoutingTableUtil.softUpdate(kademliaNode, message.getNode());
        } catch (HandlerNotFoundException e) {
            logger.error(e.getMessage(), e);
        }
        return new EmptyKademliaMessage<>();
    }
}
