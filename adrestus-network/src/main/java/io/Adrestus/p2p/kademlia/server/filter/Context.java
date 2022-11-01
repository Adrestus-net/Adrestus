package io.Adrestus.p2p.kademlia.server.filter;

import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.node.DHTKademliaNodeAPI;
import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;

public interface Context<K extends Serializable, V extends Serializable> {
    ChannelHandlerContext getChannelHandlerContext();

    DHTKademliaNodeAPI<Long, NettyConnectionInfo, K, V> getDhtKademliaNodeApi();

    class ContextImpl<K extends Serializable, V extends Serializable> implements Context<K, V> {
        private final ChannelHandlerContext channelHandlerContext;
        private final DHTKademliaNodeAPI<Long, NettyConnectionInfo, K, V> getDhtKademliaNode;

        public ContextImpl(ChannelHandlerContext channelHandlerContext, DHTKademliaNodeAPI<Long, NettyConnectionInfo, K, V> getDhtKademliaNode) {
            this.channelHandlerContext = channelHandlerContext;
            this.getDhtKademliaNode = getDhtKademliaNode;
        }

        @Override
        public ChannelHandlerContext getChannelHandlerContext() {
            return channelHandlerContext;
        }

        @Override
        public DHTKademliaNodeAPI<Long, NettyConnectionInfo, K, V> getDhtKademliaNodeApi() {
            return getDhtKademliaNode;
        }
    }
}
