package io.Adrestus.consensus;

import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.config.KademliaConfiguration;
import io.Adrestus.core.AbstractBlock;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedSecurityHeaders;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.SecurityAuditProofs;
import io.Adrestus.crypto.WalletAddress;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.BLSSignatureData;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.crypto.elliptic.ECDSASignatureData;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomSerializerTreeMap;
import io.Adrestus.crypto.elliptic.mapper.StakingData;
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import io.Adrestus.crypto.vdf.engine.VdfEngine;
import io.Adrestus.crypto.vdf.engine.VdfEnginePietrzak;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseInstance;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import org.apache.commons.codec.binary.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConsensusCommitteeTest {
    public static ArrayList<String> addreses;
    private static ArrayList<ECKeyPair> keypair;
    private static ArrayList<ECDSASignatureData> signatureData;
    private static SerializationUtil<AbstractBlock> serenc;
    private static ECDSASign ecdsaSign = new ECDSASign();
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

    private static BLSPrivateKey sk6;
    private static BLSPublicKey vk6;

    private static BLSPrivateKey sk7;
    private static BLSPublicKey vk7;

    private static BLSPrivateKey sk8;
    private static BLSPublicKey vk8;

    @BeforeAll
    public static void setup() throws Exception {
        IDatabase<String, CommitteeBlock> db = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);
        db.delete_db();
        IDatabase<String, CommitteeBlock> database = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        serenc = new SerializationUtil<AbstractBlock>(AbstractBlock.class, list);
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

        sk6 = new BLSPrivateKey(6);
        vk6 = new BLSPublicKey(sk6);

        sk7 = new BLSPrivateKey(7);
        vk7 = new BLSPublicKey(sk7);

        sk8 = new BLSPrivateKey(8);
        vk8 = new BLSPublicKey(sk8);

        int version = 0x00;
        addreses = new ArrayList<>();
        keypair = new ArrayList<>();
        signatureData = new ArrayList<>();
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

        ECDSASignatureData signatureData1 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress1)), ecKeyPair1);
        ECDSASignatureData signatureData2 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress2)), ecKeyPair2);
        ECDSASignatureData signatureData3 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress3)), ecKeyPair3);
        ECDSASignatureData signatureData4 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress4)), ecKeyPair4);
        ECDSASignatureData signatureData5 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress5)), ecKeyPair5);
        ECDSASignatureData signatureData6 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress5)), ecKeyPair5);
        ECDSASignatureData signatureData7 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress6)), ecKeyPair6);
        ECDSASignatureData signatureData8 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress7)), ecKeyPair7);
        ECDSASignatureData signatureData9 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress8)), ecKeyPair8);
        ECDSASignatureData signatureData10 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress9)), ecKeyPair9);
        ECDSASignatureData signatureData11 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress10)), ecKeyPair10);

        signatureData.add(signatureData1);
        signatureData.add(signatureData2);
        signatureData.add(signatureData3);
        signatureData.add(signatureData4);
        signatureData.add(signatureData5);
        signatureData.add(signatureData6);
        signatureData.add(signatureData7);
        signatureData.add(signatureData8);
        signatureData.add(signatureData9);
        signatureData.add(signatureData10);
        signatureData.add(signatureData11);

        TreeFactory.getMemoryTree(0).store(adddress1, new PatriciaTreeNode(2000, 0));
        TreeFactory.getMemoryTree(0).store(adddress2, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(0).store(adddress3, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(0).store(adddress4, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(0).store(adddress5, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(0).store(adddress6, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(0).store(adddress7, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(0).store(adddress8, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(0).store(adddress9, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(0).store(adddress10, new PatriciaTreeNode(3000, 0));

        CommitteeBlock prevblock = new CommitteeBlock();
        prevblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        prevblock.setHash("hash3");
        prevblock.setGeneration(0);
        prevblock.setHeight(0);
        prevblock.setDifficulty(113);

        prevblock.getStakingMap().put(new StakingData(1, 1010.0), new KademliaData(new SecurityAuditProofs(addreses.get(0), vk1, keypair.get(0).getPublicKey(), signatureData.get(0)), new NettyConnectionInfo("192.168.1.101", KademliaConfiguration.PORT)));
        prevblock.getStakingMap().put(new StakingData(2, 1013.0), new KademliaData(new SecurityAuditProofs(addreses.get(1), vk2, keypair.get(1).getPublicKey(), signatureData.get(1)), new NettyConnectionInfo("192.168.1.101", KademliaConfiguration.PORT)));
        prevblock.getStakingMap().put(new StakingData(3, 1007.0), new KademliaData(new SecurityAuditProofs(addreses.get(2), vk3, keypair.get(2).getPublicKey(), signatureData.get(2)), new NettyConnectionInfo("192.168.1.101", KademliaConfiguration.PORT)));
        prevblock.getStakingMap().put(new StakingData(4, 1022.0), new KademliaData(new SecurityAuditProofs(addreses.get(3), vk4, keypair.get(3).getPublicKey(), signatureData.get(3)), new NettyConnectionInfo("192.168.1.101", KademliaConfiguration.PORT)));
        prevblock.getStakingMap().put(new StakingData(5, 1006.0), new KademliaData(new SecurityAuditProofs(addreses.get(4), vk5, keypair.get(4).getPublicKey(), signatureData.get(4)), new NettyConnectionInfo("192.168.1.101", KademliaConfiguration.PORT)));
        prevblock.getStakingMap().put(new StakingData(6, 1032.0), new KademliaData(new SecurityAuditProofs(addreses.get(5), vk6, keypair.get(5).getPublicKey(), signatureData.get(5)), new NettyConnectionInfo("192.168.1.101", KademliaConfiguration.PORT)));
        prevblock.getStakingMap().put(new StakingData(7, 1012.0), new KademliaData(new SecurityAuditProofs(addreses.get(6), vk7, keypair.get(6).getPublicKey(), signatureData.get(6)), new NettyConnectionInfo("192.168.1.101", KademliaConfiguration.PORT)));
        prevblock.getStakingMap().put(new StakingData(8, 1031.0), new KademliaData(new SecurityAuditProofs(addreses.get(7), vk8, keypair.get(7).getPublicKey(), signatureData.get(7)), new NettyConnectionInfo("192.168.1.101", KademliaConfiguration.PORT)));


        CachedLatestBlocks.getInstance().setCommitteeBlock(prevblock);
        VdfEngine vdf = new VdfEnginePietrzak(2048);
        CachedSecurityHeaders.getInstance().getSecurityHeader().setPRnd(Hex.decode("c1f72aa5bd1e1d53c723b149259b63f759f40d5ab003b547d5c13d45db9a5da8"));
        CachedSecurityHeaders.getInstance().getSecurityHeader().setRnd(vdf.solve(CachedSecurityHeaders.getInstance().getSecurityHeader().getPRnd(), CachedLatestBlocks.getInstance().getCommitteeBlock().getDifficulty()));
        Thread.sleep(500);

        database = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);
        CommitteeBlock firstblock = new CommitteeBlock();
        firstblock.setDifficulty(112);
        firstblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        database.save("1", firstblock);
        Thread.sleep(200);
        CommitteeBlock secondblock = new CommitteeBlock();
        secondblock.setDifficulty(117);
        secondblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        Thread.sleep(200);
        database.save("2", secondblock);
        CommitteeBlock thirdblock = new CommitteeBlock();
        thirdblock.setCommitteeProposer(new int[]{4, 2, 4, 2});
        thirdblock.setDifficulty(119);
        thirdblock.setHeight(254);
        thirdblock.setVRF("c1f72aa5bd1e1d53c723b149259b63f759f40d5ab003b547d5c13d45db9a5da8");
        thirdblock.setHash("hash3");
        thirdblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        database.save("3", thirdblock);
        Thread.sleep(200);

    }

    @Test
    public void ConsensusCommitteBlockTest() throws Exception {
        ConsensusManager consensusManager = new ConsensusManager(true);
        consensusManager.changeStateTo(ConsensusRoleType.SUPERVISOR);

        BLSPrivateKey sk = new BLSPrivateKey(new SecureRandom());
        BLSPublicKey vk = new BLSPublicKey(sk);

        CachedBLSKeyPair.getInstance().setPrivateKey(sk);
        CachedBLSKeyPair.getInstance().setPublicKey(vk);

        var supervisorphase = consensusManager.getRole().manufacturePhases(ConsensusType.COMMITTEE_BLOCK);
        CommitteeBlock committeeBlock = new CommitteeBlock();
        ConsensusMessage<CommitteeBlock> consensusMessage = new ConsensusMessage<>(committeeBlock);

        supervisorphase.AnnouncePhase(consensusMessage);
        CommitteeBlock test = consensusMessage.getData();
        consensusManager.changeStateTo(ConsensusRoleType.VALIDATOR);
        BFTConsensusPhase validatorphase = (BFTConsensusPhase) consensusManager.getRole().manufacturePhases(ConsensusType.COMMITTEE_BLOCK);


        validatorphase.AnnouncePhase(consensusMessage);
        if (consensusMessage.getStatusType().equals(ConsensusStatusType.SUCCESS)) {
            BLSSignatureData blsSignatureData = new BLSSignatureData();
            blsSignatureData.getSignature()[0] = consensusMessage.getChecksumData().getSignature();
            consensusMessage.getSignatures().put(consensusMessage.getChecksumData().getBlsPublicKey(), blsSignatureData);
        }
        sk = new BLSPrivateKey(new SecureRandom());
        vk = new BLSPublicKey(sk);

        CachedBLSKeyPair.getInstance().setPrivateKey(sk);
        CachedBLSKeyPair.getInstance().setPublicKey(vk);

        validatorphase.AnnouncePhase(consensusMessage);

        assertEquals(test, consensusMessage.getData());
        supervisorphase.PreparePhase(consensusMessage);

        HashMap<BLSPublicKey, BLSSignatureData> list = new HashMap<>();
        ;
        validatorphase.PreparePhase(consensusMessage);
        if (consensusMessage.getStatusType().equals(ConsensusStatusType.SUCCESS)) {
            BLSSignatureData blsSignatureData = new BLSSignatureData();
            blsSignatureData.getSignature()[1] = consensusMessage.getChecksumData().getSignature();
            list.put(consensusMessage.getChecksumData().getBlsPublicKey(), blsSignatureData);
        }

        sk = new BLSPrivateKey(new SecureRandom());
        vk = new BLSPublicKey(sk);

        CachedBLSKeyPair.getInstance().setPrivateKey(sk);
        CachedBLSKeyPair.getInstance().setPublicKey(vk);

        validatorphase.PreparePhase(consensusMessage);
        if (consensusMessage.getStatusType().equals(ConsensusStatusType.SUCCESS)) {
            BLSSignatureData blsSignatureData = new BLSSignatureData();
            blsSignatureData.getSignature()[1] = consensusMessage.getChecksumData().getSignature();
            list.put(consensusMessage.getChecksumData().getBlsPublicKey(), blsSignatureData);
        }

        consensusMessage.clear();
        consensusMessage.setSignatures(list);

        supervisorphase.CommitPhase(consensusMessage);

        validatorphase.CommitPhase(consensusMessage);
        validatorphase.CommitPhase(consensusMessage);

    }
}
