package io.Adrestus.p2p.kademlia.client;

import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.connection.MessageSender;
import io.Adrestus.p2p.kademlia.node.KademliaNodeAPI;
import io.Adrestus.p2p.kademlia.node.Node;
import io.Adrestus.p2p.kademlia.protocol.MessageType;
import io.Adrestus.p2p.kademlia.protocol.message.KademliaMessage;
import io.Adrestus.p2p.kademlia.serialization.GsonMessageSerializer;
import io.Adrestus.p2p.kademlia.serialization.MessageSerializer;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.rmi.ConnectException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Slf4j
public class OkHttpMessageSender<K extends Serializable, V extends Serializable> implements MessageSender<BigInteger, NettyConnectionInfo> {
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final MessageSerializer messageSerializer;
    private final OkHttpClient client;
    private final ExecutorService executorService;

    public OkHttpMessageSender(MessageSerializer messageSerializer, ExecutorService executorService) {
        this.messageSerializer = messageSerializer;
        this.executorService = executorService;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    public OkHttpMessageSender(MessageSerializer messageSerializer) {
        this(messageSerializer, Executors.newSingleThreadExecutor());
    }

    public OkHttpMessageSender(ExecutorService executorService, Class<K> kClass, Class<V> vClass) {
        this(new GsonMessageSerializer<K, V>(kClass, vClass), executorService);
    }

    public OkHttpMessageSender(Class<K> kClass, Class<V> vClass) {
        this(new GsonMessageSerializer<K, V>(kClass, vClass));
    }

    @Override
    public synchronized <I extends Serializable, O extends Serializable> KademliaMessage<BigInteger, NettyConnectionInfo, I> sendMessage(KademliaNodeAPI<BigInteger, NettyConnectionInfo> caller, Node<BigInteger, NettyConnectionInfo> receiver, KademliaMessage<BigInteger, NettyConnectionInfo, O> message) {
        message.setNode(caller);
        String messageStr = messageSerializer.serialize(message);
        RequestBody body = RequestBody.create(messageStr, JSON);
        Request request = new Request.Builder()
                .url(String.format("http://%s:%d/", receiver.getConnectionInfo().getHost(), receiver.getConnectionInfo().getPort()))
                .post(body)
                .build();
        // System.out.println(">>>>> from "+caller.getId()+" to "+receiver.getId()+ " data "+messageStr);
        try {
            Response response = client.newCall(request).execute();
            String responseStr = Objects.requireNonNull(response.body()).string();
            // System.out.println(">>>>> from "+receiver.getId()+" to "+caller.getId()+ " >body "+responseStr);
            return messageSerializer.deserialize(responseStr);
        } catch (IOException e) {
            //logger.info("Failed to Connect:");
            return new KademliaMessage<BigInteger, NettyConnectionInfo, I>() {
                @Override
                public I getData() {
                    return null;
                }

                @Override
                public String getType() {
                    return MessageType.EMPTY;
                }

                @Override
                public Node<BigInteger, NettyConnectionInfo> getNode() {
                    return receiver;
                }

                @Override
                public boolean isAlive() {
                    return false;
                }
            };
        } catch (Exception ex) {
            //logger.info("Failed to Connect:");
            return new KademliaMessage<BigInteger, NettyConnectionInfo, I>() {
                @Override
                public I getData() {
                    return null;
                }

                @Override
                public String getType() {
                    return MessageType.EMPTY;
                }

                @Override
                public Node<BigInteger, NettyConnectionInfo> getNode() {
                    return receiver;
                }

                @Override
                public boolean isAlive() {
                    return !(ex instanceof ConnectException);
                }
            };
        }
    }

    @Override
    public <O extends Serializable> void sendAsyncMessage(KademliaNodeAPI<BigInteger, NettyConnectionInfo> caller, Node<BigInteger, NettyConnectionInfo> receiver, KademliaMessage<BigInteger, NettyConnectionInfo, O> message) {
        executorService.submit(() -> sendMessage(caller, receiver, message));
    }

    public void stop() {
        this.executorService.shutdownNow();
    }

}
