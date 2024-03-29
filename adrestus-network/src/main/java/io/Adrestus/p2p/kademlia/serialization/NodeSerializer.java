package io.Adrestus.p2p.kademlia.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.node.Node;

import java.lang.reflect.Type;
import java.math.BigInteger;

public class NodeSerializer implements JsonSerializer<Node<BigInteger, NettyConnectionInfo>> {
    @Override
    public JsonElement serialize(Node<BigInteger, NettyConnectionInfo> src, Type type, JsonSerializationContext context) {
        JsonObject jsonNode = new JsonObject();
        jsonNode.addProperty("id", src.getId());
        jsonNode.add("connectionInfo", context.serialize(src.getConnectionInfo(), NettyConnectionInfo.class));
        return jsonNode;
    }
}
