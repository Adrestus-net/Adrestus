package io.Adrestus.p2p.kademlia.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.model.FindNodeAnswer;
import io.Adrestus.p2p.kademlia.node.external.ExternalNode;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.List;

public class FindNodeAnswerDeserializer implements JsonDeserializer<FindNodeAnswer<BigInteger, NettyConnectionInfo>> {
    @Override
    public FindNodeAnswer<BigInteger, NettyConnectionInfo> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        FindNodeAnswer<BigInteger, NettyConnectionInfo> findNodeAnswer = new FindNodeAnswer<>();
        findNodeAnswer.setDestinationId(jsonElement.getAsJsonObject().get("destination_id").getAsBigInteger());
        findNodeAnswer.setNodes(jsonDeserializationContext.deserialize(
                jsonElement.getAsJsonObject().get("nodes").getAsJsonArray(),
                TypeToken.getParameterized(List.class, TypeToken.getParameterized(ExternalNode.class, BigInteger.class, NettyConnectionInfo.class).getType()).getType()));
        return findNodeAnswer;
    }
}
