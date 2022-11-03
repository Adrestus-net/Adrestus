package io.Adrestus.p2p.kademlia.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.node.external.ExternalNode;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.Date;

public class ExternalNodeSerializer implements JsonSerializer<ExternalNode<BigInteger, NettyConnectionInfo>> {
    @Override
    public JsonElement serialize(ExternalNode<BigInteger, NettyConnectionInfo> src, Type type, JsonSerializationContext context) {
        JsonObject jsonNode = new JsonObject();
        jsonNode.addProperty("id", src.getId());
        jsonNode.add("lastSeen", context.serialize(src.getLastSeen(), Date.class));
        jsonNode.add("connectionInfo", context.serialize(src.getConnectionInfo(), NettyConnectionInfo.class));
        jsonNode.add("distance", context.serialize(src.getDistance(), BigInteger.class));
        return jsonNode;
    }
}
