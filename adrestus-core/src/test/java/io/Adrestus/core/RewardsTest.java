package io.Adrestus.core;

import io.Adrestus.MemoryTreePool;
import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.config.KademliaConfiguration;
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
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import io.distributedLedger.ZoneDatabaseFactory;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.Test;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

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
    @SneakyThrows
    @Test
    public void test() throws CloneNotSupportedException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, MnemonicException {
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

        String address ="ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        String address1 = "ADR-GBIV-HG2J-27P5-BNVN-MLN6-DL5V-M3YZ-PKEJ-CFFG-FK4L";
        String address0 = WalletAddress.generate_address((byte) version, ecKeyPair3.getPublicKey());
        ECDSASignatureData signatureData1 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address)), ecKeyPair1);
        ECDSASignatureData signatureData2 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address1)), ecKeyPair2);
        ECDSASignatureData signatureData3 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address1)), ecKeyPair3);


        String address2 = "ADR-GBZX-XXCW-LWJC-J7RZ-Q6BJ-RFBA-J5WU-NBAG-4RL7-7G6Z";
        String address3 = "ADR-GD3G-DK4I-DKM2-IQSB-KBWL-HWRV-BBQA-MUAS-MGXA-5QPP";
        String address4 = "ADR-GC2I-WBAW-IKJE-BWFC-ML6T-BNOC-7XOU-IQ74-BJ5L-WP7G";
        String address5 = "ADR-HC2I-WBAW-IKJE-BWFC-ML6T-BNOC-7XOU-IQ74-BJ1L-WP7G";

        PatriciaTreeNode treeNode2 = new PatriciaTreeNode(0, 1,0,0,0);
        PatriciaTreeNode treeNode3 = new PatriciaTreeNode(0, 6,0,0,0);
        PatriciaTreeNode treeNode4 = new PatriciaTreeNode(0, 7,0,0,0);
        PatriciaTreeNode treeNode5 = new PatriciaTreeNode(0, 1,0,0,0);
        TreeFactory.getMemoryTree(0).store(address2, treeNode2);
        TreeFactory.getMemoryTree(0).store(address3, treeNode3);
        TreeFactory.getMemoryTree(0).store(address4, treeNode4);
        TreeFactory.getMemoryTree(0).store(address5, treeNode5);

        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.setGeneration(1);
        committeeBlock.getStakingMap().put(new StakingData(1, 490), new KademliaData(new SecurityAuditProofs(address, vk1, ecKeyPair1.getPublicKey(), signatureData1), new NettyConnectionInfo("192.168.1.106", KademliaConfiguration.PORT)));
        committeeBlock.getStakingMap().put(new StakingData(2, 270), new KademliaData(new SecurityAuditProofs(address1, vk2, ecKeyPair2.getPublicKey(), signatureData2), new NettyConnectionInfo("192.168.1.116", KademliaConfiguration.PORT)));
        committeeBlock.getStakingMap().put(new StakingData(2, 120), new KademliaData(new SecurityAuditProofs(address0, vk3, ecKeyPair3.getPublicKey(), signatureData3), new NettyConnectionInfo("192.168.1.119", KademliaConfiguration.PORT)));
        committeeBlock.setCommitteeProposer(new int[committeeBlock.getStakingMap().size()]);
        committeeBlock.setBlockProposer(vk1.toRaw());
        committeeBlock.setLeaderPublicKey(vk1);
        CachedLatestBlocks.getInstance().setCommitteeBlock(committeeBlock);
        CachedStartHeightRewards.getInstance().setRewardsCommitteeEnabled(false);


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



        PatriciaTreeNode treeNode = new PatriciaTreeNode(0, 1,490,40,0);
        treeNode.getStakingInfo().setCommissionRate(10);
        PatriciaTreeNode treeNode1 = new PatriciaTreeNode(0, 1,270,30,0);
        treeNode1.getStakingInfo().setCommissionRate(10);
        PatriciaTreeNode treeNode0 = new PatriciaTreeNode(0, 1,120,10,0);
        treeNode0.getStakingInfo().setCommissionRate(10);
        TreeFactory.getMemoryTree(0).store(address, treeNode);
        TreeFactory.getMemoryTree(0).getByaddress(address).get().getDelegation().put(address2,100.0);
        TreeFactory.getMemoryTree(0).getByaddress(address).get().getDelegation().put(address3,100.0);
        TreeFactory.getMemoryTree(0).getByaddress(address).get().getDelegation().put(address4,100.0);
        TreeFactory.getMemoryTree(0).getByaddress(address).get().getDelegation().put(address5,100.0);
        PatriciaTreeNode treeNodeClone= (PatriciaTreeNode) treeNode.clone();
        TreeFactory.getMemoryTree(0).store(address1, treeNode1);
        TreeFactory.getMemoryTree(0).getByaddress(address1).get().getDelegation().put(address2,200.0);
        TreeFactory.getMemoryTree(0).getByaddress(address1).get().getDelegation().put(address3,200.0);
        TreeFactory.getMemoryTree(0).getByaddress(address1).get().getDelegation().put(address4,200.0);
        TreeFactory.getMemoryTree(0).getByaddress(address1).get().getDelegation().put(address5,200.0);
        TreeFactory.getMemoryTree(0).store(address0, treeNode0);

        MemoryTreePool cloned_tree = (MemoryTreePool) TreeFactory.getMemoryTree(0).clone();
        RewardChainBuilder king = new RewardChainBuilder();
        king.makeRequest(new Request(RequestType.EFFECTIVE_STAKE, "EFFECTIVE_STAKE"));
        king.makeRequest(new Request(RequestType.EFFECTIVE_STAKE_RATIO, "EFFECTIVE_STAKE_RATIO"));
        king.makeRequest(new Request(RequestType.DELEGATE_WEIGHTS_CALCULATOR, "DELEGATE_WEIGHTS_CALCULATOR"));
        king.makeRequest(new Request(RequestType.VALIDATOR_REWARD_CALCULATOR, "VALIDATOR_REWARD_CALCULATOR"));
        king.makeRequest(new Request(RequestType.DELEGATE_REWARD_CALCULATOR, "DELEGATE_REWARD_CALCULATOR"));
        king.makeRequest(new Request(RequestType.REWARD_STORAGE_CALCULATOR, "REWARD_STORAGE_CALCULATOR",TreeFactory.getMemoryTree(0)));
        CachedRewardMapData.getInstance().clearInstance();

        RewardChainBuilder king2 = new RewardChainBuilder();
        king2.makeRequest(new Request(RequestType.EFFECTIVE_STAKE, "EFFECTIVE_STAKE"));
        king2.makeRequest(new Request(RequestType.EFFECTIVE_STAKE_RATIO, "EFFECTIVE_STAKE_RATIO"));
        king2.makeRequest(new Request(RequestType.DELEGATE_WEIGHTS_CALCULATOR, "DELEGATE_WEIGHTS_CALCULATOR"));
        king2.makeRequest(new Request(RequestType.VALIDATOR_REWARD_CALCULATOR, "VALIDATOR_REWARD_CALCULATOR"));
        king2.makeRequest(new Request(RequestType.DELEGATE_REWARD_CALCULATOR, "DELEGATE_REWARD_CALCULATOR"));
        king2.makeRequest(new Request(RequestType.REWARD_STORAGE_CALCULATOR, "REWARD_STORAGE_CALCULATOR",cloned_tree));

        Map<String,RewardObject> maps=CachedRewardMapData.getInstance().getEffective_stakes_map();


        assertEquals(treeNode,treeNodeClone);
        assertNotNull(CachedRewardMapData.getInstance().getEffective_stakes_map().get(address));
        assertNotNull(CachedRewardMapData.getInstance().getEffective_stakes_map().get(address1));

        assertEquals(490,CachedRewardMapData.getInstance().getEffective_stakes_map().get(address).getEffective_stake());
        assertEquals(0.5568181818181818,CachedRewardMapData.getInstance().getEffective_stakes_map().get(address).getEffective_stake_ratio());
        assertEquals(7.015909090909091,CachedRewardMapData.getInstance().getEffective_stakes_map().get(address).getUnreal_reward());

        //it has some divergent cause it caclulates leader transaction/committe block rewards its normal
        assertEquals(0.8694,CachedRewardMapData.getInstance().getEffective_stakes_map().get(address).getReal_reward());

        assertEquals(270,CachedRewardMapData.getInstance().getEffective_stakes_map().get(address1).getEffective_stake());
        assertEquals(0.3068181818181818,CachedRewardMapData.getInstance().getEffective_stakes_map().get(address1).getEffective_stake_ratio());
        assertEquals(3.865909090909091,CachedRewardMapData.getInstance().getEffective_stakes_map().get(address1).getUnreal_reward());
        assertEquals(0.42954545454545456,CachedRewardMapData.getInstance().getEffective_stakes_map().get(address1).getReal_reward());

        assertEquals(120,CachedRewardMapData.getInstance().getEffective_stakes_map().get(address0).getEffective_stake());
        assertEquals(0.13636363636363635,CachedRewardMapData.getInstance().getEffective_stakes_map().get(address0).getEffective_stake_ratio());
        assertEquals(0.859090909090909,CachedRewardMapData.getInstance().getEffective_stakes_map().get(address0).getUnreal_reward());
        assertEquals(0.07159090909090908,CachedRewardMapData.getInstance().getEffective_stakes_map().get(address0).getReal_reward());


        //not stakers delegators here
        assertEquals(0.20408163265306123,CachedRewardMapData.getInstance().getEffective_stakes_map().get(address).getDelegate_stake().get(address2).getWeights());
        assertEquals(0.20408163265306123,CachedRewardMapData.getInstance().getEffective_stakes_map().get(address).getDelegate_stake().get(address4).getWeights());


        assertEquals(0.7407407407407407,CachedRewardMapData.getInstance().getEffective_stakes_map().get(address1).getDelegate_stake().get(address2).getWeights());
        assertEquals(0.7407407407407407,CachedRewardMapData.getInstance().getEffective_stakes_map().get(address1).getDelegate_stake().get(address3).getWeights());
        assertEquals(0.7407407407407407,CachedRewardMapData.getInstance().getEffective_stakes_map().get(address1).getDelegate_stake().get(address4).getWeights());

        //treemap asserts stakers
        assertEquals(0.8694,TreeFactory.getMemoryTree(0).getByaddress(address).get().getUnclaimed_reward());
        assertEquals(0.42954545454545456,TreeFactory.getMemoryTree(0).getByaddress(address1).get().getUnclaimed_reward());
        assertEquals(0.07159090909090908,TreeFactory.getMemoryTree(0).getByaddress(address0).get().getUnclaimed_reward());

        //treemap asserts delegators
        assertEquals(4.295454545454547,TreeFactory.getMemoryTree(0).getByaddress(address2).get().getUnclaimed_reward());
        assertEquals(4.295454545454547,TreeFactory.getMemoryTree(0).getByaddress(address5).get().getUnclaimed_reward());
        assertEquals(4.295454545454547,TreeFactory.getMemoryTree(0).getByaddress(address3).get().getUnclaimed_reward());
        assertEquals(4.295454545454547,TreeFactory.getMemoryTree(0).getByaddress(address4).get().getUnclaimed_reward());

        assertEquals(cloned_tree.getRootHash(), TreeFactory.getMemoryTree(0).getRootHash());

        CachedRewardMapData.getInstance().clearInstance();
        transactionBlockIDatabase.delete_db();
    }
}
