package io.Adrestus.p2p.kademlia.server.filter;

import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.node.DHTKademliaNodeAPI;
import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;
import java.math.BigInteger;


public interface Context<K extends Serializable, V extends Serializable> {
    ChannelHandlerContext getChannelHandlerContext();

    DHTKademliaNodeAPI<BigInteger, NettyConnectionInfo, K, V> getDhtKademliaNodeApi();

    class ContextImpl<K extends Serializable, V extends Serializable> implements Context<K, V> {
        private final ChannelHandlerContext channelHandlerContext;
        private final DHTKademliaNodeAPI<BigInteger, NettyConnectionInfo, K, V> getDhtKademliaNode;

        public ContextImpl(ChannelHandlerContext channelHandlerContext, DHTKademliaNodeAPI<BigInteger, NettyConnectionInfo, K, V> getDhtKademliaNode) {
            this.channelHandlerContext = channelHandlerContext;
            this.getDhtKademliaNode = getDhtKademliaNode;
        }

        @Override
        public ChannelHandlerContext getChannelHandlerContext() {
            return channelHandlerContext;
        }

        @Override
        public DHTKademliaNodeAPI<BigInteger, NettyConnectionInfo, K, V> getDhtKademliaNodeApi() {
            return getDhtKademliaNode;
        }
    }
}
