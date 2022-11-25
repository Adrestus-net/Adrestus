package io.Adrestus.p2p.kademlia.protocol.message;

import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;
import io.Adrestus.p2p.kademlia.model.FindNodeAnswer;
import io.Adrestus.p2p.kademlia.protocol.MessageType;
import lombok.ToString;

@ToString
public class FindNodeResponseMessage<ID extends Number, C extends ConnectionInfo> extends KademliaMessage<ID, C, FindNodeAnswer<ID, C>> {
    public FindNodeResponseMessage() {
        super(MessageType.FIND_NODE_RES);
    }
}
