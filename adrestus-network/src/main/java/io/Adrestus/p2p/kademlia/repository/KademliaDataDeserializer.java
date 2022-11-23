package io.Adrestus.p2p.kademlia.repository;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import io.Adrestus.crypto.SecurityAuditProofs;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.elliptic.SignatureData;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;

import java.lang.reflect.Type;
import java.math.BigInteger;

public class KademliaDataDeserializer implements JsonDeserializer<KademliaData> {
    @Override
    public KademliaData deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        Gson gson = new Gson();
        final Type blstoken = new TypeToken<BLSPublicKey>() {
        }.getType();
        final Type ecdsaSignatureData = new TypeToken<SignatureData>() {
        }.getType();
        final JsonObject resourceJson = jsonElement.getAsJsonObject();

        KademliaData kademliaData = new KademliaData();
        kademliaData.setHash(resourceJson.get("hash").getAsString());
        JsonObject blsobj = resourceJson.get("address_data").getAsJsonObject().get("validator_bl_s_public_key").getAsJsonObject();
        String address = resourceJson.get("address_data").getAsJsonObject().get("address").getAsString();
        String IP = resourceJson.get("address_data").getAsJsonObject().get("ip").getAsString();
        BigInteger ecdsaKey = resourceJson.get("address_data").getAsJsonObject().get("e_c_d_s_a_public_key").getAsBigInteger();
        JsonObject ecdsaSignatureObj = resourceJson.get("address_data").getAsJsonObject().get("e_c_d_s_a_signature").getAsJsonObject();
        SecurityAuditProofs securityAuditProofs = new SecurityAuditProofs(
                gson.fromJson(blsobj, blstoken),
                address,
                ecdsaKey,
                gson.fromJson(ecdsaSignatureObj, ecdsaSignatureData)
        );

        kademliaData.setAddressData(securityAuditProofs);
        kademliaData.getAddressData().setIp(IP);

        String host = resourceJson.get("netty_connection_info").getAsJsonObject().get("host").getAsString();
        int port = resourceJson.get("netty_connection_info").getAsJsonObject().get("port").getAsInt();
        kademliaData.setNettyConnectionInfo(new NettyConnectionInfo(host, port));
        return kademliaData;
    }

}
