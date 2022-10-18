package io.Adrestus.kademlia.p2p.serialization;

import com.google.gson.*;
import io.Adrestus.kademlia.p2p.common.NettyBigIntegerExternalNode;

import java.lang.reflect.Type;

public class NodeDeserializer implements JsonDeserializer<NettyBigIntegerExternalNode> {
    @Override
    public NettyBigIntegerExternalNode deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return new Gson().fromJson(jsonElement, NettyBigIntegerExternalNode.class);
    }
}
