package io.Adrestus.kademlia.p2p.server.filter;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

import java.io.Serializable;

public abstract class NettyKademliaServerFilter<K extends Serializable, V extends Serializable> {
    protected NettyKademliaServerFilter<K, V> next;

    public final void setNext(NettyKademliaServerFilter<K, V> nettyKademliaServerFilter){
        this.next = nettyKademliaServerFilter;
    }

    public void filter(Context<K, V> context, FullHttpRequest request, FullHttpResponse response){
        if (next != null){
            next.filter(context, request, response);
        }
    }

}
