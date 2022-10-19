package io.Adrestus.p2p.kademlia.protocol.handler;

import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;
import io.Adrestus.p2p.kademlia.node.KademliaNodeAPI;
import io.Adrestus.p2p.kademlia.protocol.message.EmptyKademliaMessage;
import io.Adrestus.p2p.kademlia.protocol.message.KademliaMessage;
import io.Adrestus.p2p.kademlia.protocol.message.ShutdownKademliaMessage;

public class ShutdownMessageHandler<ID extends Number, C extends ConnectionInfo> implements MessageHandler<ID, C> {
    @Override
    @SuppressWarnings("unchecked")
    public <I extends KademliaMessage<ID, C, ?>, O extends KademliaMessage<ID, C, ?>> O handle(KademliaNodeAPI<ID, C> kademliaNode, I message) {
        assert message instanceof ShutdownKademliaMessage;
        kademliaNode.getRoutingTable().delete(message.getNode());
        return (O) new EmptyKademliaMessage<ID, C>();
    }
}
