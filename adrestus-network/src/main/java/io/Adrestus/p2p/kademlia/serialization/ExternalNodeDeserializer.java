package io.Adrestus.p2p.kademlia.serialization;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.Adrestus.p2p.kademlia.common.NettyExternalNode;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.node.Node;
import io.Adrestus.p2p.kademlia.node.external.LongExternalNode;
import io.Adrestus.p2p.kademlia.node.external.ExternalNode;

import java.lang.reflect.Type;

public class ExternalNodeDeserializer implements JsonDeserializer<ExternalNode<Long, NettyConnectionInfo>> {
    @Override
    public ExternalNode<Long, NettyConnectionInfo> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        Node<Long, NettyConnectionInfo> node = jsonDeserializationContext.deserialize(
                jsonObject.get("node"),
                new TypeToken<NettyExternalNode>() {
                }.getType()
        );
        Long distance = jsonObject.get("distance").getAsLong();
        return new LongExternalNode<>(node, distance);
    }
}
