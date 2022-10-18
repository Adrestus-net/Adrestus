package io.Adrestus.kademlia.p2p.server;

import io.Adrestus.kademlia.p2p.common.NettyConnectionInfo;
import io.ep2p.kademlia.node.DHTKademliaNodeAPI;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelPipeline;

import java.io.Serializable;
import java.math.BigInteger;

public interface NettyServerInitializer<K extends Serializable, V extends Serializable> extends ChannelInboundHandler {
    void registerKademliaNode(DHTKademliaNodeAPI<BigInteger, NettyConnectionInfo, K, V> dhtKademliaNodeAPI);
    void pipelineInitializer(ChannelPipeline pipeline);
}
