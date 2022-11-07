package io.Adrestus.p2p.kademlia.serialization;

import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.protocol.message.KademliaMessage;

import java.io.Serializable;
import java.math.BigInteger;


public interface MessageSerializer {

    <S extends Serializable> String serialize(KademliaMessage<BigInteger, NettyConnectionInfo, S> message);

    <S extends Serializable> KademliaMessage<BigInteger, NettyConnectionInfo, S> deserialize(String message);

}
