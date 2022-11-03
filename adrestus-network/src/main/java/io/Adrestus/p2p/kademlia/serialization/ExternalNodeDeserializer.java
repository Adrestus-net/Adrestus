package io.Adrestus.p2p.kademlia.serialization;

import com.google.gson.*;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.node.Node;
import io.Adrestus.p2p.kademlia.node.external.BigIntegerExternalNode;
import io.Adrestus.p2p.kademlia.node.external.ExternalNode;

import java.lang.reflect.Type;
import java.math.BigInteger;


public class ExternalNodeDeserializer implements JsonDeserializer<ExternalNode<BigInteger, NettyConnectionInfo>> {
    @Override
    public ExternalNode<BigInteger, NettyConnectionInfo> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        BigInteger distance = jsonObject.get("distance").getAsBigInteger();
        jsonObject.remove("distance");
        Node<BigInteger, NettyConnectionInfo> node = null;
        if (jsonObject.has("node")){
            node = jsonDeserializationContext.deserialize(
                    jsonObject.get("node"),
                    Node.class
            );
        }else {
            node = jsonDeserializationContext.deserialize(
                    jsonObject,
                    Node.class
            );
        }
        return new BigIntegerExternalNode<>(node, distance);
    }
}
