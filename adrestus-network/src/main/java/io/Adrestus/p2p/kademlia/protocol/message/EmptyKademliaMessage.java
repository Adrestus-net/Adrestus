package io.Adrestus.p2p.kademlia.protocol.message;

import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;
import io.Adrestus.p2p.kademlia.protocol.MessageType;
import lombok.ToString;

import java.io.Serializable;

@ToString
public class EmptyKademliaMessage<ID extends Number, C extends ConnectionInfo> extends KademliaMessage<ID, C, Serializable> {
    public EmptyKademliaMessage() {
        super(MessageType.EMPTY);
    }
}
