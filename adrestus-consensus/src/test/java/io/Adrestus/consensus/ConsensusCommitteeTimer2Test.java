package io.Adrestus.consensus;

import io.Adrestus.config.KademliaConfiguration;
import io.Adrestus.config.NodeSettings;
import io.Adrestus.consensus.helper.ConsensusCommitteeTimer;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.MemoryTreePool;
import io.Adrestus.core.Trie.PatriciaTreeNode;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.SecurityAuditProofs;
import io.Adrestus.crypto.WalletAddress;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import io.Adrestus.crypto.elliptic.SignatureData;
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.exception.UnsupportedBoundingException;
import io.Adrestus.p2p.kademlia.node.DHTBootstrapNode;
import io.Adrestus.p2p.kademlia.node.DHTCachedNodes;
import io.Adrestus.p2p.kademlia.node.DHTRegularNode;
import io.Adrestus.p2p.kademlia.node.KeyHashGenerator;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.p2p.kademlia.util.BoundedHashUtil;
import io.Adrestus.p2p.kademlia.util.LoggerKademlia;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import org.apache.commons.codec.binary.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class ConsensusCommitteeTimer2Test {
    private static BLSPrivateKey sk1;
    private static BLSPublicKey vk1;

    private static BLSPrivateKey sk2;
    private static BLSPublicKey vk2;

    private static ECKeyPair ecKeyPair1, ecKeyPair2;
    private static String address1, address2;
    private static ECDSASign ecdsaSign = new ECDSASign();

    @BeforeAll
    public static void setup() throws Exception {


        int version = 0x00;
        LoggerKademlia.setLevelOFF();
        int port = 1080;
        KademliaConfiguration.IDENTIFIER_SIZE = 3;
        NodeSettings.getInstance();
        KeyHashGenerator<BigInteger, String> keyHashGenerator = key -> {
            try {
                return new BoundedHashUtil(NodeSettings.getInstance().getIdentifierSize()).hash(key.hashCode(), BigInteger.class);
            } catch (UnsupportedBoundingException e) {
                throw new IllegalArgumentException("Key hash generator not valid");
            }
        };


        sk1 = new BLSPrivateKey(1);
        vk1 = new BLSPublicKey(sk1);

        sk2 = new BLSPrivateKey(2);
        vk2 = new BLSPublicKey(sk2);

        char[] mnemonic1 = "sample sail jungle learn general promote task puppy own conduct green affair ".toCharArray();
        char[] mnemonic2 = "photo monitor cushion indicate civil witness orchard estate online favorite sustain extend".toCharArray();
        char[] passphrase = "p4ssphr4se".toCharArray();

        Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
        byte[] key1 = mnem.createSeed(mnemonic1, passphrase);
        byte[] key2 = mnem.createSeed(mnemonic2, passphrase);
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        random.setSeed(key1);
        ecKeyPair1 = Keys.createEcKeyPair(random);
        random.setSeed(key2);
        ecKeyPair2 = Keys.createEcKeyPair(random);

        address1 = WalletAddress.generate_address((byte) version, ecKeyPair1.getPublicKey());
        address2 = WalletAddress.generate_address((byte) version, ecKeyPair2.getPublicKey());

        SignatureData signatureData1 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address1)), ecKeyPair1);
        SignatureData signatureData2 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address2)), ecKeyPair2);

        MemoryTreePool.getInstance().store(address1, new PatriciaTreeNode(3000, 0, 213));
        MemoryTreePool.getInstance().store(address2, new PatriciaTreeNode(3000, 0, 10));

        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.getHeaderData().setTimestamp("2022-11-18 15:01:29.304");

        //###############################
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("google.com", 80));
        String IP = socket.getLocalAddress().getHostAddress();

        DHTBootstrapNode dhtBootstrapNode = new DHTBootstrapNode(
                new NettyConnectionInfo("192.168.1.106", KademliaConfiguration.BootstrapNodePORT),
                BigInteger.valueOf(0),
                keyHashGenerator);
        NettyConnectionInfo nettyConnectionInfo = null;
        if (IP.equals("192.168.1.106")) {
            nettyConnectionInfo = new NettyConnectionInfo(IP, KademliaConfiguration.BootstrapNodePORT);
            SignatureData signatureData = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address1)), ecKeyPair1);
            KademliaData kademliaData = new KademliaData(new SecurityAuditProofs(dhtBootstrapNode.getNettyConnectionInfo().getHost(), address1, vk1, ecKeyPair1.getPublicKey(), signatureData), nettyConnectionInfo);
            dhtBootstrapNode.setKademliaData(kademliaData);
            dhtBootstrapNode.start();
            dhtBootstrapNode.scheduledFuture();
            Thread.sleep(10000);
            DHTCachedNodes.getInstance().setDhtBootstrapNode(dhtBootstrapNode);
            List<KademliaData> list_data = dhtBootstrapNode.getActiveNodes();
            System.out.println("Size:" + list_data.size());
            committeeBlock.getStructureMap().get(0).put(list_data.get(0).getAddressData().getValidatorBlSPublicKey(), list_data.get(0).getNettyConnectionInfo().getHost());
            committeeBlock.getStructureMap().get(0).put(list_data.get(1).getAddressData().getValidatorBlSPublicKey(), list_data.get(1).getNettyConnectionInfo().getHost());

            committeeBlock.getStakingMap().put(10.0, list_data.get(0).getAddressData());
            committeeBlock.getStakingMap().put(13.0, list_data.get(1).getAddressData());
        } else if (IP.equals("192.168.1.116")) {
            dhtBootstrapNode.Init();
            nettyConnectionInfo = new NettyConnectionInfo(IP, KademliaConfiguration.PORT);
            DHTRegularNode nextnode = new DHTRegularNode(nettyConnectionInfo, BigInteger.valueOf(1), keyHashGenerator);
            SignatureData signatureData = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address2)), ecKeyPair2);
            KademliaData kademliaData = new KademliaData(new SecurityAuditProofs(nettyConnectionInfo.getHost(), address2, vk2, ecKeyPair2.getPublicKey(), signatureData), nettyConnectionInfo);
            nextnode.setKademliaData(kademliaData);
            nextnode.start(dhtBootstrapNode);
            nextnode.scheduledFuture();
            DHTCachedNodes.getInstance().setDhtRegularNode(nextnode);
            Thread.sleep(3000);

            List<KademliaData> list_data = nextnode.getActiveNodes();
            committeeBlock.getStructureMap().get(0).put(list_data.get(1).getAddressData().getValidatorBlSPublicKey(), list_data.get(1).getNettyConnectionInfo().getHost());
            committeeBlock.getStructureMap().get(0).put(list_data.get(0).getAddressData().getValidatorBlSPublicKey(), list_data.get(0).getNettyConnectionInfo().getHost());

            committeeBlock.getStakingMap().put(10.0, list_data.get(1).getAddressData());
            committeeBlock.getStakingMap().put(13.0, list_data.get(0).getAddressData());
        } else {

        }

        //###############################
        //  committeeBlock.getStructureMap().get(0).put(vk1, "192.168.1.106");
        // committeeBlock.getStructureMap().get(0).put(vk2, "192.168.1.116");

        //  committeeBlock.getStakingMap().put(10.0, new SecurityAuditProofs("192.168.1.106", address1, vk1, ecKeyPair1.getPublicKey(), signatureData1));
        //  committeeBlock.getStakingMap().put(13.0, new SecurityAuditProofs("192.168.1.116", address2, vk2, ecKeyPair2.getPublicKey(), signatureData2));

        CachedLatestBlocks.getInstance().setCommitteeBlock(committeeBlock);
    }

    @Test
    public void committe_test() throws Exception {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("google.com", 80));
        String IP = socket.getLocalAddress().getHostAddress();
        int hit = 0;
        for (Map.Entry<BLSPublicKey, String> entry : CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).entrySet()) {
            if (IP.equals(entry.getValue())) {
                if (vk1.equals(entry.getKey())) {
                    CachedBLSKeyPair.getInstance().setPrivateKey(sk1);
                    CachedBLSKeyPair.getInstance().setPublicKey(vk1);
                } else if (vk2.equals(entry.getKey())) {
                    CachedBLSKeyPair.getInstance().setPrivateKey(sk2);
                    CachedBLSKeyPair.getInstance().setPublicKey(vk2);
                }
                hit = 1;
                break;
            }
        }
        if (hit == 0)
            return;
        IDatabase<String, CommitteeBlock> database = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB);

        CountDownLatch latch = new CountDownLatch(5);
        ConsensusCommitteeTimer c = new ConsensusCommitteeTimer(latch);
        latch.await();
        c.close();

        database.deleteAll();
    }
}
