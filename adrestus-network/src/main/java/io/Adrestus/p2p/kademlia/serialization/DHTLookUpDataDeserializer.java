package io.Adrestus.p2p.kademlia.serialization;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.node.Node;
import io.Adrestus.p2p.kademlia.protocol.message.DHTLookupKademliaMessage;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigInteger;


public class DHTLookUpDataDeserializer<K extends Serializable> implements JsonDeserializer<DHTLookupKademliaMessage.DHTLookup<BigInteger, NettyConnectionInfo, K>> {

    private final Class<K> kClass;

    public DHTLookUpDataDeserializer(Class<K> kClass) {
        this.kClass = kClass;
    }

    @Override
    public DHTLookupKademliaMessage.DHTLookup<BigInteger, NettyConnectionInfo, K> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        DHTLookupKademliaMessage.DHTLookup<BigInteger, NettyConnectionInfo, K> dhtLookup = new DHTLookupKademliaMessage.DHTLookup<>();
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonObject requesterJsonObject;
        if (jsonObject.getAsJsonObject("requester") == null) {
            jsonObject = jsonObject.getAsJsonObject("data");
            requesterJsonObject = jsonObject.getAsJsonObject("requester");
        } else {
            requesterJsonObject = jsonObject.getAsJsonObject("requester");
        }
        dhtLookup.setRequester(jsonDeserializationContext.deserialize(requesterJsonObject, Node.class));
        dhtLookup.setCurrentTry(jsonObject.get("current_try").getAsInt());
        dhtLookup.setKey(jsonDeserializationContext.deserialize(jsonObject.get("key"), TypeToken.getParameterized(kClass).getType()));
        return dhtLookup;
    }
}
