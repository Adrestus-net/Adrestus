package io.Adrestus.p2p.kademlia.protocol.message;

import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;
import io.Adrestus.p2p.kademlia.protocol.MessageType;

public class FindNodeRequestMessage<ID extends Number, C extends ConnectionInfo> extends KademliaMessage<ID, C, ID> {
    public FindNodeRequestMessage() {
        super(MessageType.FIND_NODE_REQ);
    }

    public ID getDestinationId() {
        return this.getData();
    }
}
