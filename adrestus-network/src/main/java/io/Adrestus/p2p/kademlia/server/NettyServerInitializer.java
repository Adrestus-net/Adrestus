package io.Adrestus.p2p.kademlia.server;

import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.node.DHTKademliaNodeAPI;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelPipeline;

import java.io.Serializable;

public interface NettyServerInitializer<K extends Serializable, V extends Serializable> extends ChannelInboundHandler {
    void registerKademliaNode(DHTKademliaNodeAPI<Long, NettyConnectionInfo, K, V> dhtKademliaNodeAPI);

    void pipelineInitializer(ChannelPipeline pipeline);
}
