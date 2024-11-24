package io.Adrestus.p2p.kademlia.serialization;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.Adrestus.p2p.kademlia.factory.SingletonGsonFactory;
import io.Adrestus.p2p.kademlia.model.LookupAnswer;
import io.Adrestus.p2p.kademlia.protocol.message.DHTLookupResultKademliaMessage;
import io.Adrestus.p2p.kademlia.repository.KademliaData;

import java.io.Serializable;
import java.lang.reflect.Type;

public class DHTLookUpResultDeserializer<K extends Serializable, V extends Serializable> implements JsonDeserializer<DHTLookupResultKademliaMessage.DHTLookupResult<K, V>> {
    private final Class<K> kClass;
    private final Class<V> vClass;

    public DHTLookUpResultDeserializer(Class<K> kClass, Class<V> vClass) {
        this.kClass = kClass;
        this.vClass = vClass;
    }

    @Override
    public DHTLookupResultKademliaMessage.DHTLookupResult<K, V> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        DHTLookupResultKademliaMessage.DHTLookupResult<K, V> dhtLookupResult = new DHTLookupResultKademliaMessage.DHTLookupResult<>();
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        dhtLookupResult.setKey(jsonDeserializationContext.deserialize(jsonObject.get("key"), TypeToken.getParameterized(kClass).getType()));
        try {
            dhtLookupResult.setValue(SingletonGsonFactory.getInstance().gson().fromJson(jsonObject.get("value"),
                    TypeToken.getParameterized(KademliaData.class).getType()));
        } catch (Exception e) {
            dhtLookupResult.setValue(jsonDeserializationContext.deserialize(jsonObject.get("value"), TypeToken.getParameterized(vClass).getType()));
        }
        dhtLookupResult.setResult(LookupAnswer.Result.valueOf(jsonObject.get("result").getAsString()));
        return dhtLookupResult;
    }
}
