package io.Adrestus.p2p.kademlia.server;

import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.factory.KademliaMessageHandlerFactory;
import io.ep2p.kademlia.node.DHTKademliaNodeAPI;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import java.io.Serializable;
import java.math.BigInteger;

public class DefaultNettyServerInitializer<K extends Serializable, V extends Serializable> extends ChannelInitializer<SocketChannel> implements NettyServerInitializer<K, V> {
    protected DHTKademliaNodeAPI<BigInteger, NettyConnectionInfo, K, V> dhtKademliaNodeAPI;
    private final KademliaMessageHandlerFactory<K, V> kademliaMessageHandlerFactory;

    public DefaultNettyServerInitializer(KademliaMessageHandlerFactory<K, V> kademliaMessageHandlerFactory) {
        this.kademliaMessageHandlerFactory = kademliaMessageHandlerFactory;
    }

    @Override
    public void registerKademliaNode(DHTKademliaNodeAPI<BigInteger, NettyConnectionInfo, K, V> dhtKademliaNodeAPI) {
        this.dhtKademliaNodeAPI = dhtKademliaNodeAPI;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        this.pipelineInitializer(pipeline);
    }

    @Override
    public void pipelineInitializer(ChannelPipeline pipeline) {
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(Integer.MAX_VALUE));
        pipeline.addLast(this.kademliaMessageHandlerFactory.getKademliaMessageHandler(this.dhtKademliaNodeAPI));
    }
}
