package io.Adrestus.p2p.kademlia.serialization;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.Adrestus.p2p.kademlia.common.NettyBigIntegerExternalNode;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.protocol.message.DHTStoreKademliaMessage;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigInteger;

public class DHTStoreDeserializer<K extends Serializable, V extends Serializable> implements JsonDeserializer<DHTStoreKademliaMessage.DHTData<BigInteger, NettyConnectionInfo, K, V>> {

    @Override
    public DHTStoreKademliaMessage.DHTData<BigInteger, NettyConnectionInfo, K, V> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        DHTStoreKademliaMessage.DHTData<BigInteger, NettyConnectionInfo, K, V> dhtData = new DHTStoreKademliaMessage.DHTData<>();
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        dhtData.setKey(jsonDeserializationContext.deserialize(jsonObject.get("key"), new TypeToken<K>() {
        }.getType()));
        dhtData.setValue(jsonDeserializationContext.deserialize(jsonObject.get("value"), new TypeToken<V>() {
        }.getType()));
        dhtData.setRequester(jsonDeserializationContext.deserialize(jsonObject.getAsJsonObject("requester"), NettyBigIntegerExternalNode.class));
        return dhtData;
    }
}
