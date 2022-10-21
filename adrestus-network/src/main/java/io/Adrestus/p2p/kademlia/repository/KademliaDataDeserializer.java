package io.Adrestus.p2p.kademlia.repository;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import io.Adrestus.crypto.bls.model.BLSPublicKey;

import java.lang.reflect.Type;
import java.util.List;

public class KademliaDataDeserializer implements JsonDeserializer<KademliaData> {
    @Override
    public KademliaData deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        Gson gson = new Gson();
        final Type blstoken = new TypeToken<List<BLSPublicKey>>(){}.getType();
        final JsonObject resourceJson = jsonElement.getAsJsonObject();

        KademliaData kademliaData=new KademliaData();
        kademliaData.setHash(resourceJson.get("Hash").getAsString());
        kademliaData.setValidatorBlSPublicKey(gson.fromJson(resourceJson.get("ValidatorBlSPublicKey"), blstoken));
        return kademliaData;
    }

}
