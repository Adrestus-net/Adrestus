package io.Adrestus.p2p.kademlia.protocol.message;

import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;
import io.Adrestus.p2p.kademlia.protocol.MessageType;
import lombok.Getter;
import lombok.Setter;

public class PongKademliaMessage<ID extends Number, C extends ConnectionInfo> extends KademliaMessage<ID, C, String> {
    @Getter
    @Setter
    private boolean fromFindHandler = false;

    public PongKademliaMessage() {
        super(MessageType.PONG);
        setData("PONG");
    }
}
