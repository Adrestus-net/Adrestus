package io.Adrestus.kademlia.p2p.serialization;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.Adrestus.kademlia.p2p.common.NettyBigIntegerExternalNode;
import io.Adrestus.kademlia.p2p.common.NettyConnectionInfo;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.node.external.BigIntegerExternalNode;
import io.ep2p.kademlia.node.external.ExternalNode;

import java.lang.reflect.Type;
import java.math.BigInteger;

public class ExternalNodeDeserializer implements JsonDeserializer<ExternalNode<BigInteger, NettyConnectionInfo>> {
    @Override
    public ExternalNode<BigInteger, NettyConnectionInfo> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        Node<BigInteger, NettyConnectionInfo> node = jsonDeserializationContext.deserialize(
                jsonObject.get("node"),
                new TypeToken<NettyBigIntegerExternalNode>(){}.getType()
        );
        BigInteger distance = jsonObject.get("distance").getAsBigInteger();
        return new BigIntegerExternalNode<>(node, distance);
    }
}
