package io.Adrestus.p2p.kademlia.server.filter;


import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.protocol.message.KademliaMessage;
import io.Adrestus.p2p.kademlia.serialization.MessageSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;


@Slf4j
public class KademliaMainHandlerFilter<K extends Serializable, V extends Serializable> extends NettyKademliaServerFilter<K, V> {

    private final MessageSerializer messageSerializer;

    public KademliaMainHandlerFilter(MessageSerializer messageSerializer) {
        this.messageSerializer = messageSerializer;
    }

    @Override
    public void filter(Context<K, V> context, FullHttpRequest request, FullHttpResponse response) {
        KademliaMessage<BigInteger, NettyConnectionInfo, ? extends Serializable> responseMessage = null;

        try {
            KademliaMessage<BigInteger, NettyConnectionInfo, Serializable> kademliaMessage = this.toKademliaMessage(
                    this.parseJsonRequest(request)
            );
            responseMessage = context.getDhtKademliaNodeApi().onMessage(kademliaMessage);
            responseMessage.setNode(context.getDhtKademliaNodeApi());
        } catch (Exception e){
            logger.error("Failed to parse request and pass it to the node api", e);
            response.setStatus(BAD_REQUEST);
        }

        response.content().writeBytes(
                Unpooled.wrappedBuffer(this.messageSerializer.serialize(responseMessage).getBytes(StandardCharsets.UTF_8))
        );

        response.headers()
                .set(CONTENT_TYPE, APPLICATION_JSON)
                .setInt(CONTENT_LENGTH, response.content().readableBytes());

        super.filter(context, request, response);
    }

    protected String parseJsonRequest(FullHttpRequest request){
        ByteBuf jsonBuf = request.content();
        return jsonBuf.toString(CharsetUtil.UTF_8);
    }

    protected KademliaMessage<BigInteger, NettyConnectionInfo, Serializable> toKademliaMessage(String message) {
        return this.messageSerializer.deserialize(
                message
        );
    }
}
