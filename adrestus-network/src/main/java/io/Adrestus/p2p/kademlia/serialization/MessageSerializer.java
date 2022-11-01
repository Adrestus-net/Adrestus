package io.Adrestus.p2p.kademlia.serialization;


import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.protocol.message.KademliaMessage;

import java.io.Serializable;


public interface MessageSerializer {

    <S extends Serializable> String serialize(KademliaMessage<Long, NettyConnectionInfo, S> message);
    <S extends Serializable> KademliaMessage<Long, NettyConnectionInfo, S> deserialize(String message);

}
