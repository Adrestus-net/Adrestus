package io.Adrestus.p2p.kademlia.repository;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
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
        kademliaData.setValidatorBlSPublicKey(gson.fromJson(resourceJson.get("validator_bl_s_public_key").getAsJsonObject(), blstoken));
        String address = resourceJson.get("address_data").getAsJsonObject().get("address").getAsString();
        BigInteger ecdsaKey = resourceJson.get("address_data").getAsJsonObject().get("e_c_d_s_a_public_key").getAsBigInteger();
        JsonObject ecdsaSignatureObj = resourceJson.get("address_data").getAsJsonObject().get("e_c_d_s_a_signature").getAsJsonObject();
        KademliaData.ValidatorAddressData validatorAddressData = new KademliaData.ValidatorAddressData(
                address,
                ecdsaKey,
                gson.fromJson(ecdsaSignatureObj, ecdsaSignatureData)
        );

        kademliaData.setAddressData(validatorAddressData);

        String host = resourceJson.get("netty_connection_info").getAsJsonObject().get("host").getAsString();
        int port = resourceJson.get("netty_connection_info").getAsJsonObject().get("port").getAsInt();
        kademliaData.setNettyConnectionInfo(new NettyConnectionInfo(host, port));
        return kademliaData;
    }

}
