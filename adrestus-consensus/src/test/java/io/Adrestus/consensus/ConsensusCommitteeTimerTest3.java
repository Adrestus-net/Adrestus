package io.Adrestus.consensus;

import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.config.ConsensusConfiguration;
import io.Adrestus.config.KademliaConfiguration;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedSecurityHeaders;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.SecurityAuditProofs;
import io.Adrestus.crypto.WalletAddress;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.crypto.elliptic.ECDSASignatureData;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import io.Adrestus.crypto.elliptic.mapper.StakingData;
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import io.Adrestus.crypto.vdf.engine.VdfEngine;
import io.Adrestus.crypto.vdf.engine.VdfEnginePietrzak;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseInstance;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import org.apache.commons.codec.binary.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class ConsensusCommitteeTimerTest3 {
    private static BLSPrivateKey sk1;
    private static BLSPublicKey vk1;

    private static BLSPrivateKey sk2;
    private static BLSPublicKey vk2;

    private static BLSPrivateKey sk3;
    private static BLSPublicKey vk3;

    private static BLSPrivateKey sk4;
    private static BLSPublicKey vk4;

    private static BLSPrivateKey sk5;
    private static BLSPublicKey vk5;

    private static ECKeyPair ecKeyPair1, ecKeyPair2, ecKeyPair3, ecKeyPair4, ecKeyPair5;
    private static String address1, address2, address3, address4, address5;
    private static ECDSASign ecdsaSign = new ECDSASign();
    private static VdfEngine vdf;

    @BeforeAll
    public static void setup() throws Exception {
        if (System.out.getClass().getName().contains("maven")) {
            return;
        }
        IDatabase<String, CommitteeBlock> db = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);
        db.delete_db();
        IDatabase<String, CommitteeBlock> database = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);
        vdf = new VdfEnginePietrzak(2048);

        int version = 0x00;
        sk1 = new BLSPrivateKey(1);
        vk1 = new BLSPublicKey(sk1);

        sk2 = new BLSPrivateKey(2);
        vk2 = new BLSPublicKey(sk2);

        sk3 = new BLSPrivateKey(3);
        vk3 = new BLSPublicKey(sk3);

        sk4 = new BLSPrivateKey(4);
        vk4 = new BLSPublicKey(sk4);

        sk5 = new BLSPrivateKey(5);
        vk5 = new BLSPublicKey(sk5);


        char[] mnemonic1 = "sample sail jungle learn general promote task puppy own conduct green affair ".toCharArray();
        char[] mnemonic2 = "photo monitor cushion indicate civil witness orchard estate online favorite sustain extend".toCharArray();
        char[] mnemonic3 = "initial car bulb nature animal honey learn awful grit arrow phrase entire ".toCharArray();
        char[] mnemonic4 = "enrich pulse twin version inject horror village aunt brief magnet blush else ".toCharArray();
        char[] mnemonic5 = "struggle travel ketchup tomato satoshi caught fog process grace pupil item ahead ".toCharArray();
        char[] passphrase = "p4ssphr4se".toCharArray();

        Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
        byte[] key1 = mnem.createSeed(mnemonic1, passphrase);
        byte[] key2 = mnem.createSeed(mnemonic2, passphrase);
        byte[] key3 = mnem.createSeed(mnemonic3, passphrase);
        byte[] key4 = mnem.createSeed(mnemonic4, passphrase);
        byte[] key5 = mnem.createSeed(mnemonic5, passphrase);

        SecureRandom random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(key1);
        ecKeyPair1 = Keys.createEcKeyPair(random);
        random.setSeed(key2);
        ecKeyPair2 = Keys.createEcKeyPair(random);
        random.setSeed(key3);
        ecKeyPair3 = Keys.createEcKeyPair(random);
        random.setSeed(key4);
        ecKeyPair4 = Keys.createEcKeyPair(random);
        random.setSeed(key5);
        ecKeyPair5 = Keys.createEcKeyPair(random);

        address1 = WalletAddress.generate_address((byte) version, ecKeyPair1.getPublicKey());
        address2 = WalletAddress.generate_address((byte) version, ecKeyPair2.getPublicKey());
        address3 = WalletAddress.generate_address((byte) version, ecKeyPair3.getPublicKey());
        address4 = WalletAddress.generate_address((byte) version, ecKeyPair4.getPublicKey());
        address5 = WalletAddress.generate_address((byte) version, ecKeyPair5.getPublicKey());

        ECDSASignatureData signatureData1 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address1)), ecKeyPair1);
        ECDSASignatureData signatureData2 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address2)), ecKeyPair2);
        ECDSASignatureData signatureData3 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address3)), ecKeyPair3);
        ECDSASignatureData signatureData4 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address4)), ecKeyPair4);
        ECDSASignatureData signatureData5 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address5)), ecKeyPair5);

        TreeFactory.getMemoryTree(0).store(address1, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(0).store(address2, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(0).store(address3, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(0).store(address4, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(0).store(address5, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));

        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.getHeaderData().setTimestamp("2022-11-18 15:01:29.304");
        committeeBlock.getStructureMap().get(0).put(vk1, "192.168.1.106");
        committeeBlock.getStructureMap().get(0).put(vk2, "192.168.1.113");
        committeeBlock.getStructureMap().get(0).put(vk3, "192.168.1.116");
        committeeBlock.getStructureMap().get(0).put(vk4, "192.168.1.110");
        committeeBlock.getStructureMap().get(0).put(vk5, "192.168.1.112");


        committeeBlock.getStakingMap().put(new StakingData(1, BigDecimal.valueOf(10.0)), new KademliaData(new SecurityAuditProofs(address1, vk1, ecKeyPair1.getPublicKey(), signatureData1), new NettyConnectionInfo("192.168.1.106", KademliaConfiguration.PORT)));
        committeeBlock.getStakingMap().put(new StakingData(2, BigDecimal.valueOf(11.0)), new KademliaData(new SecurityAuditProofs(address2, vk2, ecKeyPair2.getPublicKey(), signatureData2), new NettyConnectionInfo("192.168.1.113", KademliaConfiguration.PORT)));
        committeeBlock.getStakingMap().put(new StakingData(3, BigDecimal.valueOf(151.0)), new KademliaData(new SecurityAuditProofs(address3, vk3, ecKeyPair3.getPublicKey(), signatureData3), new NettyConnectionInfo("192.168.1.116", KademliaConfiguration.PORT)));
        committeeBlock.getStakingMap().put(new StakingData(4, BigDecimal.valueOf(16.0)), new KademliaData(new SecurityAuditProofs(address4, vk4, ecKeyPair4.getPublicKey(), signatureData4), new NettyConnectionInfo("192.168.1.110", KademliaConfiguration.PORT)));
        committeeBlock.getStakingMap().put(new StakingData(5, BigDecimal.valueOf(271.0)), new KademliaData(new SecurityAuditProofs(address5, vk5, ecKeyPair5.getPublicKey(), signatureData5), new NettyConnectionInfo("192.168.1.112", KademliaConfiguration.PORT)));

        CachedLatestBlocks.getInstance().setCommitteeBlock(committeeBlock);

        CachedLatestBlocks.getInstance().getCommitteeBlock().setDifficulty(112);
        CachedLatestBlocks.getInstance().getCommitteeBlock().setHash("hash");
        CachedLatestBlocks.getInstance().getCommitteeBlock().setGeneration(0);
        CachedLatestBlocks.getInstance().getCommitteeBlock().setHeight(0);

        database.save(String.valueOf(CachedLatestBlocks.getInstance().getCommitteeBlock().getGeneration()), CachedLatestBlocks.getInstance().getCommitteeBlock());

        CachedSecurityHeaders.getInstance().getSecurityHeader().setPRnd(Hex.decode("c1f72aa5bd1e1d53c723b149259b63f759f40d5ab003b547d5c13d45db9a5da8"));
        CachedSecurityHeaders.getInstance().getSecurityHeader().setRnd(vdf.solve(CachedSecurityHeaders.getInstance().getSecurityHeader().getPRnd(), CachedLatestBlocks.getInstance().getCommitteeBlock().getDifficulty()));

    }

    @Test
    public void committe_test() throws Exception {
        if (System.out.getClass().getName().contains("maven")) {
            return;
        }
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
                } else if (vk3.equals(entry.getKey())) {
                    CachedBLSKeyPair.getInstance().setPrivateKey(sk3);
                    CachedBLSKeyPair.getInstance().setPublicKey(vk3);
                } else if (vk4.equals(entry.getKey())) {
                    CachedBLSKeyPair.getInstance().setPrivateKey(sk4);
                    CachedBLSKeyPair.getInstance().setPublicKey(vk4);
                } else if (vk5.equals(entry.getKey())) {
                    CachedBLSKeyPair.getInstance().setPrivateKey(sk5);
                    CachedBLSKeyPair.getInstance().setPublicKey(vk5);
                }

                hit = 1;
                break;
            }
        }
        if (hit == 0)
            return;

        ConsensusConfiguration.EPOCH_TRANSITION = 0;
        CountDownLatch latch = new CountDownLatch(5);
        ConsensusState c = new ConsensusState(latch);
        c.getTransaction_block_timer().scheduleAtFixedRate(new ConsensusState.TransactionBlockConsensusTask(), ConsensusConfiguration.CONSENSUS_TIMER, ConsensusConfiguration.CONSENSUS_TIMER);
        //c.getCommittee_block_timer().scheduleAtFixedRate(new ConsensusState.CommitteeBlockConsensusTask(), ConsensusConfiguration.CONSENSUS_COMMITTEE_TIMER, ConsensusConfiguration.CONSENSUS_COMMITTEE_TIMER);
        latch.await();

    }
}
