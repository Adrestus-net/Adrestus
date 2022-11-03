package io.Adrestus.p2p.kademlia.serialization;

import com.google.gson.*;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.common.NettyExternalNode;
import io.Adrestus.p2p.kademlia.node.Node;

import java.lang.reflect.Type;
import java.math.BigInteger;


public class NodeDeserializer implements JsonDeserializer<Node<BigInteger, NettyConnectionInfo>> {
    @Override
    public Node<BigInteger, NettyConnectionInfo> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        NettyExternalNode nettyExternalNode = new NettyExternalNode();
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        nettyExternalNode.setId(jsonDeserializationContext.deserialize(jsonObject.get("id"), BigInteger.class));
        nettyExternalNode.setConnectionInfo(jsonDeserializationContext.deserialize(jsonObject.get("connectionInfo"), NettyConnectionInfo.class));
        return nettyExternalNode;
    }
}
