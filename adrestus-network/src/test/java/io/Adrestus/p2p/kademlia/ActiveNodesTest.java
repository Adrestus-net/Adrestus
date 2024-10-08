package io.Adrestus.p2p.kademlia;

import com.google.common.net.InetAddresses;
import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.config.KademliaConfiguration;
import io.Adrestus.config.NodeSettings;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.SecurityAuditProofs;
import io.Adrestus.crypto.WalletAddress;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.crypto.elliptic.ECDSASignatureData;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.exception.DuplicateStoreRequest;
import io.Adrestus.p2p.kademlia.exception.FullBucketException;
import io.Adrestus.p2p.kademlia.exception.UnsupportedBoundingException;
import io.Adrestus.p2p.kademlia.helpers.EmptyConnectionInfo;
import io.Adrestus.p2p.kademlia.node.DHTBootstrapNode;
import io.Adrestus.p2p.kademlia.node.DHTRegularNode;
import io.Adrestus.p2p.kademlia.node.KeyHashGenerator;
import io.Adrestus.p2p.kademlia.node.Node;
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ActiveNodesTest {
    public static ArrayList<String> addreses;
    private static ArrayList<ECKeyPair> keypair;
    private static ECDSASign ecdsaSign;
    private static BLSPrivateKey sk1;
    private static BLSPublicKey vk1;

    @BeforeAll
    public static void setup() throws Exception {

        sk1 = new BLSPrivateKey(1);
        vk1 = new BLSPublicKey(sk1);

        ecdsaSign = new ECDSASign();
        int version = 0x00;
        addreses = new ArrayList<>();
        keypair = new ArrayList<>();
        char[] mnemonic1 = "sample sail jungle learn general promote task puppy own conduct green affair ".toCharArray();
        char[] mnemonic2 = "photo monitor cushion indicate civil witness orchard estate online favorite sustain extend".toCharArray();
        char[] mnemonic3 = "initial car bulb nature animal honey learn awful grit arrow phrase entire ".toCharArray();
        char[] mnemonic4 = "enrich pulse twin version inject horror village aunt brief magnet blush else ".toCharArray();
        char[] mnemonic5 = "struggle travel ketchup tomato satoshi caught fog process grace pupil item ahead ".toCharArray();
        char[] mnemonic6 = "spare defense enhance settle sun educate peace steel broken praise fluid intact ".toCharArray();
        char[] mnemonic7 = "harvest school flip powder plunge bitter noise artefact actor people motion sport".toCharArray();
        char[] mnemonic8 = "crucial rule cute steak mandate source supply current remove laugh blouse dial".toCharArray();
        char[] mnemonic9 = "skate fluid door glide pause any youth jelly spatial faith chase sad ".toCharArray();
        char[] mnemonic10 = "abstract raise duty scare year add fluid danger include smart senior ensure".toCharArray();
        char[] passphrase = "p4ssphr4se".toCharArray();

        Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
        byte[] key1 = mnem.createSeed(mnemonic1, passphrase);
        byte[] key2 = mnem.createSeed(mnemonic2, passphrase);
        byte[] key3 = mnem.createSeed(mnemonic3, passphrase);
        byte[] key4 = mnem.createSeed(mnemonic4, passphrase);
        byte[] key5 = mnem.createSeed(mnemonic5, passphrase);
        byte[] key6 = mnem.createSeed(mnemonic6, passphrase);
        byte[] key7 = mnem.createSeed(mnemonic7, passphrase);
        byte[] key8 = mnem.createSeed(mnemonic8, passphrase);
        byte[] key9 = mnem.createSeed(mnemonic9, passphrase);
        byte[] key10 = mnem.createSeed(mnemonic10, passphrase);

        ECKeyPair ecKeyPair1 = Keys.createEcKeyPair(new SecureRandom(key1));
        ECKeyPair ecKeyPair2 = Keys.createEcKeyPair(new SecureRandom(key2));
        ECKeyPair ecKeyPair3 = Keys.createEcKeyPair(new SecureRandom(key3));
        ECKeyPair ecKeyPair4 = Keys.createEcKeyPair(new SecureRandom(key4));
        ECKeyPair ecKeyPair5 = Keys.createEcKeyPair(new SecureRandom(key5));
        ECKeyPair ecKeyPair6 = Keys.createEcKeyPair(new SecureRandom(key6));
        ECKeyPair ecKeyPair7 = Keys.createEcKeyPair(new SecureRandom(key7));
        ECKeyPair ecKeyPair8 = Keys.createEcKeyPair(new SecureRandom(key8));
        ECKeyPair ecKeyPair9 = Keys.createEcKeyPair(new SecureRandom(key9));
        ECKeyPair ecKeyPair10 = Keys.createEcKeyPair(new SecureRandom(key10));
        String adddress1 = WalletAddress.generate_address((byte) version, ecKeyPair1.getPublicKey());
        String adddress2 = WalletAddress.generate_address((byte) version, ecKeyPair2.getPublicKey());
        String adddress3 = WalletAddress.generate_address((byte) version, ecKeyPair3.getPublicKey());
        String adddress4 = WalletAddress.generate_address((byte) version, ecKeyPair4.getPublicKey());
        String adddress5 = WalletAddress.generate_address((byte) version, ecKeyPair5.getPublicKey());
        String adddress6 = WalletAddress.generate_address((byte) version, ecKeyPair6.getPublicKey());
        String adddress7 = WalletAddress.generate_address((byte) version, ecKeyPair7.getPublicKey());
        String adddress8 = WalletAddress.generate_address((byte) version, ecKeyPair8.getPublicKey());
        String adddress9 = WalletAddress.generate_address((byte) version, ecKeyPair9.getPublicKey());
        String adddress10 = WalletAddress.generate_address((byte) version, ecKeyPair10.getPublicKey());

        addreses.add(adddress1);
        addreses.add(adddress2);
        addreses.add(adddress3);
        addreses.add(adddress4);
        addreses.add(adddress5);
        addreses.add(adddress6);
        addreses.add(adddress7);
        addreses.add(adddress8);
        addreses.add(adddress9);
        addreses.add(adddress10);
        keypair.add(ecKeyPair1);
        keypair.add(ecKeyPair2);
        keypair.add(ecKeyPair3);
        keypair.add(ecKeyPair4);
        keypair.add(ecKeyPair5);
        keypair.add(ecKeyPair6);
        keypair.add(ecKeyPair7);
        keypair.add(ecKeyPair8);
        keypair.add(ecKeyPair9);
        keypair.add(ecKeyPair10);

        TreeFactory.getMemoryTree(0).store(adddress1, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(0).store(adddress2, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(0).store(adddress3, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(0).store(adddress4, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(0).store(adddress5, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(0).store(adddress6, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(0).store(adddress7, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(0).store(adddress8, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(0).store(adddress9, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(0).store(adddress10, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
    }

    @Test
    public void test() throws FullBucketException, InterruptedException, DuplicateStoreRequest, ExecutionException, TimeoutException {
        LoggerKademlia.setLevelOFF();
        int port = 1080;
        KademliaConfiguration.IDENTIFIER_SIZE = 3;
        NodeSettings.getInstance();
        KeyHashGenerator<BigInteger, String> keyHashGenerator = key -> {
            try {
                return new BoundedHashUtil(NodeSettings.getInstance().getIdentifierSize()).hash(key.hashCode(), BigInteger.class);
                // return new BoundedHashUtil(NodeSettings.getInstance().getIdentifierSize()).hash(new BigInteger(HashUtil.convertIPtoHex(key, 16)), BigInteger.class);
            } catch (UnsupportedBoundingException e) {
                throw new IllegalArgumentException("Key hash generator not valid");
            }
        };

        // node 1
        DHTBootstrapNode dhtBootstrapNode = new DHTBootstrapNode(
                new NettyConnectionInfo("127.0.0.1", port),
                BigInteger.valueOf(7L),
                keyHashGenerator);
        dhtBootstrapNode.start();
        RoutingTableFactory<BigInteger, NettyConnectionInfo, Bucket<BigInteger, NettyConnectionInfo>> routingTableFactory = new DefaultRoutingTableFactory<>();

        port = port + 1;
        Random random = new Random();
        ArrayList<DHTRegularNode> list = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            String ipString1 = InetAddresses.fromInteger(random.nextInt()).getHostAddress();
            BigInteger id1 = new BigInteger(HashUtil.convertIPtoHex(ipString1, 16));
            RoutingTable<BigInteger, NettyConnectionInfo, Bucket<BigInteger, NettyConnectionInfo>> routingTable = routingTableFactory.getRoutingTable(BigInteger.valueOf(i));
            NettyConnectionInfo nettyConnectionInfo = new NettyConnectionInfo("127.0.0.1", port + (int) i);
            DHTRegularNode nextnode = new DHTRegularNode(nettyConnectionInfo, BigInteger.valueOf(i), keyHashGenerator);
            ECDSASignatureData signatureData = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(addreses.get(i))), keypair.get(i));
            KademliaData kademliaData = new KademliaData(new SecurityAuditProofs(addreses.get(i), vk1, keypair.get(i).getPublicKey(), signatureData), nettyConnectionInfo);

            //boolean verify = ecdsaSign.secp256Verify(value.getAddressData().getAddress().getBytes(StandardCharsets.UTF_8), value.getAddressData().getAddress(), value.getAddressData().getECDSASignature());
            nextnode.setKademliaData(kademliaData);
            nextnode.start(dhtBootstrapNode, routingTable);
            routingTable.update(nextnode.getRegular_node());
            list.add(nextnode);
            nextnode.getRegular_node().store(String.valueOf(i), kademliaData).get();
        }


        Thread.sleep(3000);
        //assertArrayEquals(list.get(4).getRegular_node().getRoutingTable().getBuckets().get(0).getNodeIds().toArray(),Arrays.asList(BigInteger.valueOf(4)).toArray());
        // assertArrayEquals(list.get(4).getRegular_node().getRoutingTable().getBuckets().get(1).getNodeIds().toArray(),Arrays.asList(BigInteger.valueOf(5)).toArray());
        // assertArrayEquals(list.get(4).getRegular_node().getRoutingTable().getBuckets().get(2).getNodeIds().toArray(),Arrays.asList(BigInteger.valueOf(6),BigInteger.valueOf(7)).toArray());
        // assertArrayEquals(list.get(4).getRegular_node().getRoutingTable().getBuckets().get(3).getNodeIds().toArray(),Arrays.asList(BigInteger.valueOf(0),BigInteger.valueOf(3),BigInteger.valueOf(2),BigInteger.valueOf(1)).toArray());

        List<KademliaData> data = list.get(0).getActiveNodes();
        assertEquals(7, data.size());
        list.forEach(x -> x.close());
        dhtBootstrapNode.close();
    }

    private boolean listContainsAll(List<Node<BigInteger, EmptyConnectionInfo>> referencedNodes, Integer... nodeIds) {
        List<Integer> nodeIdsToContain = new ArrayList<>(Arrays.asList(nodeIds));
        for (Node<BigInteger, EmptyConnectionInfo> referencedNode : referencedNodes) {
            nodeIdsToContain.remove(referencedNode.getId());
        }
        return nodeIdsToContain.size() == 0;
    }
}
