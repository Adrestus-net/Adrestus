package io.Adrestus.p2p.kademlia.protocol.message;

import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;
import io.Adrestus.p2p.kademlia.protocol.MessageType;
import lombok.ToString;

@ToString
public class PingKademliaMessage<ID extends Number, C extends ConnectionInfo> extends KademliaMessage<ID, C, String> {
    public PingKademliaMessage() {
        super(MessageType.PING);
        setData("PING");
    }
}
