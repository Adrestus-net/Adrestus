package io.Adrestus.p2p.kademlia.server;

import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.server.filter.Context;
import io.Adrestus.p2p.kademlia.server.filter.NettyKademliaServerFilter;
import io.Adrestus.p2p.kademlia.server.filter.NettyKademliaServerFilterChain;
import io.Adrestus.p2p.kademlia.node.DHTKademliaNodeAPI;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;


public class NettyKademliaMessageHandler<K extends Serializable, V extends Serializable> extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static Logger log = LoggerFactory.getLogger(NettyKademliaMessageHandler.class);
    private final NettyKademliaServerFilterChain<K, V> filterChain;
    private final DHTKademliaNodeAPI<BigInteger, NettyConnectionInfo, K, V> dhtKademliaNodeAPI;

    public NettyKademliaMessageHandler(DHTKademliaNodeAPI<BigInteger, NettyConnectionInfo, K, V> dhtKademliaNodeAPI, NettyKademliaServerFilterChain<K, V> filterChain) {
        this.filterChain = filterChain;
        this.dhtKademliaNodeAPI = dhtKademliaNodeAPI;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest request) {
        List<NettyKademliaServerFilter<K, V>> filters = filterChain.getFilters();
        if (filters.isEmpty()) {
            log.error("Filter Chain is empty. Closing connection.");
            channelHandlerContext.channel().close();
            return;
        }

        Context<K, V> context = new Context.ContextImpl<>(channelHandlerContext, dhtKademliaNodeAPI);

        NettyKademliaServerFilter<K, V> filter = filters.get(0);
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(
                request.protocolVersion(),
                OK
        );
        filter.filter(context, request, httpResponse);
        ChannelFuture f = channelHandlerContext.writeAndFlush(httpResponse);
        f.addListener(ChannelFutureListener.CLOSE);
    }
}
