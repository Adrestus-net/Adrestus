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
import io.Adrestus.p2p.kademlia.exception.DuplicateStoreRequest;
import io.Adrestus.p2p.kademlia.model.LookupAnswer;
import io.Adrestus.p2p.kademlia.node.DHTBootstrapNode;
import io.Adrestus.p2p.kademlia.node.DHTRegularNode;
import io.Adrestus.p2p.kademlia.node.KademliaNodeAPI;
import io.Adrestus.p2p.kademlia.node.KeyHashGenerator;
import io.Adrestus.p2p.kademlia.protocol.MessageType;
import io.Adrestus.p2p.kademlia.protocol.handler.MessageHandler;
import io.Adrestus.p2p.kademlia.protocol.handler.PongMessageHandler;
import io.Adrestus.p2p.kademlia.protocol.message.KademliaMessage;
import io.Adrestus.p2p.kademlia.protocol.message.PongKademliaMessage;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.p2p.kademlia.repository.KademliaRepository;
import io.Adrestus.p2p.kademlia.util.BoundedHashUtil;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AdrestusNodeTest {
    private static int version = 0x00;
    private static KademliaData kademliaData;
    private static NettyMessageSender<String, String> nettyMessageSender1;
    private static BLSPublicKey vk;
    private static NettyKademliaDHTNode<String, KademliaData> node1;
    private static NettyKademliaDHTNode<String, KademliaData> node2;
    private static KademliaData seridata;
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
        seridata=gson.fromJson(jsonString2,KademliaData.class);
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
    public void test_1() throws ExecutionException, InterruptedException, TimeoutException, DuplicateStoreRequest {
        /*DHTBootstrapNode bootstrapNode=new DHTBootstrapNode(new NettyConnectionInfo(KademliaConfiguration.BootstrapNodeIP,KademliaConfiguration.BootstrapNodePORT));
        bootstrapNode.start();

        DHTRegularNode regularNode=new DHTRegularNode(new NettyConnectionInfo("localhost",KademliaConfiguration.PORT));
        regularNode.start();
        regularNode.getRegular_node().store("localhost",new KademliaData("hash"));

        while (true){
            Thread.sleep(1000);
            // System.out.println(bootstrapNode.getBootStrapNode().lookup("localhost").get().getValue().toString());
        }*/

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("io.netty");
        rootLogger.setLevel(ch.qos.logback.classic.Level.OFF);
        NodeSettings.Default.IDENTIFIER_SIZE = 4;
        NodeSettings.Default.BUCKET_SIZE = 100;
        NodeSettings.Default.PING_SCHEDULE_TIME_VALUE = 1;
        NodeSettings.Default.MAXIMUM_STORE_AND_LOOKUP_TIMEOUT_VALUE=1;
        NodeSettings.Default.MAXIMUM_STORE_AND_LOOKUP_TIMEOUT_TIME_UNIT=TimeUnit.SECONDS;
        NodeSettings.Default.ENABLED_FIRST_STORE_REQUEST_FORCE_PASS=true;
        NodeSettings.Default.PING_SCHEDULE_TIME_UNIT= TimeUnit.SECONDS;
        NodeSettings.Default.PING_SCHEDULE_TIME_VALUE=2;
        NodeSettings settings=NodeSettings.Default.build();
        KeyHashGenerator<BigInteger, String> keyHashGenerator = key ->  new BoundedHashUtil(NodeSettings.Default.IDENTIFIER_SIZE).hash(key.hashCode(), BigInteger.class);
        nettyMessageSender1 = new NettyMessageSender<>();

        MessageHandler<BigInteger, NettyConnectionInfo> handler = new PongMessageHandler<BigInteger, NettyConnectionInfo>() {
            @Override
            public <I extends KademliaMessage<BigInteger, NettyConnectionInfo, ?>, O extends KademliaMessage<BigInteger, NettyConnectionInfo, ?>> O doHandle(KademliaNodeAPI<BigInteger, NettyConnectionInfo> kademliaNode, I message) {
                kademliaNode.getRoutingTable().getBuckets().stream().forEach(x -> System.out.println(x.toString()));
                return (O) doHandle(kademliaNode, (PongKademliaMessage<BigInteger, NettyConnectionInfo>) message);
            }
        };

        node1 = new NettyKademliaDHTNodeBuilder<>(
                BigInteger.valueOf(1),
                new NettyConnectionInfo("127.0.0.1", 8081),
                new SampleRepository(),
                keyHashGenerator
        ).withNodeSettings(settings).build();
        node1.start();
        // node 2
        node2 = new NettyKademliaDHTNodeBuilder<>(
                BigInteger.valueOf(2),
                new NettyConnectionInfo("127.0.0.1", 8082),
                new SampleRepository(),
                keyHashGenerator
        ).withNodeSettings(settings).build();
        node2.registerMessageHandler(MessageType.PONG, handler);
        System.out.println("Bootstrapped? " + node2.start(node1).get(1, TimeUnit.SECONDS));
        node2.store("V",kademliaData);
        Thread.sleep(3000);
        KademliaData cp=node1.lookup("V").get().getValue();
        KademliaData cp2=node2.lookup("V").get().getValue();
        System.out.println(cp.toString());
        assertEquals(seridata,cp);
        assertEquals(seridata,cp2);
    }

    public static class SampleRepository implements KademliaRepository<String, KademliaData> {
        protected final Map<String, KademliaData> data = new HashMap<>();

        @Override
        public void store(String key, KademliaData value) {

            data.putIfAbsent(key, value);
        }

        @Override
        public KademliaData get(String key) {
            return data.get(key);
        }

        @Override
        public void remove(String key) {
            data.remove(key);
        }

        @Override
        public boolean contains(String key) {
            return data.containsKey(key);
        }
    }
}
