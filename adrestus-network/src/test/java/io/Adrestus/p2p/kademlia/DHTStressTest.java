package io.Adrestus.p2p.kademlia;

import com.google.common.net.InetAddresses;
import com.google.gson.Gson;
import io.Adrestus.config.KademliaConfiguration;
import io.Adrestus.config.NodeSettings;
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
import io.Adrestus.p2p.kademlia.client.OkHttpMessageSender;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.exception.DuplicateStoreRequest;
import io.Adrestus.p2p.kademlia.exception.UnsupportedBoundingException;
import io.Adrestus.p2p.kademlia.model.FindNodeAnswer;
import io.Adrestus.p2p.kademlia.model.StoreAnswer;
import io.Adrestus.p2p.kademlia.node.DHTBootstrapNode;
import io.Adrestus.p2p.kademlia.node.DHTRegularNode;
import io.Adrestus.p2p.kademlia.node.KeyHashGenerator;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.p2p.kademlia.util.BoundedHashUtil;
import org.apache.commons.codec.binary.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DHTStressTest {
    private static int version = 0x00;
    private static KademliaData kademliaData;
    private static BLSPublicKey vk;
    private static KademliaData seridata;
    private static OkHttpMessageSender<String, String> nettyMessageSender1,nettyMessageSender3,nettyMessageSender4,nettyMessageSender5;
    private static OkHttpMessageSender<String, String> nettyMessageSender2;
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
        vk = new BLSPublicKey(sk);
        BLSPublicKey copy = BLSPublicKey.fromByte(Hex.decode(KademliaConfiguration.BLSPublicKeyHex));
        assertEquals(copy, vk);


        kademliaData = new KademliaData(new KademliaData.ValidatorAddressData(adddress, ecKeyPair.getPublicKey(), signatureData));
        kademliaData.setValidatorBlSPublicKey(vk);
        Gson gson = new Gson();
        String jsonString = gson.toJson(kademliaData);
        KademliaData copydata = gson.fromJson(jsonString, KademliaData.class);
        assertEquals(kademliaData, copydata);

        kademliaData.setHash(jsonString);
        Signature bls_sig = BLSSignature.sign(StringUtils.getBytesUtf8(kademliaData.getHash()), sk);

        kademliaData.getBootstrapNodeProofs().setBlsPublicKey(vk);
        kademliaData.getBootstrapNodeProofs().setSignature(bls_sig);


        String jsonString2 = gson.toJson(kademliaData);
        seridata = gson.fromJson(jsonString2, KademliaData.class);
        KademliaData clonebale = (KademliaData) seridata.clone();
        assertEquals(seridata, clonebale);

        clonebale.setHash("");

        clonebale.setBootstrapNodeProofs((KademliaData.BootstrapNodeProofs) clonebale.getBootstrapNodeProofs().clone());
        clonebale.getBootstrapNodeProofs().InitEmpty();


        //checks
        String clonedhash = gson.toJson(clonebale);
        assertEquals(seridata.getHash(), clonedhash);
        assertEquals(bls_sig, seridata.getBootstrapNodeProofs().getSignature());
        boolean verify = BLSSignature.verify(seridata.getBootstrapNodeProofs().getSignature(), StringUtils.getBytesUtf8(clonedhash), BLSPublicKey.fromByte(Hex.decode(KademliaConfiguration.BLSPublicKeyHex)));
        boolean verify2 = ecdsaSign.secp256Verify(HashUtil.sha256(StringUtils.getBytesUtf8(seridata.getAddressData().getAddress())), seridata.getAddressData().getAddress(), seridata.getAddressData().getECDSASignature());
        assertEquals(true, verify);
        assertEquals(true, verify2);
        System.out.println("done");
    }

    @Test
    public void test() throws DuplicateStoreRequest, ExecutionException, InterruptedException, TimeoutException {
        Random random = new Random();
        int count = 2;
        KeyHashGenerator<BigInteger, String> keyHashGenerator = key -> {
            try {
                return new BoundedHashUtil(NodeSettings.getInstance().getIdentifierSize()).hash(key.hashCode(), BigInteger.class);
            } catch (UnsupportedBoundingException e) {
                return null;
            }
        };
        while (count > 0) {
            String ipString1 = InetAddresses.fromInteger(random.nextInt()).getHostAddress();
            String ipString2 = InetAddresses.fromInteger(random.nextInt()).getHostAddress();
            String ipString3 = InetAddresses.fromInteger(random.nextInt()).getHostAddress();
            String ipString4 = InetAddresses.fromInteger(random.nextInt()).getHostAddress();
            BigInteger id1 = new BigInteger(HashUtil.convertIPtoHex(ipString1, 16));
            BigInteger id2 = new BigInteger(HashUtil.convertIPtoHex(ipString2, 16));
            BigInteger id3 = new BigInteger(HashUtil.convertIPtoHex(ipString3, 16));
            BigInteger id4 = new BigInteger(HashUtil.convertIPtoHex(ipString4, 16));


            nettyMessageSender1 = new OkHttpMessageSender<>();
            nettyMessageSender2 = new OkHttpMessageSender<>();
            nettyMessageSender3 = new OkHttpMessageSender<>();
            nettyMessageSender4 = new OkHttpMessageSender<>();
            nettyMessageSender5 = new OkHttpMessageSender<>();

            DHTBootstrapNode dhtBootstrapNode = new DHTBootstrapNode(
                    new NettyConnectionInfo(KademliaConfiguration.BootstrapNodeIP, KademliaConfiguration.BootstrapNodePORT), id1,keyHashGenerator);
            dhtBootstrapNode.start();

            DHTRegularNode regularNode = new DHTRegularNode(
                    new NettyConnectionInfo(KademliaConfiguration.BootstrapNodeIP, KademliaConfiguration.PORT), id2,keyHashGenerator);
            regularNode.start(dhtBootstrapNode);

            DHTRegularNode regularNode2 = new DHTRegularNode(
                    new NettyConnectionInfo(KademliaConfiguration.BootstrapNodeIP, KademliaConfiguration.PORT+1), id3,keyHashGenerator);
            regularNode2.start(dhtBootstrapNode);

            DHTRegularNode regularNode3 = new DHTRegularNode(
                    new NettyConnectionInfo(KademliaConfiguration.BootstrapNodeIP, KademliaConfiguration.PORT+2), id4,keyHashGenerator);
            regularNode3.start(dhtBootstrapNode);

            StoreAnswer<BigInteger, String> storeAnswer = regularNode.getRegular_node().store("V", seridata).get(5, TimeUnit.SECONDS);
            StoreAnswer<BigInteger, String> storeAnswer2 = regularNode2.getRegular_node().store("F", seridata).get(5, TimeUnit.SECONDS);
            StoreAnswer<BigInteger, String> storeAnswer3 = regularNode3.getRegular_node().store("G", seridata).get(5, TimeUnit.SECONDS);
            Thread.sleep(3000);
            KademliaData cp = regularNode.getRegular_node().lookup("V").get(5, TimeUnit.SECONDS).getValue();
            KademliaData cp2 = regularNode2.getRegular_node().lookup("V").get(5, TimeUnit.SECONDS).getValue();
            KademliaData cp3 = regularNode3.getRegular_node().lookup("V").get(5, TimeUnit.SECONDS).getValue();
            KademliaData cp4 = dhtBootstrapNode.getBootStrapNode().lookup("V").get(5, TimeUnit.SECONDS).getValue();
            assertEquals(seridata, cp);
            assertEquals(seridata, cp2);
            assertEquals(seridata, cp3);
            assertEquals(seridata, cp4);

            FindNodeAnswer<BigInteger,NettyConnectionInfo> sa=regularNode2.getRegular_node().getRoutingTable().findClosest(id3);
            System.out.println("Done:"+count);
            nettyMessageSender1.stop();
            nettyMessageSender2.stop();
            nettyMessageSender3.stop();
            nettyMessageSender4.stop();
            nettyMessageSender5.stop();
            regularNode.getRegular_node().stop();
            regularNode2.getRegular_node().stop();
            regularNode3.getRegular_node().stop();
            dhtBootstrapNode.getBootStrapNode().stop();
            count--;
        }
    }
}
