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
import java.util.List;

public class FindNodeAnswerDeserializer implements JsonDeserializer<FindNodeAnswer<Long, NettyConnectionInfo>> {
    @Override
    public FindNodeAnswer<Long, NettyConnectionInfo> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        FindNodeAnswer<Long, NettyConnectionInfo> findNodeAnswer = new FindNodeAnswer<>();
        findNodeAnswer.setDestinationId(jsonElement.getAsJsonObject().get("destination_id").getAsLong());
        findNodeAnswer.setNodes(jsonDeserializationContext.deserialize(
                jsonElement.getAsJsonObject().get("nodes").getAsJsonArray(),
                new TypeToken<List<ExternalNode<Long, NettyConnectionInfo>>>() {
                }.getType()
        ));
        return findNodeAnswer;
    }
}
