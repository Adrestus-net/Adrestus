package io.Adrestus.p2p.kademlia.serialization;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.Adrestus.p2p.kademlia.model.StoreAnswer;
import io.Adrestus.p2p.kademlia.protocol.message.DHTStoreResultKademliaMessage;

import java.io.Serializable;
import java.lang.reflect.Type;

public class DHTStoreResultDataDataDeserializer<K extends Serializable> implements JsonDeserializer<DHTStoreResultKademliaMessage.DHTStoreResult<K>> {

    private final Class<K> kClass;

    public DHTStoreResultDataDataDeserializer(Class<K> kClass) {
        this.kClass = kClass;
    }

    @Override
    public DHTStoreResultKademliaMessage.DHTStoreResult<K> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        DHTStoreResultKademliaMessage.DHTStoreResult<K> dhtStoreResult = new DHTStoreResultKademliaMessage.DHTStoreResult<>();
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        dhtStoreResult.setKey(jsonDeserializationContext.deserialize(jsonObject.get("key"), TypeToken.getParameterized(kClass).getType()));
        dhtStoreResult.setResult(StoreAnswer.Result.valueOf(jsonObject.get("result").getAsString()));
        return dhtStoreResult;
    }
}
