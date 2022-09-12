package io.Adrestus.consensus;

import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.BLSSignature;
import io.Adrestus.crypto.bls.model.Signature;
import io.Adrestus.util.SerializationUtil;
import io.activej.serializer.BinarySerializer;
import io.activej.serializer.SerializerBuilder;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SerializeBLSTest {


    @Test
    public void serialize_g1point() {
        BLSPrivateKey sk = new BLSPrivateKey(new SecureRandom());
        BLSPublicKey vk = new BLSPublicKey(sk);
        List<SerializationUtil.Mapping> list=new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class,ctx->new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx->new ECP2mapper()));
        SerializationUtil<BLSPublicKey> ser = new SerializationUtil<BLSPublicKey>(BLSPublicKey.class,list);
        byte []data=ser.encode(vk);
        BLSPublicKey copy=ser.decode(data);
        System.out.println(copy.toString());
        byte[] msg = "Test_Message".getBytes();
        Signature bls_sig = BLSSignature.sign(msg, sk);
        assertEquals(true, BLSSignature.verify(bls_sig, msg, copy));
    }


    @Test
    public void serialize_g2point() {
        BLSPrivateKey sk = new BLSPrivateKey(new SecureRandom());
        BLSPublicKey vk = new BLSPublicKey(sk);
        List<SerializationUtil.Mapping> list=new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class,ctx->new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx->new ECP2mapper()));

        SerializationUtil<Signature> ser = new SerializationUtil<Signature>(Signature.class,list);
        byte[] msg = "Test_Message".getBytes();
        Signature bls_sig = BLSSignature.sign(msg, sk);

        byte []data=ser.encode(bls_sig);
        Signature copy=ser.decode(data);

        assertEquals(true, BLSSignature.verify(copy, msg, vk));
    }

}
