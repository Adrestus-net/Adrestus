package io.Adrestus.p2p.kademlia.serialization;

import com.google.gson.*;
import io.Adrestus.p2p.kademlia.common.NettyBigIntegerExternalNode;

import java.lang.reflect.Type;

public class NodeDeserializer implements JsonDeserializer<NettyBigIntegerExternalNode> {
    @Override
    public NettyBigIntegerExternalNode deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return new Gson().fromJson(jsonElement, NettyBigIntegerExternalNode.class);
    }
}
