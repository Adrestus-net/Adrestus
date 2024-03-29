package io.Adrestus.p2p.kademlia.protocol.message;

import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;
import io.Adrestus.p2p.kademlia.protocol.MessageType;
import lombok.ToString;

import java.io.Serializable;

@ToString
public class ShutdownKademliaMessage<ID extends Number, C extends ConnectionInfo> extends KademliaMessage<ID, C, Serializable> {

    public ShutdownKademliaMessage() {
        super(MessageType.SHUTDOWN);
    }
}
