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
    private Class<K> kClass;
    private Class<V> vClass;

    public GsonMessageSerializer(Class<K> kClass, Class<V> vClass) {
        this(kClass, vClass, new GsonFactory.DefaultGsonFactory<K, V>(kClass, vClass).gsonBuilder());
    }

    public GsonMessageSerializer(Class<K> kClass, Class<V> vClass, GsonBuilder gsonBuilder) {
        this.kClass = kClass;
        this.vClass = vClass;
        gsonBuilder.registerTypeAdapter(KademliaMessage.class, new KademliaMessageDeserializer<K, V>(kClass, vClass));
        this.gson = gsonBuilder.create();
        init();
    }

    protected void init() {
        this.registerMessageType(MessageType.DHT_LOOKUP, TypeToken.getParameterized(DHTLookupKademliaMessage.DHTLookup.class, BigInteger.class, NettyConnectionInfo.class, kClass).getType());
        this.registerMessageType(MessageType.DHT_LOOKUP_RESULT, TypeToken.getParameterized(DHTLookupResultKademliaMessage.class, BigInteger.class, NettyConnectionInfo.class, kClass, vClass).getType());
        this.registerMessageType(MessageType.DHT_STORE, TypeToken.getParameterized(DHTStoreKademliaMessage.DHTData.class, BigInteger.class, NettyConnectionInfo.class, kClass, vClass).getType());
        this.registerMessageType(MessageType.DHT_STORE_RESULT, TypeToken.getParameterized(DHTStoreResultKademliaMessage.DHTStoreResult.class, kClass).getType());
        this.registerMessageType(MessageType.FIND_NODE_REQ, TypeToken.getParameterized(FindNodeRequestMessage.class, BigInteger.class, NettyConnectionInfo.class).getType());
        this.registerMessageType(MessageType.FIND_NODE_RES, TypeToken.getParameterized(FindNodeResponseMessage.class, BigInteger.class, NettyConnectionInfo.class).getType());
        this.registerMessageType(MessageType.PING, TypeToken.getParameterized(PingKademliaMessage.class, BigInteger.class, NettyConnectionInfo.class).getType());
        this.registerMessageType(MessageType.PONG, TypeToken.getParameterized(PongKademliaMessage.class, BigInteger.class, NettyConnectionInfo.class).getType());
        this.registerMessageType(MessageType.SHUTDOWN, TypeToken.getParameterized(ShutdownKademliaMessage.class, BigInteger.class, NettyConnectionInfo.class).getType());
        this.registerMessageType(MessageType.EMPTY, TypeToken.getParameterized(EmptyKademliaMessage.class, BigInteger.class, NettyConnectionInfo.class).getType());
    }

    @Override
    public <S extends Serializable> String serialize(KademliaMessage<BigInteger, NettyConnectionInfo, S> message) {
        return this.gson.toJson(message);
    }

    @Override
    public <S extends Serializable> KademliaMessage<BigInteger, NettyConnectionInfo, S> deserialize(String message) {
        return this.gson.fromJson(message, TypeToken.getParameterized(KademliaMessage.class, BigInteger.class, NettyConnectionInfo.class, Serializable.class).getType());
    }

    @Override
    public KademliaMessage<BigInteger, NettyConnectionInfo, ?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String message = jsonObject.getAsJsonPrimitive("type").getAsString();
        // After
        Type messageType = this.messageTypeRegistry.get(message);
        if (messageType == null) {
            throw new JsonParseException("Unknown message type: " + messageType);
        }
        return this.gson.fromJson(jsonObject, messageType);
    }

    public void registerMessageType(String name, Type type) {
        this.messageTypeRegistry.put(name, type);
    }

}
