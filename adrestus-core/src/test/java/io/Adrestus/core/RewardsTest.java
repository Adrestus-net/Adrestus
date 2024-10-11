package io.Adrestus.core;

import io.Adrestus.MemoryTreePool;
import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.Trie.PatriciaTreeTransactionType;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.config.KademliaConfiguration;
import io.Adrestus.config.RewardConfiguration;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedStartHeightRewards;
import io.Adrestus.core.RewardMechanism.*;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.SecurityAuditProofs;
import io.Adrestus.crypto.WalletAddress;
import io.Adrestus.crypto.bls.BLSSignatureData;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.BLSSignature;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.crypto.elliptic.ECDSASignatureData;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import io.Adrestus.crypto.elliptic.mapper.StakingData;
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.MnemonicException;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.util.GetTime;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import io.distributedLedger.ZoneDatabaseFactory;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RewardsTest {

    private static BLSPrivateKey sk1;
    private static BLSPublicKey vk1;

    private static BLSPrivateKey sk2;
    private static BLSPublicKey vk2;

    private static BLSPrivateKey sk3;
    private static BLSPublicKey vk3;
    private static ECDSASign ecdsaSign = new ECDSASign();
    private static ECKeyPair ecKeyPair1, ecKeyPair2, ecKeyPair3;
    private static int version = 0x00;
    ;

    @SneakyThrows
    @Test
    public void SimpleTestCheckOperations() throws CloneNotSupportedException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, MnemonicException {
        IDatabase<String, TransactionBlock> transactionBlockIDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(0));
        int version = 0x00;
        sk1 = new BLSPrivateKey(1);
        vk1 = new BLSPublicKey(sk1);

        sk2 = new BLSPrivateKey(2);
        vk2 = new BLSPublicKey(sk2);

        sk3 = new BLSPrivateKey(3);
        vk3 = new BLSPublicKey(sk3);


        char[] mnemonic1 = "sample sail jungle learn general promote task puppy own conduct green affair ".toCharArray();
        char[] mnemonic2 = "photo monitor cushion indicate civil witness orchard estate online favorite sustain extend".toCharArray();
        char[] mnemonic3 = "struggle travel ketchup tomato satoshi caught fog process grace pupil item ahead ".toCharArray();
        char[] passphrase = "p4ssphr4se".toCharArray();

        Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
        byte[] key1 = mnem.createSeed(mnemonic1, passphrase);
        byte[] key2 = mnem.createSeed(mnemonic2, passphrase);
        byte[] key3 = mnem.createSeed(mnemonic3, passphrase);
        SecureRandom random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(key1);
        ecKeyPair1 = Keys.createEcKeyPair(random);
        random.setSeed(key2);
        ecKeyPair2 = Keys.createEcKeyPair(random);
        random.setSeed(key3);
        ecKeyPair3 = Keys.createEcKeyPair(random);

        String address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        String address1 = "ADR-GBIV-HG2J-27P5-BNVN-MLN6-DL5V-M3YZ-PKEJ-CFFG-FK4L";
        String address0 = WalletAddress.generate_address((byte) version, ecKeyPair3.getPublicKey());
        ECDSASignatureData signatureData1 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address)), ecKeyPair1);
        ECDSASignatureData signatureData2 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address1)), ecKeyPair2);
        ECDSASignatureData signatureData3 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address1)), ecKeyPair3);


        String address2 = "ADR-GBZX-XXCW-LWJC-J7RZ-Q6BJ-RFBA-J5WU-NBAG-4RL7-7G6Z";
        String address3 = "ADR-GD3G-DK4I-DKM2-IQSB-KBWL-HWRV-BBQA-MUAS-MGXA-5QPP";
        String address4 = "ADR-GC2I-WBAW-IKJE-BWFC-ML6T-BNOC-7XOU-IQ74-BJ5L-WP7G";
        String address5 = "ADR-HC2I-WBAW-IKJE-BWFC-ML6T-BNOC-7XOU-IQ74-BJ1L-WP7G";

        PatriciaTreeNode treeNode2 = new PatriciaTreeNode(BigDecimal.ZERO, 1, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        PatriciaTreeNode treeNode3 = new PatriciaTreeNode(BigDecimal.ZERO, 6, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        PatriciaTreeNode treeNode4 = new PatriciaTreeNode(BigDecimal.ZERO, 7, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        PatriciaTreeNode treeNode5 = new PatriciaTreeNode(BigDecimal.ZERO, 1, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        TreeFactory.getMemoryTree(0).store(address2, treeNode2);
        TreeFactory.getMemoryTree(0).store(address3, treeNode3);
        TreeFactory.getMemoryTree(0).store(address4, treeNode4);
        TreeFactory.getMemoryTree(0).store(address5, treeNode5);

        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.setGeneration(1);
        committeeBlock.getStakingMap().put(new StakingData(1, BigDecimal.valueOf(490)), new KademliaData(new SecurityAuditProofs(address, vk1, ecKeyPair1.getPublicKey(), signatureData1), new NettyConnectionInfo("192.168.1.106", KademliaConfiguration.PORT)));
        committeeBlock.getStakingMap().put(new StakingData(2, BigDecimal.valueOf(270)), new KademliaData(new SecurityAuditProofs(address1, vk2, ecKeyPair2.getPublicKey(), signatureData2), new NettyConnectionInfo("192.168.1.116", KademliaConfiguration.PORT)));
        committeeBlock.getStakingMap().put(new StakingData(2, BigDecimal.valueOf(120)), new KademliaData(new SecurityAuditProofs(address0, vk3, ecKeyPair3.getPublicKey(), signatureData3), new NettyConnectionInfo("192.168.1.119", KademliaConfiguration.PORT)));
        committeeBlock.setCommitteeProposer(new int[committeeBlock.getStakingMap().size()]);
        committeeBlock.setBlockProposer(vk1.toRaw());
        committeeBlock.setLeaderPublicKey(vk1);
        CachedLatestBlocks.getInstance().setCommitteeBlock(committeeBlock);
        CachedStartHeightRewards.getInstance().setRewardsCommitteeEnabled(true);


        TransactionBlock transactionBlock1 = new TransactionBlock();
        transactionBlock1.setHash("1");
        transactionBlock1.setHeight(1);
        transactionBlock1.setGeneration(1);
        Bytes message = Bytes.wrap("Hello, world Block 1".getBytes(UTF_8));
        BLSSignatureData BLSSignatureData1 = new BLSSignatureData();
        BLSSignatureData BLSSignatureData2 = new BLSSignatureData();
        BLSSignatureData BLSSignatureData3 = new BLSSignatureData();
        BLSSignatureData1.getSignature()[0] = BLSSignature.sign(message.toArray(), sk1);
        BLSSignatureData1.getSignature()[1] = BLSSignature.sign(message.toArray(), sk1);
        BLSSignatureData2.getSignature()[0] = BLSSignature.sign(message.toArray(), sk2);
        BLSSignatureData2.getSignature()[1] = BLSSignature.sign(message.toArray(), sk2);
        BLSSignatureData3.getSignature()[0] = BLSSignature.sign(message.toArray(), sk3);
        BLSSignatureData3.getSignature()[1] = BLSSignature.sign(message.toArray(), sk3);

        BLSSignatureData1.getMessageHash()[0] = BLSSignature.GetMessageHashAsBase64String(message.toArray());
        BLSSignatureData1.getMessageHash()[1] = BLSSignature.GetMessageHashAsBase64String(message.toArray());
        BLSSignatureData2.getMessageHash()[0] = BLSSignature.GetMessageHashAsBase64String(message.toArray());
        BLSSignatureData2.getMessageHash()[1] = BLSSignature.GetMessageHashAsBase64String(message.toArray());
        BLSSignatureData3.getMessageHash()[0] = BLSSignature.GetMessageHashAsBase64String(message.toArray());
        BLSSignatureData3.getMessageHash()[1] = BLSSignature.GetMessageHashAsBase64String(message.toArray());

        transactionBlock1.getSignatureData().put(vk1, BLSSignatureData1);
        transactionBlock1.getSignatureData().put(vk2, BLSSignatureData2);
        transactionBlock1.getSignatureData().put(vk3, BLSSignatureData3);
        transactionBlock1.setLeaderPublicKey(vk1);
        transactionBlock1.setBlockProposer(vk1.toRaw());
        TransactionBlock transactionBlock2 = new TransactionBlock();
        transactionBlock2.setHash("2");
        transactionBlock2.setHeight(2);
        transactionBlock2.setGeneration(1);
        Bytes message2 = Bytes.wrap("Hello, world Block 2".getBytes(UTF_8));
        BLSSignatureData BLSSignatureData1a = new BLSSignatureData();
        BLSSignatureData BLSSignatureData2a = new BLSSignatureData();
        BLSSignatureData BLSSignatureData3a = new BLSSignatureData();

        BLSSignatureData1a.getSignature()[0] = BLSSignature.sign(message2.toArray(), sk1);
        BLSSignatureData1a.getSignature()[1] = BLSSignature.sign(message2.toArray(), sk1);
        BLSSignatureData2a.getSignature()[0] = BLSSignature.sign(message2.toArray(), sk2);
        BLSSignatureData2a.getSignature()[1] = BLSSignature.sign(message2.toArray(), sk2);
        BLSSignatureData3a.getSignature()[0] = BLSSignature.sign(message2.toArray(), sk3);
        BLSSignatureData3a.getSignature()[1] = BLSSignature.sign(message2.toArray(), sk3);

        BLSSignatureData1a.getMessageHash()[0] = BLSSignature.GetMessageHashAsBase64String(message2.toArray());
        BLSSignatureData1a.getMessageHash()[1] = BLSSignature.GetMessageHashAsBase64String(message2.toArray());
        BLSSignatureData2a.getMessageHash()[0] = BLSSignature.GetMessageHashAsBase64String(message2.toArray());
        BLSSignatureData2a.getMessageHash()[1] = BLSSignature.GetMessageHashAsBase64String(message2.toArray());
        BLSSignatureData3a.getMessageHash()[0] = BLSSignature.GetMessageHashAsBase64String(message2.toArray());
        BLSSignatureData3a.getMessageHash()[1] = BLSSignature.GetMessageHashAsBase64String(message2.toArray());


        transactionBlock2.getSignatureData().put(vk1, BLSSignatureData1a);
        transactionBlock2.getSignatureData().put(vk2, BLSSignatureData2a);
        transactionBlock2.getSignatureData().put(vk3, BLSSignatureData3a);
        transactionBlockIDatabase.save(String.valueOf(transactionBlock1.getHeight()), transactionBlock1);
        transactionBlockIDatabase.save(String.valueOf(transactionBlock2.getHeight()), transactionBlock2);
        CachedStartHeightRewards.getInstance().setHeight(1);


        PatriciaTreeNode treeNode = new PatriciaTreeNode(BigDecimal.valueOf(12.2), 1, BigDecimal.valueOf(490), BigDecimal.valueOf(40), BigDecimal.ZERO);
        treeNode.getStakingInfo().setCommissionRate(10);
        PatriciaTreeNode treeNode1 = new PatriciaTreeNode(BigDecimal.ZERO, 1, BigDecimal.valueOf(270), BigDecimal.valueOf(30), BigDecimal.ZERO);
        treeNode1.getStakingInfo().setCommissionRate(10);
        PatriciaTreeNode treeNode0 = new PatriciaTreeNode(BigDecimal.ZERO, 1, BigDecimal.valueOf(120), BigDecimal.valueOf(10), BigDecimal.ZERO);
        treeNode0.getStakingInfo().setCommissionRate(10);
        TreeFactory.getMemoryTree(0).store(address, treeNode);
        TreeFactory.getMemoryTree(0).getByaddress(address).get().getDelegation().put(address2, BigDecimal.valueOf(100.0));
        TreeFactory.getMemoryTree(0).getByaddress(address).get().getDelegation().put(address3, BigDecimal.valueOf(100.0));
        TreeFactory.getMemoryTree(0).getByaddress(address).get().getDelegation().put(address4, BigDecimal.valueOf(100.0));
        TreeFactory.getMemoryTree(0).getByaddress(address).get().getDelegation().put(address5, BigDecimal.valueOf(100.0));
        PatriciaTreeNode treeNodeClone = (PatriciaTreeNode) treeNode.clone();
        TreeFactory.getMemoryTree(0).store(address1, treeNode1);
        TreeFactory.getMemoryTree(0).getByaddress(address1).get().getDelegation().put(address2, BigDecimal.valueOf(200.0));
        TreeFactory.getMemoryTree(0).getByaddress(address1).get().getDelegation().put(address3, BigDecimal.valueOf(200.0));
        TreeFactory.getMemoryTree(0).getByaddress(address1).get().getDelegation().put(address4, BigDecimal.valueOf(200.0));
        TreeFactory.getMemoryTree(0).getByaddress(address1).get().getDelegation().put(address5, BigDecimal.valueOf(200.0));
        TreeFactory.getMemoryTree(0).store(address0, treeNode0);

        MemoryTreePool cloned_tree = (MemoryTreePool) TreeFactory.getMemoryTree(0).clone();
        RewardChainBuilder king = new RewardChainBuilder();
        king.makeRequest(new Request(RequestType.EFFECTIVE_STAKE, "EFFECTIVE_STAKE"));
        king.makeRequest(new Request(RequestType.EFFECTIVE_STAKE_RATIO, "EFFECTIVE_STAKE_RATIO"));
        king.makeRequest(new Request(RequestType.DELEGATE_WEIGHTS_CALCULATOR, "DELEGATE_WEIGHTS_CALCULATOR"));
        king.makeRequest(new Request(RequestType.VALIDATOR_REWARD_CALCULATOR, "VALIDATOR_REWARD_CALCULATOR"));
        king.makeRequest(new Request(RequestType.DELEGATE_REWARD_CALCULATOR, "DELEGATE_REWARD_CALCULATOR"));
        king.makeRequest(new Request(RequestType.REWARD_PRECISION_CALCULATOR, "REWARD_PRECISION_CALCULATOR"));
        king.makeRequest(new Request(RequestType.REWARD_STORAGE_CALCULATOR, "REWARD_STORAGE_CALCULATOR", TreeFactory.getMemoryTree(0)));
        CachedRewardMapData.getInstance().clearInstance();

        RewardChainBuilder king2 = new RewardChainBuilder();
        king2.makeRequest(new Request(RequestType.EFFECTIVE_STAKE, "EFFECTIVE_STAKE"));
        king2.makeRequest(new Request(RequestType.EFFECTIVE_STAKE_RATIO, "EFFECTIVE_STAKE_RATIO"));
        king2.makeRequest(new Request(RequestType.DELEGATE_WEIGHTS_CALCULATOR, "DELEGATE_WEIGHTS_CALCULATOR"));
        king2.makeRequest(new Request(RequestType.VALIDATOR_REWARD_CALCULATOR, "VALIDATOR_REWARD_CALCULATOR"));
        king2.makeRequest(new Request(RequestType.DELEGATE_REWARD_CALCULATOR, "DELEGATE_REWARD_CALCULATOR"));
        king2.makeRequest(new Request(RequestType.REWARD_PRECISION_CALCULATOR, "REWARD_PRECISION_CALCULATOR"));
        king2.makeRequest(new Request(RequestType.REWARD_STORAGE_CALCULATOR, "REWARD_STORAGE_CALCULATOR", cloned_tree));

        Map<String, RewardObject> maps = CachedRewardMapData.getInstance().getEffective_stakes_map();


        assertEquals(12.2, TreeFactory.getMemoryTree(0).getByaddress(address).get().getAmount().doubleValue());
        assertEquals(treeNode, treeNodeClone);
        assertNotNull(CachedRewardMapData.getInstance().getEffective_stakes_map().get(address));
        assertNotNull(CachedRewardMapData.getInstance().getEffective_stakes_map().get(address1));

        assertEquals(490.000000, CachedRewardMapData.getInstance().getEffective_stakes_map().get(address).getEffective_stake().doubleValue());
        assertEquals(0.556818, CachedRewardMapData.getInstance().getEffective_stakes_map().get(address).getEffective_stake_ratio().doubleValue());
        assertEquals(7.015907, CachedRewardMapData.getInstance().getEffective_stakes_map().get(address).getUnreal_reward().doubleValue());

        //it has some divergent cause it caclulates leader transaction/committe block rewards its normal
        assertEquals(BigDecimal.valueOf(1.656533), CachedRewardMapData.getInstance().getEffective_stakes_map().get(address).getReal_reward());

        assertEquals(270.000000, CachedRewardMapData.getInstance().getEffective_stakes_map().get(address1).getEffective_stake().doubleValue());
        assertEquals(0.306818, CachedRewardMapData.getInstance().getEffective_stakes_map().get(address1).getEffective_stake_ratio().doubleValue());
        assertEquals(3.865907, CachedRewardMapData.getInstance().getEffective_stakes_map().get(address1).getUnreal_reward().doubleValue());
        assertEquals(0.859090, CachedRewardMapData.getInstance().getEffective_stakes_map().get(address1).getReal_reward().doubleValue());

        assertEquals(120.000000, CachedRewardMapData.getInstance().getEffective_stakes_map().get(address0).getEffective_stake().doubleValue());
        assertEquals(0.136364, CachedRewardMapData.getInstance().getEffective_stakes_map().get(address0).getEffective_stake_ratio().doubleValue());
        assertEquals(1.718186, CachedRewardMapData.getInstance().getEffective_stakes_map().get(address0).getUnreal_reward().doubleValue());
        assertEquals(0.334092, CachedRewardMapData.getInstance().getEffective_stakes_map().get(address0).getReal_reward().doubleValue());


        //not stakers delegators here
        assertEquals(0.204082, CachedRewardMapData.getInstance().getEffective_stakes_map().get(address).getDelegate_stake().get(address2).getWeights().doubleValue());
        assertEquals(0.204082, CachedRewardMapData.getInstance().getEffective_stakes_map().get(address).getDelegate_stake().get(address4).getWeights().doubleValue());


        assertEquals(0.740741, CachedRewardMapData.getInstance().getEffective_stakes_map().get(address1).getDelegate_stake().get(address2).getWeights().doubleValue());
        assertEquals(0.740741, CachedRewardMapData.getInstance().getEffective_stakes_map().get(address1).getDelegate_stake().get(address3).getWeights().doubleValue());
        assertEquals(0.740741, CachedRewardMapData.getInstance().getEffective_stakes_map().get(address1).getDelegate_stake().get(address4).getWeights().doubleValue());

        //treemap asserts stakers
        assertEquals(1.656533, TreeFactory.getMemoryTree(0).getByaddress(address).get().getUnclaimed_reward().doubleValue());
        assertEquals(0.859090, TreeFactory.getMemoryTree(0).getByaddress(address1).get().getUnclaimed_reward().doubleValue());
        assertEquals(0.334092, TreeFactory.getMemoryTree(0).getByaddress(address0).get().getUnclaimed_reward().doubleValue());

        //treemap asserts delegators
        assertEquals(4.295456, TreeFactory.getMemoryTree(0).getByaddress(address2).get().getUnclaimed_reward().doubleValue());
        assertEquals(4.295456, TreeFactory.getMemoryTree(0).getByaddress(address5).get().getUnclaimed_reward().doubleValue());
        assertEquals(4.295456, TreeFactory.getMemoryTree(0).getByaddress(address3).get().getUnclaimed_reward().doubleValue());
        assertEquals(4.295456, TreeFactory.getMemoryTree(0).getByaddress(address4).get().getUnclaimed_reward().doubleValue());

        assertEquals(cloned_tree.getRootHash(), TreeFactory.getMemoryTree(0).getRootHash());

        CachedRewardMapData.getInstance().clearInstance();
        TreeFactory.ClearMemoryTree(0);
        transactionBlockIDatabase.delete_db();
    }

    @SneakyThrows
    @Test
    public void CheckClonedTreeEquals() throws CloneNotSupportedException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, MnemonicException {
        IDatabase<String, TransactionBlock> transactionBlockIDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(0));

        int version = 0x00;
        sk1 = new BLSPrivateKey(1);
        vk1 = new BLSPublicKey(sk1);

        sk2 = new BLSPrivateKey(2);
        vk2 = new BLSPublicKey(sk2);

        sk3 = new BLSPrivateKey(3);
        vk3 = new BLSPublicKey(sk3);


        char[] mnemonic1 = "sample sail jungle learn general promote task puppy own conduct green affair ".toCharArray();
        char[] mnemonic2 = "photo monitor cushion indicate civil witness orchard estate online favorite sustain extend".toCharArray();
        char[] mnemonic3 = "struggle travel ketchup tomato satoshi caught fog process grace pupil item ahead ".toCharArray();
        char[] passphrase = "p4ssphr4se".toCharArray();

        Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
        byte[] key1 = mnem.createSeed(mnemonic1, passphrase);
        byte[] key2 = mnem.createSeed(mnemonic2, passphrase);
        byte[] key3 = mnem.createSeed(mnemonic3, passphrase);
        SecureRandom random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(key1);
        ecKeyPair1 = Keys.createEcKeyPair(random);
        random.setSeed(key2);
        ecKeyPair2 = Keys.createEcKeyPair(random);
        random.setSeed(key3);
        ecKeyPair3 = Keys.createEcKeyPair(random);

        String address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        String address1 = "ADR-GBIV-HG2J-27P5-BNVN-MLN6-DL5V-M3YZ-PKEJ-CFFG-FK4L";
        String address0 = WalletAddress.generate_address((byte) version, ecKeyPair3.getPublicKey());
        ECDSASignatureData signatureData1 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address)), ecKeyPair1);
        ECDSASignatureData signatureData2 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address1)), ecKeyPair2);
        ECDSASignatureData signatureData3 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address1)), ecKeyPair3);


        String address2 = "ADR-GBZX-XXCW-LWJC-J7RZ-Q6BJ-RFBA-J5WU-NBAG-4RL7-7G6Z";
        String address3 = "ADR-GD3G-DK4I-DKM2-IQSB-KBWL-HWRV-BBQA-MUAS-MGXA-5QPP";
        String address4 = "ADR-GC2I-WBAW-IKJE-BWFC-ML6T-BNOC-7XOU-IQ74-BJ5L-WP7G";
        String address5 = "ADR-HC2I-WBAW-IKJE-BWFC-ML6T-BNOC-7XOU-IQ74-BJ1L-WP7G";

        PatriciaTreeNode treeNode2 = new PatriciaTreeNode(BigDecimal.ZERO, 1, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        PatriciaTreeNode treeNode3 = new PatriciaTreeNode(BigDecimal.ZERO, 6, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        PatriciaTreeNode treeNode4 = new PatriciaTreeNode(BigDecimal.ZERO, 7, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        PatriciaTreeNode treeNode5 = new PatriciaTreeNode(BigDecimal.ZERO, 1, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        TreeFactory.getMemoryTree(0).store(address2, treeNode2);
        TreeFactory.getMemoryTree(0).store(address3, treeNode3);
        TreeFactory.getMemoryTree(0).store(address4, treeNode4);
        TreeFactory.getMemoryTree(0).store(address5, treeNode5);

        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.setGeneration(1);
        committeeBlock.getStakingMap().put(new StakingData(1, BigDecimal.valueOf(490)), new KademliaData(new SecurityAuditProofs(address, vk1, ecKeyPair1.getPublicKey(), signatureData1), new NettyConnectionInfo("192.168.1.106", KademliaConfiguration.PORT)));
        committeeBlock.getStakingMap().put(new StakingData(2, BigDecimal.valueOf(270)), new KademliaData(new SecurityAuditProofs(address1, vk2, ecKeyPair2.getPublicKey(), signatureData2), new NettyConnectionInfo("192.168.1.116", KademliaConfiguration.PORT)));
        committeeBlock.getStakingMap().put(new StakingData(2, BigDecimal.valueOf(120)), new KademliaData(new SecurityAuditProofs(address0, vk3, ecKeyPair3.getPublicKey(), signatureData3), new NettyConnectionInfo("192.168.1.119", KademliaConfiguration.PORT)));
        committeeBlock.setCommitteeProposer(new int[committeeBlock.getStakingMap().size()]);
        committeeBlock.setBlockProposer(vk1.toRaw());
        committeeBlock.setLeaderPublicKey(vk1);
        CachedLatestBlocks.getInstance().setCommitteeBlock(committeeBlock);
        CachedStartHeightRewards.getInstance().setRewardsCommitteeEnabled(true);


        TransactionBlock transactionBlock1 = new TransactionBlock();
        transactionBlock1.setHash("1");
        transactionBlock1.setHeight(1);
        transactionBlock1.setGeneration(1);
        Bytes message = Bytes.wrap("Hello, world Block 1".getBytes(UTF_8));
        BLSSignatureData BLSSignatureData1 = new BLSSignatureData();
        BLSSignatureData BLSSignatureData2 = new BLSSignatureData();
        BLSSignatureData1.getSignature()[0] = BLSSignature.sign(message.toArray(), sk1);
        BLSSignatureData1.getSignature()[1] = BLSSignature.sign(message.toArray(), sk1);
        BLSSignatureData2.getSignature()[0] = BLSSignature.sign(message.toArray(), sk2);
        BLSSignatureData2.getSignature()[1] = BLSSignature.sign(message.toArray(), sk2);

        BLSSignatureData1.getMessageHash()[0] = BLSSignature.GetMessageHashAsBase64String(message.toArray());
        BLSSignatureData1.getMessageHash()[1] = BLSSignature.GetMessageHashAsBase64String(message.toArray());
        BLSSignatureData2.getMessageHash()[0] = BLSSignature.GetMessageHashAsBase64String(message.toArray());
        BLSSignatureData2.getMessageHash()[1] = BLSSignature.GetMessageHashAsBase64String(message.toArray());

        transactionBlock1.getSignatureData().put(vk1, BLSSignatureData1);
        transactionBlock1.getSignatureData().put(vk2, BLSSignatureData2);
        transactionBlock1.getSignatureData().put(vk2, BLSSignatureData2);
        transactionBlock1.setLeaderPublicKey(vk1);
        transactionBlock1.setBlockProposer(vk1.toRaw());
        TransactionBlock transactionBlock2 = new TransactionBlock();
        transactionBlock2.setHash("2");
        transactionBlock2.setHeight(2);
        transactionBlock2.setGeneration(1);
        Bytes message2 = Bytes.wrap("Hello, world Block 2".getBytes(UTF_8));
        BLSSignatureData BLSSignatureData1a = new BLSSignatureData();
        BLSSignatureData BLSSignatureData2a = new BLSSignatureData();
        BLSSignatureData BLSSignatureData3a = new BLSSignatureData();

        BLSSignatureData1a.getSignature()[0] = BLSSignature.sign(message2.toArray(), sk1);
        BLSSignatureData1a.getSignature()[1] = BLSSignature.sign(message2.toArray(), sk1);
        BLSSignatureData2a.getSignature()[0] = BLSSignature.sign(message2.toArray(), sk2);
        BLSSignatureData2a.getSignature()[1] = BLSSignature.sign(message2.toArray(), sk2);
        BLSSignatureData3a.getSignature()[0] = BLSSignature.sign(message2.toArray(), sk3);
        BLSSignatureData3a.getSignature()[1] = BLSSignature.sign(message2.toArray(), sk3);

        BLSSignatureData1a.getMessageHash()[0] = BLSSignature.GetMessageHashAsBase64String(message2.toArray());
        BLSSignatureData1a.getMessageHash()[1] = BLSSignature.GetMessageHashAsBase64String(message2.toArray());
        BLSSignatureData2a.getMessageHash()[0] = BLSSignature.GetMessageHashAsBase64String(message2.toArray());
        BLSSignatureData2a.getMessageHash()[1] = BLSSignature.GetMessageHashAsBase64String(message2.toArray());
        BLSSignatureData3a.getMessageHash()[0] = BLSSignature.GetMessageHashAsBase64String(message2.toArray());
        BLSSignatureData3a.getMessageHash()[1] = BLSSignature.GetMessageHashAsBase64String(message2.toArray());


        transactionBlock2.getSignatureData().put(vk1, BLSSignatureData1a);
        transactionBlock2.getSignatureData().put(vk2, BLSSignatureData2a);
        transactionBlock2.getSignatureData().put(vk3, BLSSignatureData3a);
        transactionBlockIDatabase.save(String.valueOf(transactionBlock1.getHeight()), transactionBlock1);
        transactionBlockIDatabase.save(String.valueOf(transactionBlock2.getHeight()), transactionBlock2);
        CachedStartHeightRewards.getInstance().setHeight(1);


        PatriciaTreeNode treeNode = new PatriciaTreeNode(BigDecimal.ZERO, 1, BigDecimal.valueOf(490), BigDecimal.valueOf(40), BigDecimal.ZERO);
        treeNode.getStakingInfo().setCommissionRate(10);
        PatriciaTreeNode treeNode1 = new PatriciaTreeNode(BigDecimal.ZERO, 1, BigDecimal.valueOf(270), BigDecimal.valueOf(30), BigDecimal.ZERO);
        treeNode1.getStakingInfo().setCommissionRate(10);
        PatriciaTreeNode treeNode0 = new PatriciaTreeNode(BigDecimal.ZERO, 1, BigDecimal.valueOf(120), BigDecimal.valueOf(10), BigDecimal.ZERO);
        treeNode0.getStakingInfo().setCommissionRate(10);
        TreeFactory.getMemoryTree(0).store(address, treeNode);
        PatriciaTreeNode treeNodeClone = (PatriciaTreeNode) treeNode.clone();
        TreeFactory.getMemoryTree(0).store(address1, treeNode1);
        TreeFactory.getMemoryTree(0).store(address0, treeNode0);

        MemoryTreePool cloned_tree = (MemoryTreePool) TreeFactory.getMemoryTree(0).clone();
        RewardChainBuilder king = new RewardChainBuilder();
        king.makeRequest(new Request(RequestType.EFFECTIVE_STAKE, "EFFECTIVE_STAKE"));
        king.makeRequest(new Request(RequestType.EFFECTIVE_STAKE_RATIO, "EFFECTIVE_STAKE_RATIO"));
        king.makeRequest(new Request(RequestType.DELEGATE_WEIGHTS_CALCULATOR, "DELEGATE_WEIGHTS_CALCULATOR"));
        king.makeRequest(new Request(RequestType.VALIDATOR_REWARD_CALCULATOR, "VALIDATOR_REWARD_CALCULATOR"));
        king.makeRequest(new Request(RequestType.DELEGATE_REWARD_CALCULATOR, "DELEGATE_REWARD_CALCULATOR"));
        king.makeRequest(new Request(RequestType.REWARD_PRECISION_CALCULATOR, "REWARD_PRECISION_CALCULATOR"));
        king.makeRequest(new Request(RequestType.REWARD_STORAGE_CALCULATOR, "REWARD_STORAGE_CALCULATOR", TreeFactory.getMemoryTree(0)));
        CachedRewardMapData.getInstance().clearInstance();

        RewardChainBuilder king2 = new RewardChainBuilder();
        king2.makeRequest(new Request(RequestType.EFFECTIVE_STAKE, "EFFECTIVE_STAKE"));
        king2.makeRequest(new Request(RequestType.EFFECTIVE_STAKE_RATIO, "EFFECTIVE_STAKE_RATIO"));
        king2.makeRequest(new Request(RequestType.DELEGATE_WEIGHTS_CALCULATOR, "DELEGATE_WEIGHTS_CALCULATOR"));
        king2.makeRequest(new Request(RequestType.VALIDATOR_REWARD_CALCULATOR, "VALIDATOR_REWARD_CALCULATOR"));
        king2.makeRequest(new Request(RequestType.DELEGATE_REWARD_CALCULATOR, "DELEGATE_REWARD_CALCULATOR"));
        king2.makeRequest(new Request(RequestType.REWARD_PRECISION_CALCULATOR, "REWARD_PRECISION_CALCULATOR"));
        king2.makeRequest(new Request(RequestType.REWARD_STORAGE_CALCULATOR, "REWARD_STORAGE_CALCULATOR", cloned_tree));

        Map<String, RewardObject> maps = CachedRewardMapData.getInstance().getEffective_stakes_map();


        assertEquals(treeNode, treeNodeClone);
        assertNotNull(CachedRewardMapData.getInstance().getEffective_stakes_map().get(address));
        assertNotNull(CachedRewardMapData.getInstance().getEffective_stakes_map().get(address1));


        assertEquals(cloned_tree.getRootHash(), TreeFactory.getMemoryTree(0).getRootHash());

        assertEquals(TreeFactory.getMemoryTree(0).getByaddress(address1).get().getUnclaimed_reward(), cloned_tree.getByaddress(address1).get().getUnclaimed_reward());
        assertEquals(TreeFactory.getMemoryTree(0).getByaddress(address2).get().getUnclaimed_reward(), cloned_tree.getByaddress(address2).get().getUnclaimed_reward());
        assertEquals(TreeFactory.getMemoryTree(0).getByaddress(address3).get().getUnclaimed_reward(), cloned_tree.getByaddress(address3).get().getUnclaimed_reward());
        assertEquals(TreeFactory.getMemoryTree(0).getByaddress(address4).get().getUnclaimed_reward(), cloned_tree.getByaddress(address4).get().getUnclaimed_reward());
        assertEquals(TreeFactory.getMemoryTree(0).getByaddress(address5).get().getUnclaimed_reward(), cloned_tree.getByaddress(address5).get().getUnclaimed_reward());

        CachedRewardMapData.getInstance().clearInstance();
        transactionBlockIDatabase.delete_db();
        TreeFactory.ClearMemoryTree(0);
    }

    @SneakyThrows
    @Test
    public void WholeTestMultipleBlocks() {
        IDatabase<String, TransactionBlock> transactionBlockIDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(0));

        int sizeBlocks = 2;
        int version = 0x00;
        sk1 = new BLSPrivateKey(1);
        vk1 = new BLSPublicKey(sk1);

        sk2 = new BLSPrivateKey(2);
        vk2 = new BLSPublicKey(sk2);

        sk3 = new BLSPrivateKey(3);
        vk3 = new BLSPublicKey(sk3);


        char[] mnemonic1 = "sample sail jungle learn general promote task puppy own conduct green affair ".toCharArray();
        char[] mnemonic2 = "photo monitor cushion indicate civil witness orchard estate online favorite sustain extend".toCharArray();
        char[] mnemonic3 = "struggle travel ketchup tomato satoshi caught fog process grace pupil item ahead ".toCharArray();
        char[] passphrase = "p4ssphr4se".toCharArray();

        Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
        byte[] key1 = mnem.createSeed(mnemonic1, passphrase);
        byte[] key2 = mnem.createSeed(mnemonic2, passphrase);
        byte[] key3 = mnem.createSeed(mnemonic3, passphrase);
        SecureRandom random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(key1);
        ecKeyPair1 = Keys.createEcKeyPair(random);
        random.setSeed(key2);
        ecKeyPair2 = Keys.createEcKeyPair(random);
        random.setSeed(key3);
        ecKeyPair3 = Keys.createEcKeyPair(random);

        String address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        String address1 = "ADR-GBIV-HG2J-27P5-BNVN-MLN6-DL5V-M3YZ-PKEJ-CFFG-FK4L";
        String address0 = WalletAddress.generate_address((byte) version, ecKeyPair3.getPublicKey());
        ECDSASignatureData signatureData1 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address)), ecKeyPair1);
        ECDSASignatureData signatureData2 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address1)), ecKeyPair2);
        ECDSASignatureData signatureData3 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address1)), ecKeyPair3);


        String address2 = "ADR-GBZX-XXCW-LWJC-J7RZ-Q6BJ-RFBA-J5WU-NBAG-4RL7-7G6Z";
        String address3 = "ADR-GD3G-DK4I-DKM2-IQSB-KBWL-HWRV-BBQA-MUAS-MGXA-5QPP";
        String address4 = "ADR-GC2I-WBAW-IKJE-BWFC-ML6T-BNOC-7XOU-IQ74-BJ5L-WP7G";
        String address5 = "ADR-HC2I-WBAW-IKJE-BWFC-ML6T-BNOC-7XOU-IQ74-BJ1L-WP7G";

        PatriciaTreeNode treeNode2 = new PatriciaTreeNode(BigDecimal.ZERO, 1, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        PatriciaTreeNode treeNode3 = new PatriciaTreeNode(BigDecimal.ZERO, 6, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        PatriciaTreeNode treeNode4 = new PatriciaTreeNode(BigDecimal.ZERO, 7, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        PatriciaTreeNode treeNode5 = new PatriciaTreeNode(BigDecimal.ZERO, 1, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        TreeFactory.getMemoryTree(0).store(address2, treeNode2);
        TreeFactory.getMemoryTree(0).store(address3, treeNode3);
        TreeFactory.getMemoryTree(0).store(address4, treeNode4);
        TreeFactory.getMemoryTree(0).store(address5, treeNode5);

        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.setGeneration(1);
        committeeBlock.getStakingMap().put(new StakingData(1, BigDecimal.valueOf(490)), new KademliaData(new SecurityAuditProofs(address, vk1, ecKeyPair1.getPublicKey(), signatureData1), new NettyConnectionInfo("192.168.1.106", KademliaConfiguration.PORT)));
        committeeBlock.getStakingMap().put(new StakingData(2, BigDecimal.valueOf(270)), new KademliaData(new SecurityAuditProofs(address1, vk2, ecKeyPair2.getPublicKey(), signatureData2), new NettyConnectionInfo("192.168.1.116", KademliaConfiguration.PORT)));
        committeeBlock.getStakingMap().put(new StakingData(2, BigDecimal.valueOf(120)), new KademliaData(new SecurityAuditProofs(address0, vk3, ecKeyPair3.getPublicKey(), signatureData3), new NettyConnectionInfo("192.168.1.119", KademliaConfiguration.PORT)));
        committeeBlock.setCommitteeProposer(new int[committeeBlock.getStakingMap().size()]);
        committeeBlock.setBlockProposer(vk1.toRaw());
        committeeBlock.setLeaderPublicKey(vk1);
        CachedLatestBlocks.getInstance().setCommitteeBlock(committeeBlock);
        CachedStartHeightRewards.getInstance().setRewardsCommitteeEnabled(true);


        for (int i = 1; i <= sizeBlocks; i++) {
            TransactionBlock transactionBlock = new TransactionBlock();
            transactionBlock.setHash(String.valueOf(i));
            transactionBlock.setHeight(i);
            transactionBlock.setGeneration(i);
            Bytes message = Bytes.wrap(("Hello, world Block " + String.valueOf(i)).getBytes(UTF_8));

            BLSSignatureData BLSSignatureData1 = new BLSSignatureData();
            BLSSignatureData BLSSignatureData2 = new BLSSignatureData();
            BLSSignatureData BLSSignatureData3 = new BLSSignatureData();
            BLSSignatureData1.getSignature()[0] = BLSSignature.sign(message.toArray(), sk1);
            BLSSignatureData1.getSignature()[1] = BLSSignature.sign(message.toArray(), sk1);
            BLSSignatureData2.getSignature()[0] = BLSSignature.sign(message.toArray(), sk2);
            BLSSignatureData2.getSignature()[1] = BLSSignature.sign(message.toArray(), sk2);
            BLSSignatureData3.getSignature()[0] = BLSSignature.sign(message.toArray(), sk3);
            BLSSignatureData3.getSignature()[1] = BLSSignature.sign(message.toArray(), sk3);

            BLSSignatureData1.getMessageHash()[0] = BLSSignature.GetMessageHashAsBase64String(message.toArray());
            BLSSignatureData1.getMessageHash()[1] = BLSSignature.GetMessageHashAsBase64String(message.toArray());
            BLSSignatureData2.getMessageHash()[0] = BLSSignature.GetMessageHashAsBase64String(message.toArray());
            BLSSignatureData2.getMessageHash()[1] = BLSSignature.GetMessageHashAsBase64String(message.toArray());
            BLSSignatureData3.getMessageHash()[0] = BLSSignature.GetMessageHashAsBase64String(message.toArray());
            BLSSignatureData3.getMessageHash()[1] = BLSSignature.GetMessageHashAsBase64String(message.toArray());

            transactionBlock.getSignatureData().put(vk1, BLSSignatureData1);
            transactionBlock.getSignatureData().put(vk2, BLSSignatureData2);
            transactionBlock.getSignatureData().put(vk3, BLSSignatureData3);
            transactionBlock.setLeaderPublicKey(vk1);
            transactionBlock.setBlockProposer(vk1.toRaw());
            transactionBlockIDatabase.save(String.valueOf(transactionBlock.getHeight()), transactionBlock);
        }


        CachedStartHeightRewards.getInstance().setHeight(1);


        PatriciaTreeNode treeNode = new PatriciaTreeNode(BigDecimal.ZERO, 1, BigDecimal.valueOf(490), BigDecimal.valueOf(40), BigDecimal.ZERO);
        treeNode.getStakingInfo().setCommissionRate(10);
        PatriciaTreeNode treeNode1 = new PatriciaTreeNode(BigDecimal.ZERO, 1, BigDecimal.valueOf(270), BigDecimal.valueOf(30), BigDecimal.ZERO);
        treeNode1.getStakingInfo().setCommissionRate(10);
        PatriciaTreeNode treeNode0 = new PatriciaTreeNode(BigDecimal.ZERO, 1, BigDecimal.valueOf(120), BigDecimal.valueOf(10), BigDecimal.ZERO);
        treeNode0.getStakingInfo().setCommissionRate(10);
        TreeFactory.getMemoryTree(0).store(address, treeNode);
        PatriciaTreeNode treeNodeClone = (PatriciaTreeNode) treeNode.clone();
        TreeFactory.getMemoryTree(0).store(address1, treeNode1);
        TreeFactory.getMemoryTree(0).store(address0, treeNode0);

        MemoryTreePool cloned_tree = (MemoryTreePool) TreeFactory.getMemoryTree(0).clone();
        RewardChainBuilder king = new RewardChainBuilder();
        king.makeRequest(new Request(RequestType.EFFECTIVE_STAKE, "EFFECTIVE_STAKE"));
        king.makeRequest(new Request(RequestType.EFFECTIVE_STAKE_RATIO, "EFFECTIVE_STAKE_RATIO"));
        king.makeRequest(new Request(RequestType.DELEGATE_WEIGHTS_CALCULATOR, "DELEGATE_WEIGHTS_CALCULATOR"));
        king.makeRequest(new Request(RequestType.VALIDATOR_REWARD_CALCULATOR, "VALIDATOR_REWARD_CALCULATOR"));
        king.makeRequest(new Request(RequestType.DELEGATE_REWARD_CALCULATOR, "DELEGATE_REWARD_CALCULATOR"));
        king.makeRequest(new Request(RequestType.REWARD_PRECISION_CALCULATOR, "REWARD_PRECISION_CALCULATOR"));
        king.makeRequest(new Request(RequestType.REWARD_STORAGE_CALCULATOR, "REWARD_STORAGE_CALCULATOR", TreeFactory.getMemoryTree(0)));


        CachedRewardMapData.getInstance().clearInstance();
        RewardChainBuilder king1 = new RewardChainBuilder();
        king1.makeRequest(new Request(RequestType.EFFECTIVE_STAKE, "EFFECTIVE_STAKE"));
        king1.makeRequest(new Request(RequestType.EFFECTIVE_STAKE_RATIO, "EFFECTIVE_STAKE_RATIO"));
        king1.makeRequest(new Request(RequestType.DELEGATE_WEIGHTS_CALCULATOR, "DELEGATE_WEIGHTS_CALCULATOR"));
        king1.makeRequest(new Request(RequestType.VALIDATOR_REWARD_CALCULATOR, "VALIDATOR_REWARD_CALCULATOR"));
        king1.makeRequest(new Request(RequestType.DELEGATE_REWARD_CALCULATOR, "DELEGATE_REWARD_CALCULATOR"));
        king1.makeRequest(new Request(RequestType.REWARD_PRECISION_CALCULATOR, "REWARD_PRECISION_CALCULATOR"));
        king1.makeRequest(new Request(RequestType.REWARD_STORAGE_CALCULATOR, "REWARD_STORAGE_CALCULATOR", cloned_tree));

        Map<String, RewardObject> maps = CachedRewardMapData.getInstance().getEffective_stakes_map();


        assertEquals(1.724147,TreeFactory.getMemoryTree(0).getByaddress(address).get().getUnclaimed_reward().doubleValue());
        assertEquals(0.859090,TreeFactory.getMemoryTree(0).getByaddress(address1).get().getUnclaimed_reward().doubleValue());
        assertEquals(0.334092,TreeFactory.getMemoryTree(0).getByaddress(address0).get().getUnclaimed_reward().doubleValue());

        assertEquals(TreeFactory.getMemoryTree(0).getByaddress(address1).get().getUnclaimed_reward(), cloned_tree.getByaddress(address1).get().getUnclaimed_reward());
        assertEquals(TreeFactory.getMemoryTree(0).getByaddress(address2).get().getUnclaimed_reward(), cloned_tree.getByaddress(address2).get().getUnclaimed_reward());
        assertEquals(TreeFactory.getMemoryTree(0).getByaddress(address3).get().getUnclaimed_reward(), cloned_tree.getByaddress(address3).get().getUnclaimed_reward());
        assertEquals(TreeFactory.getMemoryTree(0).getByaddress(address4).get().getUnclaimed_reward(), cloned_tree.getByaddress(address4).get().getUnclaimed_reward());
        assertEquals(TreeFactory.getMemoryTree(0).getByaddress(address5).get().getUnclaimed_reward(), cloned_tree.getByaddress(address5).get().getUnclaimed_reward());

        CachedRewardMapData.getInstance().clearInstance();
        transactionBlockIDatabase.delete_db();
        TreeFactory.ClearMemoryTree(0);
    }


    //This test should be equal with consensus transaction rewards test
    // the correct way is to calculate rewards every 3 blocks not one time after 6 blocks
    @SneakyThrows
    @Test
    public void ConsensusTransactionTimerRewardTest() {
        IDatabase<String, TransactionBlock> transactionBlockIDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(0));
        transactionBlockIDatabase.delete_db();
        transactionBlockIDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(0));

        sk1 = new BLSPrivateKey(1);
        vk1 = new BLSPublicKey(sk1);

        sk2 = new BLSPrivateKey(2);
        vk2 = new BLSPublicKey(sk2);

        sk3 = new BLSPrivateKey(3);
        vk3 = new BLSPublicKey(sk3);

        String address1 = "ADR-AB2W-RIQY-LSIH-CXQQ-FGRV-AINR-57RO-NFXU-IWM5-IANJ";
        String address2 = "ADR-ACAO-BKTC-CFKG-VXWF-PSI2-QHWR-ZIGK-CCOL-LGJN-CM3U";
        String address3 = "ADR-AADE-ROH3-CAFV-XK5V-2NKZ-QMTG-SFMC-37W5-SHUV-2T46";

        char[] mnemonic1 = "sample sail jungle learn general promote task puppy own conduct green affair ".toCharArray();
        char[] mnemonic2 = "photo monitor cushion indicate civil witness orchard estate online favorite sustain extend".toCharArray();
        char[] mnemonic3 = "struggle travel ketchup tomato satoshi caught fog process grace pupil item ahead ".toCharArray();
        char[] passphrase = "p4ssphr4se".toCharArray();

        Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
        byte[] key1 = mnem.createSeed(mnemonic1, passphrase);
        byte[] key2 = mnem.createSeed(mnemonic2, passphrase);
        byte[] key3 = mnem.createSeed(mnemonic3, passphrase);
        SecureRandom random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(key1);
        ecKeyPair1 = Keys.createEcKeyPair(random);
        random.setSeed(key2);
        ecKeyPair2 = Keys.createEcKeyPair(random);
        random.setSeed(key3);
        ecKeyPair3 = Keys.createEcKeyPair(random);

        ECDSASignatureData signatureData1 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address1)), ecKeyPair1);
        ECDSASignatureData signatureData2 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address2)), ecKeyPair2);
        ECDSASignatureData signatureData3 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address3)), ecKeyPair3);

        PatriciaTreeNode treeNode1 = new PatriciaTreeNode(BigDecimal.valueOf(1000), 0, BigDecimal.valueOf(100), BigDecimal.valueOf(40));
        //treeNode1.getStakingInfo().setCommissionRate(10);
        PatriciaTreeNode treeNode2 = new PatriciaTreeNode(BigDecimal.valueOf(1000), 0, BigDecimal.valueOf(200), BigDecimal.valueOf(30));
        // treeNode2.getStakingInfo().setCommissionRate(10);
        PatriciaTreeNode treeNode3 = new PatriciaTreeNode(BigDecimal.valueOf(1000), 0, BigDecimal.valueOf(300), BigDecimal.valueOf(20));
        // treeNode3.getStakingInfo().setCommissionRate(10);
        TreeFactory.getMemoryTree(0).store(address1, treeNode1);
        TreeFactory.getMemoryTree(0).store(address2, treeNode2);
        TreeFactory.getMemoryTree(0).store(address3, treeNode3);

        TransactionBlock prevblock = new TransactionBlock();
        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.setGeneration(1);
        committeeBlock.setViewID(1);
        prevblock.setHeight(1);
        prevblock.setHash("hash");
        prevblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        Bytes message1 = Bytes.wrap("Hello, world Block 1".getBytes(UTF_8));
        BLSSignatureData BLSSignatureData1a = new BLSSignatureData();
        BLSSignatureData BLSSignatureData2a = new BLSSignatureData();
        BLSSignatureData BLSSignatureData3a = new BLSSignatureData();
        BLSSignatureData1a.getSignature()[0] = BLSSignature.sign(message1.toArray(), sk1);
        BLSSignatureData1a.getSignature()[1] = BLSSignature.sign(message1.toArray(), sk1);
        BLSSignatureData2a.getSignature()[0] = BLSSignature.sign(message1.toArray(), sk2);
        BLSSignatureData2a.getSignature()[1] = BLSSignature.sign(message1.toArray(), sk2);
        BLSSignatureData3a.getSignature()[0] = BLSSignature.sign(message1.toArray(), sk3);
        BLSSignatureData3a.getSignature()[1] = BLSSignature.sign(message1.toArray(), sk3);

        BLSSignatureData1a.getMessageHash()[0] = BLSSignature.GetMessageHashAsBase64String(message1.toArray());
        BLSSignatureData1a.getMessageHash()[1] = BLSSignature.GetMessageHashAsBase64String(message1.toArray());
        BLSSignatureData2a.getMessageHash()[0] = BLSSignature.GetMessageHashAsBase64String(message1.toArray());
        BLSSignatureData2a.getMessageHash()[1] = BLSSignature.GetMessageHashAsBase64String(message1.toArray());
        BLSSignatureData3a.getMessageHash()[0] = BLSSignature.GetMessageHashAsBase64String(message1.toArray());
        BLSSignatureData3a.getMessageHash()[1] = BLSSignature.GetMessageHashAsBase64String(message1.toArray());

        prevblock.getSignatureData().put(vk1, BLSSignatureData1a);
        prevblock.getSignatureData().put(vk2, BLSSignatureData2a);
        prevblock.getSignatureData().put(vk3, BLSSignatureData3a);
        prevblock.setLeaderPublicKey(vk1);
        prevblock.setBlockProposer(vk1.toRaw());

        KademliaData kad1 = new KademliaData(new SecurityAuditProofs(address1, vk1, ecKeyPair1.getPublicKey(), signatureData1), new NettyConnectionInfo("192.168.1.106", KademliaConfiguration.PORT));
        KademliaData kad2 = new KademliaData(new SecurityAuditProofs(address2, vk2, ecKeyPair2.getPublicKey(), signatureData2), new NettyConnectionInfo("192.168.1.115", KademliaConfiguration.PORT));
        KademliaData kad3 = new KademliaData(new SecurityAuditProofs(address3, vk3, ecKeyPair3.getPublicKey(), signatureData3), new NettyConnectionInfo("192.168.1.116", KademliaConfiguration.PORT));

        committeeBlock.getStakingMap().put(new StakingData(1, BigDecimal.valueOf(100.0)), kad3);
        committeeBlock.getStakingMap().put(new StakingData(2, BigDecimal.valueOf(200.0)), kad2);
        committeeBlock.getStakingMap().put(new StakingData(3, BigDecimal.valueOf(300.0)), kad1);

        CachedLatestBlocks.getInstance().setCommitteeBlock(committeeBlock);
        CachedLatestBlocks.getInstance().setTransactionBlock(prevblock);
        transactionBlockIDatabase.save(String.valueOf(prevblock.getHeight()), prevblock);

        MemoryTreePool replica = (MemoryTreePool) TreeFactory.getMemoryTree(0).clone();
        List<BLSPublicKey> keys = committeeBlock.getStakingMap().values().stream().map(KademliaData::getAddressData).map(SecurityAuditProofs::getValidatorBlSPublicKey).collect(Collectors.toList());
        int count = 0;
        ArrayList<Map<String, RewardObject>> rewardsTotal = new ArrayList<>(3);
        CachedStartHeightRewards.getInstance().setHeight(1);
        for (int i = 2; i <= 6; i++) {
            TransactionBlock transactionBlock = new TransactionBlock();
            Transaction transaction=new RegularTransaction();
            transaction.setAmount(BigDecimal.valueOf(100));
            transaction.setAmountWithTransactionFee(BigDecimal.valueOf(10));
            transactionBlock.getTransactionList().add(transaction);
            transactionBlock.setHash(String.valueOf(i));
            transactionBlock.setHeight(i);
            transactionBlock.setGeneration(i);
            Bytes message = Bytes.wrap("Hello, world Block 1".getBytes(UTF_8));
            BLSSignatureData BLSSignatureData1 = new BLSSignatureData();
            BLSSignatureData BLSSignatureData2 = new BLSSignatureData();
            BLSSignatureData BLSSignatureData3 = new BLSSignatureData();
            BLSSignatureData1.getSignature()[0] = BLSSignature.sign(message.toArray(), sk1);
            BLSSignatureData1.getSignature()[1] = BLSSignature.sign(message.toArray(), sk1);
            BLSSignatureData2.getSignature()[0] = BLSSignature.sign(message.toArray(), sk2);
            BLSSignatureData2.getSignature()[1] = BLSSignature.sign(message.toArray(), sk2);
            BLSSignatureData3.getSignature()[0] = BLSSignature.sign(message.toArray(), sk3);
            BLSSignatureData3.getSignature()[1] = BLSSignature.sign(message.toArray(), sk3);

            BLSSignatureData1.getMessageHash()[0] = BLSSignature.GetMessageHashAsBase64String(message.toArray());
            BLSSignatureData1.getMessageHash()[1] = BLSSignature.GetMessageHashAsBase64String(message.toArray());
            BLSSignatureData2.getMessageHash()[0] = BLSSignature.GetMessageHashAsBase64String(message.toArray());
            BLSSignatureData2.getMessageHash()[1] = BLSSignature.GetMessageHashAsBase64String(message.toArray());
            BLSSignatureData3.getMessageHash()[0] = BLSSignature.GetMessageHashAsBase64String(message.toArray());
            BLSSignatureData3.getMessageHash()[1] = BLSSignature.GetMessageHashAsBase64String(message.toArray());

            transactionBlock.getSignatureData().put(vk1, BLSSignatureData1);
            transactionBlock.getSignatureData().put(vk2, BLSSignatureData2);
            transactionBlock.getSignatureData().put(vk3, BLSSignatureData3);
            transactionBlock.setLeaderPublicKey(keys.get(count));
            transactionBlock.setBlockProposer(keys.get(count).toRaw());
            if (transactionBlock.getHeight() % RewardConfiguration.BLOCK_REWARD_HEIGHT == 0) {
                RewardChainBuilder king1 = new RewardChainBuilder();
                king1.makeRequest(new Request(RequestType.EFFECTIVE_STAKE, "EFFECTIVE_STAKE"));
                king1.makeRequest(new Request(RequestType.EFFECTIVE_STAKE_RATIO, "EFFECTIVE_STAKE_RATIO"));
                king1.makeRequest(new Request(RequestType.DELEGATE_WEIGHTS_CALCULATOR, "DELEGATE_WEIGHTS_CALCULATOR"));
                king1.makeRequest(new Request(RequestType.VALIDATOR_REWARD_CALCULATOR, "VALIDATOR_REWARD_CALCULATOR"));
                king1.makeRequest(new Request(RequestType.DELEGATE_REWARD_CALCULATOR, "DELEGATE_REWARD_CALCULATOR"));
                king1.makeRequest(new Request(RequestType.REWARD_PRECISION_CALCULATOR, "REWARD_PRECISION_CALCULATOR"));
                king1.makeRequest(new Request(RequestType.REWARD_STORAGE_CALCULATOR, "REWARD_STORAGE_CALCULATOR", replica));
                if (transactionBlock.getHeight() == 3) {
                    rewardsTotal.add(0, SerializationUtils.clone(CachedRewardMapData.getInstance().getEffective_stakes_map()));
                    CachedStartHeightRewards.getInstance().setHeight(transactionBlock.getHeight());
                    CachedRewardMapData.getInstance().clearInstance();
                } else {
                    rewardsTotal.add(1, SerializationUtils.clone(CachedRewardMapData.getInstance().getEffective_stakes_map()));
                }

            }
            if (transactionBlock.getHeight() == 6) {
                CachedStartHeightRewards.getInstance().setHeight(1);
                CachedRewardMapData.getInstance().clearInstance();

                RewardChainBuilder king = new RewardChainBuilder();
                king.makeRequest(new Request(RequestType.EFFECTIVE_STAKE, "EFFECTIVE_STAKE"));
                king.makeRequest(new Request(RequestType.EFFECTIVE_STAKE_RATIO, "EFFECTIVE_STAKE_RATIO"));
                king.makeRequest(new Request(RequestType.DELEGATE_WEIGHTS_CALCULATOR, "DELEGATE_WEIGHTS_CALCULATOR"));
                king.makeRequest(new Request(RequestType.VALIDATOR_REWARD_CALCULATOR, "VALIDATOR_REWARD_CALCULATOR"));
                king.makeRequest(new Request(RequestType.DELEGATE_REWARD_CALCULATOR, "DELEGATE_REWARD_CALCULATOR"));
                king.makeRequest(new Request(RequestType.REWARD_PRECISION_CALCULATOR, "REWARD_PRECISION_CALCULATOR"));
                king.makeRequest(new Request(RequestType.REWARD_STORAGE_CALCULATOR, "REWARD_STORAGE_CALCULATOR", TreeFactory.getMemoryTree(0)));
                rewardsTotal.add(2, SerializationUtils.clone(CachedRewardMapData.getInstance().getEffective_stakes_map()));
            }
            transactionBlockIDatabase.save(String.valueOf(i), transactionBlock);
            if (count == 2) {
                count = 0;
            } else {
                count++;
            }
        }


        Map<String, TransactionBlock> res = transactionBlockIDatabase.seekFromStart();
        MemoryTreePool replica2 = (MemoryTreePool) TreeFactory.getMemoryTree(0).clone();
        for (Map.Entry<String, TransactionBlock> entry : res.entrySet()){
            BigDecimal sum=entry.getValue().getTransactionList().stream().map(Transaction::getAmountWithTransactionFee).reduce(BigDecimal.ZERO, BigDecimal::add);
            String address=CachedLatestBlocks.getInstance().getCommitteeBlock().getStakingMap().values().stream().map(KademliaData::getAddressData).filter(val->val.getValidatorBlSPublicKey().equals(entry.getValue().getLeaderPublicKey())).findFirst().get().getAddress();
            replica2.deposit(PatriciaTreeTransactionType.UNCLAIMED_FEE_REWARD,address,sum,BigDecimal.ZERO);
        }

        assertEquals(BigDecimal.valueOf(2*10).add(TreeFactory.getMemoryTree(0).getByaddress(address1).get().getUnclaimed_reward()),replica2.getByaddress(address1).get().getUnclaimed_reward());
        assertEquals(BigDecimal.valueOf(2*10).add(TreeFactory.getMemoryTree(0).getByaddress(address2).get().getUnclaimed_reward()),replica2.getByaddress(address2).get().getUnclaimed_reward());
        assertEquals(BigDecimal.valueOf(1*10).add(TreeFactory.getMemoryTree(0).getByaddress(address3).get().getUnclaimed_reward()),replica2.getByaddress(address3).get().getUnclaimed_reward());


        // Theese values must be the same with transaction timer test unclaimed rewards min the transaction fees
        assertEquals(1.190001,replica.getByaddress("ADR-AADE-ROH3-CAFV-XK5V-2NKZ-QMTG-SFMC-37W5-SHUV-2T46").get().getUnclaimed_reward().doubleValue());
        assertEquals(1.784998,replica.getByaddress("ADR-ACAO-BKTC-CFKG-VXWF-PSI2-QHWR-ZIGK-CCOL-LGJN-CM3U").get().getUnclaimed_reward().doubleValue());
        assertEquals(2.473339,replica.getByaddress("ADR-AB2W-RIQY-LSIH-CXQQ-FGRV-AINR-57RO-NFXU-IWM5-IANJ").get().getUnclaimed_reward().doubleValue());
        assertEquals(1.190001,replica.getByaddress(address3).get().getUnclaimed_reward().doubleValue());
        assertEquals(1.784998,replica.getByaddress(address2).get().getUnclaimed_reward().doubleValue());
        assertEquals(2.473339,replica.getByaddress(address1).get().getUnclaimed_reward().doubleValue());


        assertEquals(rewardsTotal.get(0).get(address1).getUnreal_reward().add(rewardsTotal.get(1).get(address1).getUnreal_reward()), rewardsTotal.get(2).get(address1).getUnreal_reward());
        assertEquals(rewardsTotal.get(0).get(address2).getUnreal_reward().add(rewardsTotal.get(1).get(address2).getUnreal_reward()), rewardsTotal.get(2).get(address2).getUnreal_reward());
        assertEquals(rewardsTotal.get(0).get(address3).getUnreal_reward().add(rewardsTotal.get(1).get(address3).getUnreal_reward()), rewardsTotal.get(2).get(address3).getUnreal_reward());

        assertEquals(rewardsTotal.get(0).get(address1).getReal_reward().add(rewardsTotal.get(1).get(address1).getReal_reward()), rewardsTotal.get(2).get(address1).getReal_reward());
        assertEquals(rewardsTotal.get(0).get(address2).getReal_reward().add(rewardsTotal.get(1).get(address2).getReal_reward()), rewardsTotal.get(2).get(address2).getReal_reward());
        assertEquals(rewardsTotal.get(0).get(address3).getReal_reward().add(rewardsTotal.get(1).get(address3).getReal_reward()), rewardsTotal.get(2).get(address3).getReal_reward());

        assertEquals(TreeFactory.getMemoryTree(0).getByaddress(address1).get().getUnclaimed_reward(), replica.getByaddress(address1).get().getUnclaimed_reward());
        assertEquals(TreeFactory.getMemoryTree(0).getByaddress(address2).get().getUnclaimed_reward(), replica.getByaddress(address2).get().getUnclaimed_reward());
        assertEquals(TreeFactory.getMemoryTree(0).getByaddress(address3).get().getUnclaimed_reward(), replica.getByaddress(address3).get().getUnclaimed_reward());

        assertEquals(TreeFactory.getMemoryTree(0).getByaddress(address1).get().getUnclaimed_reward(), replica.getByaddress(address1).get().getUnclaimed_reward());
        assertEquals(TreeFactory.getMemoryTree(0).getByaddress(address2).get().getUnclaimed_reward(), replica.getByaddress(address2).get().getUnclaimed_reward());
        assertEquals(TreeFactory.getMemoryTree(0).getByaddress(address3).get().getUnclaimed_reward(), replica.getByaddress(address3).get().getUnclaimed_reward());

        assertEquals(rewardsTotal.get(0).get(address1).getReal_reward().add(rewardsTotal.get(1).get(address1).getReal_reward()),TreeFactory.getMemoryTree(0).getByaddress(address1).get().getUnclaimed_reward());
        assertEquals(rewardsTotal.get(0).get(address2).getReal_reward().add(rewardsTotal.get(1).get(address2).getReal_reward()),TreeFactory.getMemoryTree(0).getByaddress(address2).get().getUnclaimed_reward());
        assertEquals(rewardsTotal.get(0).get(address3).getReal_reward().add(rewardsTotal.get(1).get(address3).getReal_reward()),TreeFactory.getMemoryTree(0).getByaddress(address3).get().getUnclaimed_reward());

        CachedRewardMapData.getInstance().clearInstance();
        transactionBlockIDatabase.delete_db();
        TreeFactory.ClearMemoryTree(0);
    }


    @SneakyThrows
    @Test
    public void NotebookRewardTest() {
        IDatabase<String, TransactionBlock> transactionBlockIDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(0));
        transactionBlockIDatabase.delete_db();
        transactionBlockIDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(0));

        sk1 = new BLSPrivateKey(1);
        vk1 = new BLSPublicKey(sk1);

        sk2 = new BLSPrivateKey(2);
        vk2 = new BLSPublicKey(sk2);


        String address1 = "ADR-AB2W-RIQY-LSIH-CXQQ-FGRV-AINR-57RO-NFXU-IWM5-IANJ";
        String address2 = "ADR-ACAO-BKTC-CFKG-VXWF-PSI2-QHWR-ZIGK-CCOL-LGJN-CM3U";

        char[] mnemonic1 = "sample sail jungle learn general promote task puppy own conduct green affair ".toCharArray();
        char[] mnemonic2 = "photo monitor cushion indicate civil witness orchard estate online favorite sustain extend".toCharArray();
        char[] passphrase = "p4ssphr4se".toCharArray();

        Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
        byte[] key1 = mnem.createSeed(mnemonic1, passphrase);
        byte[] key2 = mnem.createSeed(mnemonic1, passphrase);
        SecureRandom random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(key1);
        ecKeyPair1 = Keys.createEcKeyPair(random);
        random.setSeed(key2);
        ecKeyPair2 = Keys.createEcKeyPair(random);

        ECDSASignatureData signatureData1 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address1)), ecKeyPair1);
        ECDSASignatureData signatureData2 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address1)), ecKeyPair2);
        PatriciaTreeNode treeNode1 = new PatriciaTreeNode(BigDecimal.valueOf(1000), 0, BigDecimal.valueOf(270), BigDecimal.valueOf(20));
        PatriciaTreeNode treeNode2 = new PatriciaTreeNode(BigDecimal.valueOf(1000), 0, BigDecimal.valueOf(450), BigDecimal.valueOf(40));
        treeNode1.getStakingInfo().setCommissionRate(5);
        treeNode2.getStakingInfo().setCommissionRate(10);

        TreeFactory.getMemoryTree(0).store(address1, treeNode1);
        TreeFactory.getMemoryTree(0).store(address2, treeNode2);
        MemoryTreePool replica = (MemoryTreePool) TreeFactory.getMemoryTree(0).clone();

        List<Map<String, RewardObject>> rewardsTotal = new ArrayList<>();

        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.setGeneration(1);
        committeeBlock.setViewID(1);
        committeeBlock.setLeaderPublicKey(vk1);
        committeeBlock.setBlockProposer(vk1.toRaw());

        TransactionBlock prevblock1 = new TransactionBlock();
        prevblock1.setHeight(1);
        prevblock1.setHash("hash");
        prevblock1.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        Bytes message1 = Bytes.wrap("Hello, world Block 1".getBytes(UTF_8));
        BLSSignatureData BLSSignatureData1a = new BLSSignatureData();
        BLSSignatureData BLSSignatureData3a = new BLSSignatureData();
        BLSSignatureData1a.getSignature()[0] = BLSSignature.sign(message1.toArray(), sk1);
        BLSSignatureData1a.getSignature()[1] = BLSSignature.sign(message1.toArray(), sk1);
        BLSSignatureData3a.getSignature()[0] = BLSSignature.sign(message1.toArray(), sk2);
        BLSSignatureData3a.getSignature()[1] = BLSSignature.sign(message1.toArray(), sk2);


        BLSSignatureData1a.getMessageHash()[0] = BLSSignature.GetMessageHashAsBase64String(message1.toArray());
        BLSSignatureData1a.getMessageHash()[1] = BLSSignature.GetMessageHashAsBase64String(message1.toArray());
        BLSSignatureData3a.getMessageHash()[0] = BLSSignature.GetMessageHashAsBase64String(message1.toArray());
        BLSSignatureData3a.getMessageHash()[1] = BLSSignature.GetMessageHashAsBase64String(message1.toArray());

        prevblock1.getSignatureData().put(vk1, BLSSignatureData1a);
        prevblock1.getSignatureData().put(vk2, BLSSignatureData3a);
        prevblock1.setLeaderPublicKey(vk1);
        prevblock1.setBlockProposer(vk1.toRaw());

        TransactionBlock prevblock2 = new TransactionBlock();
        prevblock2.setHeight(2);
        prevblock2.setHash("hash2");
        prevblock2.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        BLSSignatureData BLSSignatureData2a = new BLSSignatureData();
        BLSSignatureData BLSSignatureData4a = new BLSSignatureData();
        BLSSignatureData2a.getSignature()[0] = BLSSignature.sign(message1.toArray(), sk1);
        BLSSignatureData2a.getSignature()[1] = BLSSignature.sign(message1.toArray(), sk1);
        BLSSignatureData4a.getSignature()[0] = BLSSignature.sign(message1.toArray(), sk2);
        BLSSignatureData4a.getSignature()[1] = BLSSignature.sign(message1.toArray(), sk2);


        BLSSignatureData2a.getMessageHash()[0] = BLSSignature.GetMessageHashAsBase64String(message1.toArray());
        BLSSignatureData2a.getMessageHash()[1] = BLSSignature.GetMessageHashAsBase64String(message1.toArray());
        BLSSignatureData4a.getMessageHash()[0] = BLSSignature.GetMessageHashAsBase64String(message1.toArray());
        BLSSignatureData4a.getMessageHash()[1] = BLSSignature.GetMessageHashAsBase64String(message1.toArray());

        prevblock2.getSignatureData().put(vk1, BLSSignatureData2a);
        prevblock2.getSignatureData().put(vk2, BLSSignatureData4a);
        prevblock2.setLeaderPublicKey(vk2);
        prevblock2.setBlockProposer(vk2.toRaw());

        KademliaData kad1 = new KademliaData(new SecurityAuditProofs(address1, vk1, ecKeyPair1.getPublicKey(), signatureData1), new NettyConnectionInfo("192.168.1.106", KademliaConfiguration.PORT));
        KademliaData kad2 = new KademliaData(new SecurityAuditProofs(address2, vk2, ecKeyPair2.getPublicKey(), signatureData2), new NettyConnectionInfo("192.168.1.116", KademliaConfiguration.PORT));

        committeeBlock.getStakingMap().put(new StakingData(1, BigDecimal.valueOf(270.0)), kad1);
        committeeBlock.getStakingMap().put(new StakingData(2, BigDecimal.valueOf(490.0)), kad2);


        CachedLatestBlocks.getInstance().setCommitteeBlock(committeeBlock);
        transactionBlockIDatabase.save(String.valueOf(prevblock1.getHeight()), prevblock1);
        CachedStartHeightRewards.getInstance().setHeight(1);
        CachedStartHeightRewards.getInstance().setRewardsCommitteeEnabled(true);
        RewardChainBuilder king = new RewardChainBuilder();
        king.makeRequest(new Request(RequestType.EFFECTIVE_STAKE, "EFFECTIVE_STAKE"));
        king.makeRequest(new Request(RequestType.EFFECTIVE_STAKE_RATIO, "EFFECTIVE_STAKE_RATIO"));
        king.makeRequest(new Request(RequestType.DELEGATE_WEIGHTS_CALCULATOR, "DELEGATE_WEIGHTS_CALCULATOR"));
        king.makeRequest(new Request(RequestType.VALIDATOR_REWARD_CALCULATOR, "VALIDATOR_REWARD_CALCULATOR"));
        king.makeRequest(new Request(RequestType.DELEGATE_REWARD_CALCULATOR, "DELEGATE_REWARD_CALCULATOR"));
        king.makeRequest(new Request(RequestType.REWARD_PRECISION_CALCULATOR, "REWARD_PRECISION_CALCULATOR"));
        king.makeRequest(new Request(RequestType.REWARD_STORAGE_CALCULATOR, "REWARD_STORAGE_CALCULATOR", TreeFactory.getMemoryTree(0)));

        rewardsTotal.add(0, SerializationUtils.clone(CachedRewardMapData.getInstance().getEffective_stakes_map()));
        CachedRewardMapData.getInstance().clearInstance();
        transactionBlockIDatabase.save(String.valueOf(prevblock2.getHeight()), prevblock2);
        CachedStartHeightRewards.getInstance().setHeight(2);
        CachedStartHeightRewards.getInstance().setRewardsCommitteeEnabled(false);

        RewardChainBuilder king1 = new RewardChainBuilder();
        king1.makeRequest(new Request(RequestType.EFFECTIVE_STAKE, "EFFECTIVE_STAKE"));
        king1.makeRequest(new Request(RequestType.EFFECTIVE_STAKE_RATIO, "EFFECTIVE_STAKE_RATIO"));
        king1.makeRequest(new Request(RequestType.DELEGATE_WEIGHTS_CALCULATOR, "DELEGATE_WEIGHTS_CALCULATOR"));
        king1.makeRequest(new Request(RequestType.VALIDATOR_REWARD_CALCULATOR, "VALIDATOR_REWARD_CALCULATOR"));
        king1.makeRequest(new Request(RequestType.DELEGATE_REWARD_CALCULATOR, "DELEGATE_REWARD_CALCULATOR"));
        king1.makeRequest(new Request(RequestType.REWARD_PRECISION_CALCULATOR, "REWARD_PRECISION_CALCULATOR"));
        king1.makeRequest(new Request(RequestType.REWARD_STORAGE_CALCULATOR, "REWARD_STORAGE_CALCULATOR", TreeFactory.getMemoryTree(0)));
        rewardsTotal.add(1, SerializationUtils.clone(CachedRewardMapData.getInstance().getEffective_stakes_map()));

        CachedRewardMapData.getInstance().clearInstance();
        CachedStartHeightRewards.getInstance().setHeight(1);
        CachedStartHeightRewards.getInstance().setRewardsCommitteeEnabled(true);
        RewardChainBuilder king2 = new RewardChainBuilder();
        king2.makeRequest(new Request(RequestType.EFFECTIVE_STAKE, "EFFECTIVE_STAKE"));
        king2.makeRequest(new Request(RequestType.EFFECTIVE_STAKE_RATIO, "EFFECTIVE_STAKE_RATIO"));
        king2.makeRequest(new Request(RequestType.DELEGATE_WEIGHTS_CALCULATOR, "DELEGATE_WEIGHTS_CALCULATOR"));
        king2.makeRequest(new Request(RequestType.VALIDATOR_REWARD_CALCULATOR, "VALIDATOR_REWARD_CALCULATOR"));
        king2.makeRequest(new Request(RequestType.DELEGATE_REWARD_CALCULATOR, "DELEGATE_REWARD_CALCULATOR"));
        king2.makeRequest(new Request(RequestType.REWARD_PRECISION_CALCULATOR, "REWARD_PRECISION_CALCULATOR"));
        king2.makeRequest(new Request(RequestType.REWARD_STORAGE_CALCULATOR, "REWARD_STORAGE_CALCULATOR", replica));

        rewardsTotal.add(2, SerializationUtils.clone(CachedRewardMapData.getInstance().getEffective_stakes_map()));
        CachedRewardMapData.getInstance().clearInstance();

        assertEquals(rewardsTotal.get(0).get(address1).getReal_reward().add(rewardsTotal.get(1).get(address1).getReal_reward()), rewardsTotal.get(2).get(address1).getReal_reward());
        assertEquals(rewardsTotal.get(0).get(address2).getReal_reward().add(rewardsTotal.get(1).get(address2).getReal_reward()), rewardsTotal.get(2).get(address2).getReal_reward());

        assertEquals(rewardsTotal.get(0).get(address1).getUnreal_reward().add(rewardsTotal.get(1).get(address1).getUnreal_reward()), rewardsTotal.get(2).get(address1).getUnreal_reward());
        assertEquals(rewardsTotal.get(0).get(address2).getUnreal_reward().add(rewardsTotal.get(1).get(address2).getUnreal_reward()), rewardsTotal.get(2).get(address2).getUnreal_reward());

        assertEquals(rewardsTotal.get(0).get(address1).getReal_reward().add(rewardsTotal.get(1).get(address1).getReal_reward()), replica.getByaddress(address1).get().getUnclaimed_reward());
        assertEquals(rewardsTotal.get(0).get(address2).getReal_reward().add(rewardsTotal.get(1).get(address2).getReal_reward()), replica.getByaddress(address2).get().getUnclaimed_reward());

        assertEquals(TreeFactory.getMemoryTree(0).getByaddress(address1).get().getUnclaimed_reward(), replica.getByaddress(address1).get().getUnclaimed_reward());
        assertEquals(TreeFactory.getMemoryTree(0).getByaddress(address2).get().getUnclaimed_reward(), replica.getByaddress(address2).get().getUnclaimed_reward());

        assertEquals(rewardsTotal.get(0).get(address1).getReal_reward().add(rewardsTotal.get(1).get(address1).getReal_reward()), TreeFactory.getMemoryTree(0).getByaddress(address1).get().getUnclaimed_reward());
        assertEquals(rewardsTotal.get(0).get(address2).getReal_reward().add(rewardsTotal.get(1).get(address2).getReal_reward()), TreeFactory.getMemoryTree(0).getByaddress(address2).get().getUnclaimed_reward());

        CachedRewardMapData.getInstance().clearInstance();
        transactionBlockIDatabase.delete_db();
        TreeFactory.ClearMemoryTree(0);
    }
}
