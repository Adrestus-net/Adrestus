package io.Adrestus.p2p.kademlia.repository;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.Signature;
import io.Adrestus.crypto.elliptic.SignatureData;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.List;

public class KademliaDataDeserializer implements JsonDeserializer<KademliaData> {
    @Override
    public KademliaData deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        Gson gson = new Gson();
        final Type blstoken = new TypeToken<BLSPublicKey>(){}.getType();
        final Type ecdsaSignatureData = new TypeToken<SignatureData>(){}.getType();
        final Type signature = new TypeToken<Signature>(){}.getType();
        final Type validatoraddressdata = new TypeToken<KademliaData.ValidatorAddressData>(){}.getType();
        final JsonObject resourceJson = jsonElement.getAsJsonObject();

        KademliaData kademliaData=new KademliaData();
        kademliaData.setHash(resourceJson.get("hash").getAsString());
        kademliaData.setValidatorBlSPublicKey(gson.fromJson(resourceJson.get("validator_bl_s_public_key").getAsJsonObject(), blstoken));

        JsonObject bls_public_key_Obj=resourceJson.get("bootstrap_node_proofs").getAsJsonObject().get("bls_public_key").getAsJsonObject();
        JsonObject signature_Obj=resourceJson.get("bootstrap_node_proofs").getAsJsonObject().get("signature").getAsJsonObject();
        KademliaData.BootstrapNodeProofs bootstrapNodeProofs=
                new KademliaData.BootstrapNodeProofs(
                        gson.fromJson(bls_public_key_Obj,blstoken),
                        gson.fromJson(signature_Obj,signature));
        kademliaData.setBootstrapNodeProofs(bootstrapNodeProofs);

        String address=resourceJson.get("address_data").getAsJsonObject().get("address").getAsString();
        BigInteger ecdsaKey=resourceJson.get("address_data").getAsJsonObject().get("e_c_d_s_a_public_key").getAsBigInteger();
        JsonObject ecdsaSignatureObj=resourceJson.get("address_data").getAsJsonObject().get("e_c_d_s_a_signature").getAsJsonObject();
        KademliaData.ValidatorAddressData validatorAddressData=new KademliaData.ValidatorAddressData(
                address,
                ecdsaKey,
                gson.fromJson(ecdsaSignatureObj, ecdsaSignatureData)
        );

        kademliaData.setAddressData(validatorAddressData);
        return kademliaData;
    }

}
