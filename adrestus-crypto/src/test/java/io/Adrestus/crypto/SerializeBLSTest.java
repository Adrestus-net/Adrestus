package io.Adrestus.crypto;

import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.BLSSignature;
import io.Adrestus.crypto.bls.model.G1Point;
import io.activej.serializer.BinarySerializer;
import io.activej.serializer.SerializerBuilder;
import org.apache.milagro.amcl.BLS381.ECP;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SerializeBLSTest {


    @Test
    public void serialize_g1point(){
        byte[] buffer = new byte[200];
        BLSPrivateKey sk = new BLSPrivateKey(new SecureRandom());
        ECP ecp = ECP.generator().mul(sk.getX().value);
        byte[]buff=new byte[1024];
        ecp.toBytes(buff,true);
        String d= Hex.toHexString(buff);
        G1Point g1Point = new G1Point(ecp);
        //g1Point.setValue(ecp);
        BinarySerializer<G1Point> enc = SerializerBuilder.create().build(G1Point.class);
        enc.encode(buffer, 0,g1Point);
        G1Point  copy =  enc.decode(buffer, 0);
        //assertEquals(g1Point, copy);
    }
    @Test
    public void serialize_keys(){
        byte[] buffer = new byte[200];
        BLSPrivateKey sk = new BLSPrivateKey(new SecureRandom());
        BLSPublicKey vk = new BLSPublicKey(sk);

        BinarySerializer<BLSPublicKey> enc = SerializerBuilder.create().build(BLSPublicKey.class);
        enc.encode(buffer, 0,vk);
        BLSPublicKey  copy =  enc.decode(buffer, 0);
        //assertEquals(vk, copy);
    }
}
