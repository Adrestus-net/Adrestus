package io.Adrestus.p2p.kademlia.serialization;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.model.FindNodeAnswer;
import io.Adrestus.p2p.kademlia.node.Node;
import io.Adrestus.p2p.kademlia.protocol.MessageType;
import io.Adrestus.p2p.kademlia.protocol.message.*;
import lombok.SneakyThrows;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class KademliaMessageDeserializer<K extends Serializable, V extends Serializable> implements JsonDeserializer<KademliaMessage<BigInteger, NettyConnectionInfo, Serializable>> {
    private final Map<String, Type> typeRegistry = new ConcurrentHashMap<>();
    private final Map<String, Class<?>> messageClassRegistry = new ConcurrentHashMap<>();

    public KademliaMessageDeserializer() {
        this.registerDataType(MessageType.DHT_LOOKUP, new TypeToken<DHTLookupKademliaMessage.DHTLookup<BigInteger, NettyConnectionInfo, K>>(){}.getType());
        this.registerMessageClass(MessageType.DHT_LOOKUP, DHTLookupKademliaMessage.class);
        this.registerDataType(MessageType.DHT_LOOKUP_RESULT, new TypeToken<DHTLookupResultKademliaMessage.DHTLookupResult<K, V>>(){}.getType());
        this.registerMessageClass(MessageType.DHT_LOOKUP_RESULT, DHTLookupResultKademliaMessage.class);
        this.registerDataType(MessageType.DHT_STORE, new TypeToken<DHTStoreKademliaMessage.DHTData<BigInteger, NettyConnectionInfo, K, V>>(){}.getType());
        this.registerMessageClass(MessageType.DHT_STORE, DHTStoreKademliaMessage.class);
        this.registerDataType(MessageType.DHT_STORE_RESULT, new TypeToken<DHTStoreResultKademliaMessage.DHTStoreResult<K>>(){}.getType());
        this.registerMessageClass(MessageType.DHT_STORE_RESULT, DHTStoreResultKademliaMessage.class);
        this.registerDataType(MessageType.FIND_NODE_REQ, new TypeToken<BigInteger>(){}.getType());
        this.registerMessageClass(MessageType.FIND_NODE_REQ, FindNodeRequestMessage.class);
        this.registerDataType(MessageType.FIND_NODE_RES, new TypeToken<FindNodeAnswer<BigInteger, NettyConnectionInfo>>(){}.getType());
        this.registerMessageClass(MessageType.FIND_NODE_RES, FindNodeResponseMessage.class);
        this.registerDataType(MessageType.PING, new TypeToken<String>(){}.getType());
        this.registerMessageClass(MessageType.PING, PingKademliaMessage.class);
        this.registerDataType(MessageType.PONG, new TypeToken<String>(){}.getType());
        this.registerMessageClass(MessageType.PONG, PongKademliaMessage.class);
        this.registerDataType(MessageType.SHUTDOWN, new TypeToken<String>(){}.getType());
        this.registerMessageClass(MessageType.SHUTDOWN, ShutdownKademliaMessage.class);
        this.registerDataType(MessageType.EMPTY, new TypeToken<String>(){}.getType());
        this.registerMessageClass(MessageType.EMPTY, EmptyKademliaMessage.class);
    }

    @SneakyThrows
    @Override
    public KademliaMessage<BigInteger, NettyConnectionInfo, Serializable> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String messageType = jsonObject.getAsJsonPrimitive("type").getAsString();
        Node<BigInteger, NettyConnectionInfo> node = jsonDeserializationContext.deserialize(
                jsonObject.getAsJsonObject("node"),
                Node.class
        );
        Class<?> aClass = this.messageClassRegistry.get(messageType);
        @SuppressWarnings("unchecked")
        KademliaMessage<BigInteger, NettyConnectionInfo, Serializable> o = (KademliaMessage<BigInteger, NettyConnectionInfo, Serializable>) aClass.getConstructor().newInstance();
        o.setData(getData(messageType, jsonObject, jsonDeserializationContext));
        o.setType(messageType);
        o.setNode(node);
        o.setAlive(true);
        return o;
    }

    protected <X extends Serializable> X getData(
            String type,
            JsonObject jsonObject,
            JsonDeserializationContext jsonDeserializationContext
    ){
        if (type.equals(MessageType.EMPTY))
            return null;
        Type dataType = typeRegistry.get(type);
        if (dataType != null){
            return jsonDeserializationContext.deserialize(
                    jsonObject.get("data"),
                    dataType
            );
        }
        return null;
    }

    public void registerDataType(String name, Type type){
        this.typeRegistry.put(name, type);
    }

    public void registerMessageClass(String name, Class<?> clazz){
        this.messageClassRegistry.put(name, clazz);
    }
}
