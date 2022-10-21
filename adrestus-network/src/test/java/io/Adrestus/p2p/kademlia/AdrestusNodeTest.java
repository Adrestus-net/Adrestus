package io.Adrestus.p2p.kademlia;

import com.google.gson.Gson;
import io.Adrestus.config.KademliaConfiguration;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.WalletAddress;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.BLSSignature;
import io.Adrestus.crypto.bls.model.Signature;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import io.Adrestus.crypto.elliptic.SignatureData;
import io.Adrestus.p2p.kademlia.builder.NettyKademliaDHTNodeBuilder;
import io.Adrestus.p2p.kademlia.client.NettyMessageSender;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.protocol.MessageType;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import org.apache.commons.codec.binary.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AdrestusNodeTest {
    private static int version = 0x00;
    private static KademliaData kademliaData;
    private static NettyMessageSender<String, String> nettyMessageSender1;

    private static NettyKademliaDHTNode<String, String> node1;
    private static NettyKademliaDHTNode<String, String> node2;
    @BeforeAll
    public static void setup() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, CloneNotSupportedException {
        String mnemonic_code = "fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2";
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        random.setSeed(Hex.decode(mnemonic_code));

        ECKeyPair ecKeyPair = Keys.createEcKeyPair(random);
        String adddress = WalletAddress.generate_address((byte) version, ecKeyPair.getPublicKey());
        ECDSASign ecdsaSign = new ECDSASign();


        SignatureData signatureData = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress)), ecKeyPair);


        BLSPrivateKey sk = new BLSPrivateKey(42);
        BLSPublicKey vk = new BLSPublicKey(sk);
        BLSPublicKey copy=BLSPublicKey.fromByte(Hex.decode(KademliaConfiguration.BLSPublicKeyHex));
        assertEquals(copy, vk);


        kademliaData=new KademliaData(new KademliaData.ValidatorAddressData(adddress,ecKeyPair.getPublicKey(),signatureData));
        kademliaData.setValidatorBlSPublicKey(vk);
        Gson gson = new Gson();
        String jsonString = gson.toJson(kademliaData);
        KademliaData copydata=gson.fromJson(jsonString,KademliaData.class);
        assertEquals(kademliaData,copydata);

        kademliaData.setHash(jsonString);
        Signature bls_sig = BLSSignature.sign(StringUtils.getBytesUtf8(kademliaData.getHash()), sk);

        kademliaData.getBootstrapNodeProofs().setBlsPublicKey(vk);
        kademliaData.getBootstrapNodeProofs().setSignature(bls_sig);


        String jsonString2 = gson.toJson(kademliaData);
        KademliaData seridata=gson.fromJson(jsonString2,KademliaData.class);
        KademliaData clonebale= (KademliaData) seridata.clone();
        assertEquals(seridata,clonebale);

        clonebale.setHash("");

        clonebale.setBootstrapNodeProofs((KademliaData.BootstrapNodeProofs) clonebale.getBootstrapNodeProofs().clone());
        clonebale.getBootstrapNodeProofs().InitEmpty();


        //checks
        String clonedhash= gson.toJson(clonebale);
        assertEquals(seridata.getHash(),clonedhash);
        assertEquals(bls_sig,seridata.getBootstrapNodeProofs().getSignature());
        boolean verify=BLSSignature.verify(seridata.getBootstrapNodeProofs().getSignature(),StringUtils.getBytesUtf8(clonedhash),BLSPublicKey.fromByte(Hex.decode(KademliaConfiguration.BLSPublicKeyHex)));
        boolean verify2 = ecdsaSign.secp256Verify(HashUtil.sha256(StringUtils.getBytesUtf8(seridata.getAddressData().getAddress())), seridata.getAddressData().getAddress(), seridata.getAddressData().getECDSASignature());
        assertEquals(true,verify);
        assertEquals(true,verify2);
        System.out.println("done");
    }
    @Test
    public void test_1(){
      /*  nettyMessageSender1 = new NettyMessageSender<>();

        node1 = new NettyKademliaDHTNodeBuilder<>(
                BigInteger.valueOf(1),
                new NettyConnectionInfo("127.0.0.1", 8081),
                new NodeTest.SampleRepository(),
                keyHashGenerator
        ).withNodeSettings(settings).build();
        node1.registerMessageHandler(MessageType.PONG,handler);
        node1.start();


        // node 2
        node2 = new NettyKademliaDHTNodeBuilder<>(
                BigInteger.valueOf(2),
                new NettyConnectionInfo("127.0.0.1", 8082),
                new NodeTest.SampleRepository(),
                keyHashGenerator
        ).withNodeSettings(settings).build();*/

    }
}
