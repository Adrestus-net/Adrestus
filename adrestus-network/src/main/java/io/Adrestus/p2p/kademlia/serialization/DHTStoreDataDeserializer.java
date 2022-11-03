package io.Adrestus.p2p.kademlia.serialization;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.factory.SingletonGsonFactory;
import io.Adrestus.p2p.kademlia.node.Node;
import io.Adrestus.p2p.kademlia.protocol.message.DHTStoreKademliaMessage;
import io.Adrestus.p2p.kademlia.repository.KademliaData;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigInteger;


public class DHTStoreDataDeserializer<K extends Serializable, V extends Serializable> implements JsonDeserializer<DHTStoreKademliaMessage.DHTData<BigInteger, NettyConnectionInfo, K, V>> {

    @Override
    public DHTStoreKademliaMessage.DHTData<BigInteger, NettyConnectionInfo, K, V> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        DHTStoreKademliaMessage.DHTData<BigInteger, NettyConnectionInfo, K, V> dhtData = new DHTStoreKademliaMessage.DHTData<>();
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        dhtData.setKey(jsonDeserializationContext.deserialize(jsonObject.get("key"), new TypeToken<K>() {}.getType()));
        try {
            dhtData.setValue(SingletonGsonFactory.getInstance().gson().fromJson(jsonObject.get("value"), new TypeToken<KademliaData>() {
            }.getType()));
        } catch (Exception e) {
            dhtData.setValue(jsonDeserializationContext.deserialize(jsonObject.get("value"), new TypeToken<V>() {
            }.getType()));
        }
        dhtData.setRequester(jsonDeserializationContext.deserialize(jsonObject.getAsJsonObject("requester"), Node.class));
        return dhtData;
    }
}
