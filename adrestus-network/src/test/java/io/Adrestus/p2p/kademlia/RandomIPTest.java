package io.Adrestus.p2p.kademlia;

import com.google.common.net.InetAddresses;
import com.google.gson.Gson;
import io.Adrestus.config.KademliaConfiguration;
import io.Adrestus.config.NodeSettings;
import io.Adrestus.core.Resourses.MemoryTreePool;
import io.Adrestus.core.Trie.PatriciaTreeNode;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.SecurityAuditProofs;
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
import io.Adrestus.p2p.kademlia.exception.UnsupportedBoundingException;
import io.Adrestus.p2p.kademlia.node.KeyHashGenerator;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.p2p.kademlia.table.Bucket;
import io.Adrestus.p2p.kademlia.table.DefaultRoutingTableFactory;
import io.Adrestus.p2p.kademlia.table.RoutingTable;
import io.Adrestus.p2p.kademlia.table.RoutingTableFactory;
import io.Adrestus.p2p.kademlia.util.BoundedHashUtil;
import io.Adrestus.p2p.kademlia.util.LoggerKademlia;
import org.apache.commons.codec.binary.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RandomIPTest {
    private static int version = 0x00;
    private static KademliaData kademliaData;
    private static OkHttpMessageSender<String, String> nettyMessageSender1;
    private static BLSPublicKey vk;
    private static NettyKademliaDHTNode<String, KademliaData> node1;
    private static NettyKademliaDHTNode<String, KademliaData> node2;
    private static KademliaData seridata;

    @BeforeAll
    public static void setup() throws Exception {
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


        kademliaData = new KademliaData(new SecurityAuditProofs(adddress, ecKeyPair.getPublicKey(), signatureData));
        kademliaData.getAddressData().setValidatorBlSPublicKey(vk);
        MemoryTreePool.getInstance().store(adddress, new PatriciaTreeNode(1000, 0));
        Gson gson = new Gson();
        String jsonString = gson.toJson(kademliaData);
        KademliaData copydata = gson.fromJson(jsonString, KademliaData.class);
        assertEquals(kademliaData, copydata);

        kademliaData.setHash(jsonString);
        Signature bls_sig = BLSSignature.sign(StringUtils.getBytesUtf8(kademliaData.getHash()), sk);


        String jsonString2 = gson.toJson(kademliaData);
        seridata = gson.fromJson(jsonString2, KademliaData.class);
        KademliaData clonebale = (KademliaData) seridata.clone();
        assertEquals(seridata, clonebale);

        clonebale.setHash("");


        //checks
        String clonedhash = gson.toJson(clonebale);
        assertEquals(seridata.getHash(), clonedhash);
        boolean verify2 = ecdsaSign.secp256Verify(HashUtil.sha256(StringUtils.getBytesUtf8(seridata.getAddressData().getAddress())), seridata.getAddressData().getAddress(), seridata.getAddressData().getECDSASignature());
        assertEquals(true, verify2);
        System.out.println("done");
    }

    @Test
    public void shouldAnswerWithTrue() throws InterruptedException, ExecutionException {
        LoggerKademlia.setLevelOFF();
        int port = 1080;
        //use this only for debug not for tests because nodesjoiningtest
        //produces error and need size of 4
        //KademliaConfiguration.IDENTIFIER_SIZE=3;
        NodeSettings.getInstance();
        KeyHashGenerator<BigInteger, String> keyHashGenerator = key -> {
            try {
                return new BoundedHashUtil(NodeSettings.getInstance().getIdentifierSize()).hash(new BigInteger(HashUtil.convertIPtoHex(key, 16)), BigInteger.class);
            } catch (UnsupportedBoundingException e) {
                throw new IllegalArgumentException("Key hash generator not valid");
            }
        };

        // node 1
        NettyKademliaDHTNode<String, KademliaData> bootsrtap = new NettyKademliaDHTNodeBuilder<>(
                BigInteger.valueOf(10),
                new NettyConnectionInfo("127.0.0.1", port),
                new AdrestusNodeTest.SampleRepository(),
                keyHashGenerator
        ).build();
        bootsrtap.start();

        RoutingTableFactory<BigInteger, NettyConnectionInfo, Bucket<BigInteger, NettyConnectionInfo>> routingTableFactory = new DefaultRoutingTableFactory<>();

        port = port + 1;
        ArrayList<NettyKademliaDHTNode<String, KademliaData>> list = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            String ipString1 = InetAddresses.fromInteger(random.nextInt()).getHostAddress();
            BigInteger id1 = new BigInteger(HashUtil.convertIPtoHex(ipString1, 16));
            RoutingTable<BigInteger, NettyConnectionInfo, Bucket<BigInteger, NettyConnectionInfo>> routingTable = routingTableFactory.getRoutingTable(BigInteger.valueOf(i));
            NettyKademliaDHTNode<String, KademliaData> nextnode = new NettyKademliaDHTNodeBuilder<>(
                    id1,
                    new NettyConnectionInfo("127.0.0.1", port + (int) i),
                    new AdrestusNodeTest.SampleRepository(),
                    keyHashGenerator
            ).routingTable(routingTable).build();
            System.out.println("Starting node " + nextnode.getId() + ": " + nextnode.start(bootsrtap).get());
            list.add(nextnode);
        }

        Thread.sleep(4000);
        list.get(4).getRoutingTable().getBuckets().forEach(bucket -> {
            System.out.println("Bucket [" + bucket.getId() + "] -> " + bucket.getNodeIds());
        });
        list.forEach(x -> x.stop());
        bootsrtap.stop();
    }
}
