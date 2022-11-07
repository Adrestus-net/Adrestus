package io.Adrestus.p2p.kademlia.serialization;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.factory.GsonFactory;
import io.Adrestus.p2p.kademlia.protocol.MessageType;
import io.Adrestus.p2p.kademlia.protocol.message.*;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GsonMessageSerializer<K extends Serializable, V extends Serializable> implements MessageSerializer, JsonDeserializer<KademliaMessage<BigInteger, NettyConnectionInfo, ? extends Serializable>> {
    private final Gson gson;
    private final Map<String, Type> messageTypeRegistry = new ConcurrentHashMap<>();

    public GsonMessageSerializer() {
        this(new GsonFactory.DefaultGsonFactory<K, V>().gsonBuilder());
    }

    public GsonMessageSerializer(GsonBuilder gsonBuilder) {
        gsonBuilder.registerTypeAdapter(KademliaMessage.class, this);
        this.gson = gsonBuilder.create();
        init();
    }

    protected void init() {
        this.registerMessageType(MessageType.DHT_LOOKUP, new TypeToken<DHTLookupKademliaMessage<BigInteger, NettyConnectionInfo, K>>() {
        }.getType());
        this.registerMessageType(MessageType.DHT_LOOKUP_RESULT, new TypeToken<DHTLookupResultKademliaMessage<BigInteger, NettyConnectionInfo, K, V>>() {
        }.getType());
        this.registerMessageType(MessageType.DHT_STORE, new TypeToken<DHTStoreKademliaMessage<BigInteger, NettyConnectionInfo, K, V>>() {
        }.getType());
        this.registerMessageType(MessageType.DHT_STORE_RESULT, new TypeToken<DHTStoreResultKademliaMessage<BigInteger, NettyConnectionInfo, K>>() {
        }.getType());
        this.registerMessageType(MessageType.FIND_NODE_REQ, new TypeToken<FindNodeRequestMessage<BigInteger, NettyConnectionInfo>>() {
        }.getType());
        this.registerMessageType(MessageType.FIND_NODE_RES, new TypeToken<FindNodeResponseMessage<BigInteger, NettyConnectionInfo>>() {
        }.getType());
        this.registerMessageType(MessageType.PING, new TypeToken<PingKademliaMessage<BigInteger, NettyConnectionInfo>>() {
        }.getType());
        this.registerMessageType(MessageType.PONG, new TypeToken<PongKademliaMessage<BigInteger, NettyConnectionInfo>>() {
        }.getType());
        this.registerMessageType(MessageType.SHUTDOWN, new TypeToken<ShutdownKademliaMessage<BigInteger, NettyConnectionInfo>>() {
        }.getType());
        this.registerMessageType(MessageType.EMPTY, new TypeToken<EmptyKademliaMessage<BigInteger, NettyConnectionInfo>>() {
        }.getType());
    }

    @Override
    public <S extends Serializable> String serialize(KademliaMessage<BigInteger, NettyConnectionInfo, S> message) {
        return this.gson.toJson(message);
    }

    @Override
    public <S extends Serializable> KademliaMessage<BigInteger, NettyConnectionInfo, S> deserialize(String message) {
        return this.gson.fromJson(message, new TypeToken<KademliaMessage<BigInteger, NettyConnectionInfo, Serializable>>() {
        }.getType());
    }

    @Override
    public KademliaMessage<BigInteger, NettyConnectionInfo, ?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String messageType = jsonObject.getAsJsonPrimitive("type").getAsString();
        return this.gson.fromJson(jsonObject, this.messageTypeRegistry.get(messageType));
    }

    public void registerMessageType(String name, Type type) {
        this.messageTypeRegistry.put(name, type);
    }

}
