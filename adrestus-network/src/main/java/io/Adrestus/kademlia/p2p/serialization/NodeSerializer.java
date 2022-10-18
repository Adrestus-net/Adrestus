package io.Adrestus.kademlia.p2p.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.Adrestus.kademlia.p2p.common.NettyConnectionInfo;
import io.ep2p.kademlia.node.Node;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.Date;

public class NodeSerializer implements JsonSerializer<Node<BigInteger, NettyConnectionInfo>> {
    @Override
    public JsonElement serialize(Node<BigInteger, NettyConnectionInfo> src, Type type, JsonSerializationContext context) {
        JsonObject jsonNode = new JsonObject();
        jsonNode.addProperty("id", src.getId());
        jsonNode.add("lastSeen", context.serialize(src.getLastSeen(), Date.class));
        jsonNode.add("connectionInfo", context.serialize(src.getConnectionInfo(), NettyConnectionInfo.class));
        return jsonNode;
    }
}
