package io.Adrestus.p2p.kademlia;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
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
import io.Adrestus.p2p.kademlia.builder.NettyKademliaDHTNodeBuilder;
import io.Adrestus.p2p.kademlia.client.OkHttpMessageSender;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.exception.DuplicateStoreRequest;
import io.Adrestus.p2p.kademlia.exception.FullBucketException;
import io.Adrestus.p2p.kademlia.model.FindNodeAnswer;
import io.Adrestus.p2p.kademlia.model.LookupAnswer;
import io.Adrestus.p2p.kademlia.model.StoreAnswer;
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
import io.Adrestus.p2p.kademlia.table.Bucket;
import io.Adrestus.p2p.kademlia.table.DefaultRoutingTableFactory;
import io.Adrestus.p2p.kademlia.table.RoutingTable;
import io.Adrestus.p2p.kademlia.table.RoutingTableFactory;
import io.Adrestus.p2p.kademlia.util.BoundedHashUtil;
import org.apache.commons.codec.binary.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.security.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AdrestusNodeTest {
    private static int version = 0x00;
    private static KademliaData kademliaData;
    private static OkHttpMessageSender<String, String> nettyMessageSender1;
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
    public void shouldAnswerWithTrue() throws InterruptedException, ExecutionException {

        int port = 1080;
        NodeSettings.getInstance();
        KeyHashGenerator<Long, String> keyHashGenerator = key -> {
            return new BoundedHashUtil(NodeSettings.getInstance().getIdentifierSize()).hash(key.hashCode(), Long.class);
        };

        // node 1
        NettyKademliaDHTNode<String, KademliaData> bootsrtap = new NettyKademliaDHTNodeBuilder<>(
                7L,
                new NettyConnectionInfo("127.0.0.1", port),
                new SampleRepository(),
                keyHashGenerator
        ).build();
        bootsrtap.start();

        RoutingTableFactory<Long, NettyConnectionInfo, Bucket<Long, NettyConnectionInfo>> routingTableFactory = new DefaultRoutingTableFactory<>();

        port = port + 1;
        ArrayList<NettyKademliaDHTNode<String, KademliaData>> list = new ArrayList<>();
        for (long i = 0; i < 7; i++) {
            RoutingTable<Long, NettyConnectionInfo, Bucket<Long, NettyConnectionInfo>> routingTable = routingTableFactory.getRoutingTable(i);
            NettyKademliaDHTNode<String, KademliaData> nextnode = new NettyKademliaDHTNodeBuilder<>(
                    i,
                    new NettyConnectionInfo("127.0.0.1", port + (int) i),
                    new SampleRepository(),
                    keyHashGenerator
            ).routingTable(routingTable).build();
            System.out.println("Starting node " + nextnode.getId() + ": " + nextnode.start(bootsrtap).get());
            list.add(nextnode);
        }

        Thread.sleep(4000);
        list.get(4).getRoutingTable().getBuckets().forEach(bucket -> {
            System.out.println("Bucket [" + bucket.getId() + "] -> " + bucket.getNodeIds());
        });

        int i=1;
    }
    //@Test
    public void stress_test_networkID() throws ExecutionException, InterruptedException, TimeoutException, DuplicateStoreRequest, FullBucketException {
        // node 2
        Random random = new Random();
        int count = 20;

        KademliaConfiguration.IDENTIFIER_SIZE=3;
        KademliaConfiguration.BUCKET_SIZE=10;
        while (count > 0) {
            String ipString1 = InetAddresses.fromInteger(random.nextInt()).getHostAddress();
            String ipString2 = InetAddresses.fromInteger(random.nextInt()).getHostAddress();
           // Long id1 = new Long(HashUtil.convertIPtoHex(ipString1, 16));
           // Long id2 = new Long(HashUtil.convertIPtoHex(ipString2, 16));
             Long id1 = new Long("0");
            // Long id2 = new Long("6166366639633865");
            DHTBootstrapNode dhtBootstrapNode = new DHTBootstrapNode(new NettyConnectionInfo(KademliaConfiguration.BootstrapNodeIP, KademliaConfiguration.BootstrapNodePORT), id1);
            dhtBootstrapNode.start();

            RoutingTableFactory<Long, NettyConnectionInfo, Bucket<Long, NettyConnectionInfo>> routingTableFactory = new DefaultRoutingTableFactory<>();
            DHTRegularNode  regularNode =
                    new DHTRegularNode(new NettyConnectionInfo(KademliaConfiguration.BootstrapNodeIP, 8082), new Long(String.valueOf(1)));
            regularNode.start(dhtBootstrapNode,routingTableFactory.getRoutingTable(new Long(1)));

            List<Bucket<Long,NettyConnectionInfo>>asd1= regularNode.getRegular_node().getRoutingTable().getBuckets();

            DHTRegularNode  regularNode2 = new DHTRegularNode(new NettyConnectionInfo(KademliaConfiguration.BootstrapNodeIP, 8083), new Long(String.valueOf(2)));
            regularNode2.start(dhtBootstrapNode,routingTableFactory.getRoutingTable(new Long(2)));

            List<Bucket<Long,NettyConnectionInfo>>asd2= regularNode2.getRegular_node().getRoutingTable().getBuckets();

            DHTRegularNode  regularNode3 = new DHTRegularNode(new NettyConnectionInfo(KademliaConfiguration.BootstrapNodeIP, 8084), new Long(String.valueOf(3)));
            regularNode3.start(dhtBootstrapNode,routingTableFactory.getRoutingTable(new Long(3)));

            List<Bucket<Long,NettyConnectionInfo>>asd3= regularNode3.getRegular_node().getRoutingTable().getBuckets();

            DHTRegularNode  regularNode4 = new DHTRegularNode(new NettyConnectionInfo(KademliaConfiguration.BootstrapNodeIP, 8085), new Long(String.valueOf(4)));
            regularNode4.start(dhtBootstrapNode,routingTableFactory.getRoutingTable(new Long(4)));

            DHTRegularNode  regularNode5 = new DHTRegularNode(new NettyConnectionInfo(KademliaConfiguration.BootstrapNodeIP, 8086), new Long(String.valueOf(5)));
            regularNode5.start(dhtBootstrapNode,routingTableFactory.getRoutingTable(new Long(5)));

            DHTRegularNode  regularNode6 = new DHTRegularNode(new NettyConnectionInfo(KademliaConfiguration.BootstrapNodeIP, 8087), new Long(String.valueOf(6)));
            regularNode6.start(dhtBootstrapNode,routingTableFactory.getRoutingTable(new Long(6)));

            DHTRegularNode  regularNode7 = new DHTRegularNode(new NettyConnectionInfo(KademliaConfiguration.BootstrapNodeIP, 8088), new Long(String.valueOf(7)));
            regularNode7.start(dhtBootstrapNode,routingTableFactory.getRoutingTable(new Long(7)));

            DHTRegularNode  regularNode8= new DHTRegularNode(new NettyConnectionInfo(KademliaConfiguration.BootstrapNodeIP, 8089), new Long(String.valueOf(8)));
            regularNode8.start(dhtBootstrapNode,routingTableFactory.getRoutingTable(new Long(8)));


            //regularNode4.getRegular_node().getRoutingTable().update(regularNode.getRegular_node());
          //  regularNode4.getRegular_node().getRoutingTable().update(regularNode2.getRegular_node());
           // regularNode4.getRegular_node().getRoutingTable().update(regularNode3.getRegular_node());

            //regularNode4.getRegular_node().getRoutingTable().update(regularNode.getRegular_node());
            List<Bucket<Long,NettyConnectionInfo>> bucket_of_node4= regularNode4.getRegular_node().getRoutingTable().getBuckets();
            FindNodeAnswer<Long,NettyConnectionInfo> sa=regularNode2.getRegular_node().getRoutingTable().findClosest(new Long("2"));
            List<Bucket<Long,NettyConnectionInfo>>asd5= regularNode5.getRegular_node().getRoutingTable().getBuckets();
            List<Bucket<Long,NettyConnectionInfo>>asd6= regularNode6.getRegular_node().getRoutingTable().getBuckets();
            List<Bucket<Long,NettyConnectionInfo>>asd7= regularNode7.getRegular_node().getRoutingTable().getBuckets();
            List<Bucket<Long,NettyConnectionInfo>>asd8= regularNode8.getRegular_node().getRoutingTable().getBuckets();

            RoutingTable<Long,NettyConnectionInfo,Bucket<Long,NettyConnectionInfo>> table=regularNode4.getRegular_node().getRoutingTable();
            List<Bucket<Long,NettyConnectionInfo>>bootsrap= dhtBootstrapNode.getBootStrapNode().getRoutingTable().getBuckets();

            StoreAnswer<Long, String> storeAnswer = dhtBootstrapNode.getBootStrapNode().store("V", kademliaData).get(5, TimeUnit.SECONDS);
            StoreAnswer<Long, String> storeAnswer1 = regularNode2.getRegular_node().store("F", kademliaData).get(5, TimeUnit.SECONDS);
            StoreAnswer<Long, String> storeAnswer2 = regularNode3.getRegular_node().store("S", kademliaData).get(5, TimeUnit.SECONDS);
            StoreAnswer<Long, String> storeAnswer3 = regularNode4.getRegular_node().store("D", kademliaData).get(5, TimeUnit.SECONDS);

            Thread.sleep(5000);

            KademliaData cp1 = regularNode.getRegular_node().lookup("V").get().getValue();
            KademliaData cp2 = regularNode2.getRegular_node().lookup("V").get().getValue();
            LookupAnswer<Long, String, KademliaData> LOK=regularNode3.getRegular_node().lookup("V").get();
            KademliaData cp4 =  regularNode4.getRegular_node().lookup("V").get().getValue();
            KademliaData cp5 = dhtBootstrapNode.getBootStrapNode().lookup("V").get().getValue();
            KademliaRepository<String,KademliaData>asd23= regularNode.getRegular_node().getKademliaRepository();
            assertEquals(seridata, cp1);
          //  assertEquals(seridata, cp2);
            regularNode.getRegular_node().stop();
            dhtBootstrapNode.getBootStrapNode().stop();
            count--;
        }
    }

    //@Test
    public void test2() throws InterruptedException, DuplicateStoreRequest, ExecutionException, TimeoutException {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("io.netty");
        rootLogger.setLevel(ch.qos.logback.classic.Level.OFF);

        KeyHashGenerator<Long, String> keyHashGenerator = key -> new BoundedHashUtil(NodeSettings.getInstance().getIdentifierSize()).hash(key.hashCode(), Long.class);
        nettyMessageSender1 = new OkHttpMessageSender<>();

        MessageHandler<Long, NettyConnectionInfo> handler = new PongMessageHandler<Long, NettyConnectionInfo>() {
            @Override
            public <I extends KademliaMessage<Long, NettyConnectionInfo, ?>, O extends KademliaMessage<Long, NettyConnectionInfo, ?>> O doHandle(KademliaNodeAPI<Long, NettyConnectionInfo> kademliaNode, I message) {
                kademliaNode.getRoutingTable().getBuckets().stream().forEach(x -> System.out.println(x.toString()));
                return (O) doHandle(kademliaNode, (PongKademliaMessage<Long, NettyConnectionInfo>) message);
            }
        };

        node1 = new NettyKademliaDHTNodeBuilder<>(
                Long.valueOf(1),
                new NettyConnectionInfo("127.0.0.1", 8081),
                new SampleRepository(),
                keyHashGenerator
        ).withNodeSettings(NodeSettings.getInstance()).build();
        node1.start();
        // node 2
        node2 = new NettyKademliaDHTNodeBuilder<>(
                Long.valueOf(2),
                new NettyConnectionInfo("127.0.0.1", 8082),
                new SampleRepository(),
                keyHashGenerator
        ).withNodeSettings(NodeSettings.getInstance()).build();
        node2.registerMessageHandler(MessageType.PONG, handler);
        System.out.println("Bootstrapped? " + node2.start(node1).get(5, TimeUnit.SECONDS));
        node2.store("V", kademliaData);
        Thread.sleep(3000);
        KademliaData cp = node1.lookup("V").get().getValue();
        System.out.println(cp.toString());
        assertEquals(seridata, cp);
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