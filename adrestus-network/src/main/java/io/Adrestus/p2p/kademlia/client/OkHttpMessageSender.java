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
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Slf4j
public class OkHttpMessageSender<K extends Serializable, V extends Serializable> implements MessageSender<Long, NettyConnectionInfo> {
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

    public OkHttpMessageSender(ExecutorService executorService) {
        this(new GsonMessageSerializer<K, V>(), executorService);
    }

    public OkHttpMessageSender() {
        this(new GsonMessageSerializer<K, V>());
    }

    @Override
    public synchronized <I extends Serializable, O extends Serializable> KademliaMessage<Long, NettyConnectionInfo, I> sendMessage(KademliaNodeAPI<Long, NettyConnectionInfo> caller, Node<Long, NettyConnectionInfo> receiver, KademliaMessage<Long, NettyConnectionInfo, O> message) {
        message.setNode(caller);
        String messageStr = messageSerializer.serialize(message);
        RequestBody body = RequestBody.create(messageStr, JSON);
        Request request = new Request.Builder()
                .url(String.format("http://%s:%d/", receiver.getConnectionInfo().getHost(), receiver.getConnectionInfo().getPort()))
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseStr = Objects.requireNonNull(response.body()).string();
            return messageSerializer.deserialize(responseStr);
        } catch (ConnectException ex) {
            logger.info("Failed to Connect:");
            return new KademliaMessage<Long, NettyConnectionInfo, I>() {
                @Override
                public I getData() {
                    return null;
                }

                @Override
                public String getType() {
                    return MessageType.EMPTY;
                }

                @Override
                public Node<Long, NettyConnectionInfo> getNode() {
                    return receiver;
                }

                @Override
                public boolean isAlive() {
                    return !(ex instanceof ConnectException);
                }
            };
        } catch (IOException e) {
            logger.error("Failed to send message to " + caller.getId(), e);
            return new KademliaMessage<Long, NettyConnectionInfo, I>() {
                @Override
                public I getData() {
                    return null;
                }

                @Override
                public String getType() {
                    return MessageType.EMPTY;
                }

                @Override
                public Node<Long, NettyConnectionInfo> getNode() {
                    return receiver;
                }

                @Override
                public boolean isAlive() {
                    return !(e instanceof SocketTimeoutException);
                }
            };
        }
    }

    @Override
    public <O extends Serializable> void sendAsyncMessage(KademliaNodeAPI<Long, NettyConnectionInfo> caller, Node<Long, NettyConnectionInfo> receiver, KademliaMessage<Long, NettyConnectionInfo, O> message) {
        executorService.submit(() -> sendMessage(caller, receiver, message));
    }

    public void stop() {
        this.executorService.shutdownNow();
    }

}
