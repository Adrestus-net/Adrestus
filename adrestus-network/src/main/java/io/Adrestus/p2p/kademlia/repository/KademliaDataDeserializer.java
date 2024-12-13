package io.Adrestus.p2p.kademlia.repository;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.Adrestus.crypto.SecurityAuditProofs;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.elliptic.ECDSASignatureData;
import io.Adrestus.p2p.kademlia.adapter.PublicKeyTypeAdapter;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;

import java.lang.reflect.Type;
import java.security.PublicKey;

public class KademliaDataDeserializer implements JsonDeserializer<KademliaData> {
    private final Gson gson;

    public KademliaDataDeserializer() {
        gson = new GsonBuilder()
                .registerTypeAdapter(PublicKey.class, new PublicKeyTypeAdapter())
                .create();
    }

    @Override
    public KademliaData deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        final Type blstoken = TypeToken.getParameterized(BLSPublicKey.class).getType();
        final Type pubkey = TypeToken.getParameterized(PublicKey.class).getType();
        final Type ecdsaSignatureData = TypeToken.getParameterized(ECDSASignatureData.class).getType();
        final JsonObject resourceJson = jsonElement.getAsJsonObject();

        KademliaData kademliaData = new KademliaData();
        kademliaData.setHash(resourceJson.get("hash").getAsString());
        JsonObject blsobj = resourceJson.get("address_data").getAsJsonObject().get("validator_bl_s_public_key").getAsJsonObject();
        String address = resourceJson.get("address_data").getAsJsonObject().get("address").getAsString();
        String ecdsaKey = resourceJson.get("address_data").getAsJsonObject().get("e_c_d_s_a_public_key").getAsString();
        JsonObject ecdsaSignatureObj = resourceJson.get("address_data").getAsJsonObject().get("e_c_d_s_a_signature").getAsJsonObject();
        SecurityAuditProofs securityAuditProofs = new SecurityAuditProofs(
                gson.fromJson(blsobj, blstoken),
                address,
                gson.fromJson(ecdsaKey, pubkey),
                gson.fromJson(ecdsaSignatureObj, ecdsaSignatureData)
        );

        kademliaData.setAddressData(securityAuditProofs);

        String host = resourceJson.get("netty_connection_info").getAsJsonObject().get("host").getAsString();
        int port = resourceJson.get("netty_connection_info").getAsJsonObject().get("port").getAsInt();
        kademliaData.setNettyConnectionInfo(new NettyConnectionInfo(host, port));
        return kademliaData;
    }

}
