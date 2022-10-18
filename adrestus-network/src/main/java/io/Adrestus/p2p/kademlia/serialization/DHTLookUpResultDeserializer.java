package io.Adrestus.p2p.kademlia.serialization;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.ep2p.kademlia.model.LookupAnswer;
import io.ep2p.kademlia.protocol.message.DHTLookupResultKademliaMessage;

import java.io.Serializable;
import java.lang.reflect.Type;

public class DHTLookUpResultDeserializer<K extends Serializable, V extends Serializable> implements JsonDeserializer<DHTLookupResultKademliaMessage.DHTLookupResult<K, V>> {

    @Override
    public DHTLookupResultKademliaMessage.DHTLookupResult<K, V> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        DHTLookupResultKademliaMessage.DHTLookupResult<K, V> dhtLookupResult = new DHTLookupResultKademliaMessage.DHTLookupResult<>();
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        dhtLookupResult.setKey(jsonDeserializationContext.deserialize(jsonObject.get("key"), new TypeToken<K>() {
        }.getType()));
        dhtLookupResult.setValue(jsonDeserializationContext.deserialize(jsonObject.get("value"), new TypeToken<V>() {
        }.getType()));
        dhtLookupResult.setResult(LookupAnswer.Result.valueOf(jsonObject.get("result").getAsString()));
        return dhtLookupResult;
    }
}
