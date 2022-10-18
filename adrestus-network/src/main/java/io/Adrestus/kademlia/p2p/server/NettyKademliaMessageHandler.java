package io.Adrestus.kademlia.p2p.server;

import io.Adrestus.kademlia.p2p.common.NettyConnectionInfo;
import io.Adrestus.kademlia.p2p.server.filter.Context;
import io.Adrestus.kademlia.p2p.server.filter.NettyKademliaServerFilter;
import io.Adrestus.kademlia.p2p.server.filter.NettyKademliaServerFilterChain;
import io.Adrestus.network.ConsensusServer;
import io.ep2p.kademlia.node.DHTKademliaNodeAPI;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;


@Slf4j
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
        if (filters.isEmpty()){
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
