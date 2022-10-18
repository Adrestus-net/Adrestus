package io.Adrestus.kademlia.p2p.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.Adrestus.kademlia.p2p.server.NettyKademliaMessageHandler;
import io.ep2p.kademlia.connection.MessageSender;
import io.Adrestus.kademlia.p2p.common.NettyConnectionInfo;
import io.Adrestus.kademlia.p2p.factory.GsonFactory;
import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.protocol.MessageType;
import io.ep2p.kademlia.protocol.message.KademliaMessage;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.SocketTimeoutException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Slf4j
public class NettyMessageSender<K extends Serializable, V extends Serializable> implements MessageSender<BigInteger, NettyConnectionInfo> {
    private static Logger log = LoggerFactory.getLogger(NettyMessageSender.class);

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final Gson gson;
    private final OkHttpClient client;
    private final ExecutorService executorService;

    public NettyMessageSender(Gson gson, ExecutorService executorService) {
        this.gson = gson;
        this.executorService = executorService;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    public NettyMessageSender(Gson gson) {
        this(gson, Executors.newSingleThreadExecutor());
    }

    public NettyMessageSender(ExecutorService executorService){
        this(new GsonFactory.DefaultGsonFactory<K, V>().gson(), executorService);
    }

    public NettyMessageSender() {
        this(new GsonFactory.DefaultGsonFactory<K, V>().gson());
    }

    @Override
    public <I extends Serializable, O extends Serializable> KademliaMessage<BigInteger, NettyConnectionInfo, I> sendMessage(KademliaNodeAPI<BigInteger, NettyConnectionInfo> caller, Node<BigInteger, NettyConnectionInfo> receiver, KademliaMessage<BigInteger, NettyConnectionInfo, O> message) {
        message.setNode(caller);

        RequestBody body = RequestBody.create(gson.toJson(message), JSON);
        Request request = new Request.Builder()
                .url(String.format("http://%s:%d/", receiver.getConnectionInfo().getHost(), receiver.getConnectionInfo().getPort()))
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseStr = Objects.requireNonNull(response.body()).string();
            return gson.fromJson(responseStr, new TypeToken<KademliaMessage<BigInteger, NettyConnectionInfo, Serializable>>() {}.getType());
        } catch (IOException e) {
            log.error("Failed to send message to " + caller.getId(), e);
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
                    return !(e instanceof SocketTimeoutException);
                }
            };
        }
    }

    @Override
    public <O extends Serializable> void sendAsyncMessage(KademliaNodeAPI<BigInteger, NettyConnectionInfo> caller, Node<BigInteger, NettyConnectionInfo> receiver, KademliaMessage<BigInteger, NettyConnectionInfo, O> message) {
        executorService.submit(() -> sendMessage(caller, receiver, message));
    }

    public void stop(){
        this.executorService.shutdownNow();
    }

}
