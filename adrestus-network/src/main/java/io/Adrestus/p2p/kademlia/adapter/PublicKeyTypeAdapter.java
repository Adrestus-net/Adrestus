package io.Adrestus.p2p.kademlia.adapter;

import com.google.gson.*;
import io.Adrestus.config.AdrestusConfiguration;
import org.spongycastle.util.encoders.Hex;

import java.lang.reflect.Type;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

public class PublicKeyTypeAdapter implements JsonSerializer<PublicKey>, JsonDeserializer<PublicKey> {

    @Override
    public JsonElement serialize(PublicKey src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(Hex.toHexString(src.getEncoded()));
    }

    @Override
    public PublicKey deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            byte[] pubKeyBytes = Hex.decode(json.getAsString());
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(pubKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(AdrestusConfiguration.SIGN_ALGORITHM, AdrestusConfiguration.SIGN_PROVIDER);
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            return publicKey;
        } catch (Exception e) {
            e.printStackTrace();
            throw new JsonParseException(e);
        }
    }
}
