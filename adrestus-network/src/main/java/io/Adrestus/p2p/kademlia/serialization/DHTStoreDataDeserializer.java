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

    private final Class<K> kClass;
    private final Class<V> vClass;

    public DHTStoreDataDeserializer(Class<K> kClass, Class<V> vClass) {
        this.kClass = kClass;
        this.vClass = vClass;
    }

    @Override
    public DHTStoreKademliaMessage.DHTData<BigInteger, NettyConnectionInfo, K, V> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        DHTStoreKademliaMessage.DHTData<BigInteger, NettyConnectionInfo, K, V> dhtData = new DHTStoreKademliaMessage.DHTData<>();
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        dhtData.setKey(jsonDeserializationContext.deserialize(jsonObject.get("key"), TypeToken.getParameterized(kClass).getType()));
        try {
            dhtData.setValue(SingletonGsonFactory.getInstance().gson().fromJson(jsonObject.get("value"), TypeToken.getParameterized(KademliaData.class).getType()));
        } catch (Exception e) {
            dhtData.setValue(jsonDeserializationContext.deserialize(jsonObject.get("value"), TypeToken.getParameterized(vClass).getType()));
        }
        dhtData.setRequester(jsonDeserializationContext.deserialize(jsonObject.getAsJsonObject("requester"), Node.class));
        return dhtData;
    }
}
