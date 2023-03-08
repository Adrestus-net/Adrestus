package io.Adrestus.core;

import com.google.common.reflect.TypeToken;
import io.Adrestus.MemoryTreePool;
import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.config.KademliaConfiguration;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedLeaderIndex;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.SecurityAuditProofs;
import io.Adrestus.crypto.WalletAddress;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.crypto.elliptic.ECDSASignatureData;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import io.Adrestus.crypto.elliptic.mapper.StakingData;
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import io.Adrestus.mapper.MemoryTreePoolSerializer;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.util.SerializationUtil;
import io.distributedLedger.*;
import io.vavr.control.Option;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TreemapSerializationTest {
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
        SecureRandom random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(key1);
        ecKeyPair1 = Keys.createEcKeyPair(random);
        random.setSeed(key2);
        ecKeyPair2 = Keys.createEcKeyPair(random);

        address1 = WalletAddress.generate_address((byte) version, ecKeyPair1.getPublicKey());
        address2 = WalletAddress.generate_address((byte) version, ecKeyPair2.getPublicKey());

        ECDSASignatureData signatureData1 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address1)), ecKeyPair1);
        ECDSASignatureData signatureData2 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address2)), ecKeyPair2);

        TreeFactory.getMemoryTree(0).store(address1, new PatriciaTreeNode(3000, 0));
        TreeFactory.getMemoryTree(0).store(address2, new PatriciaTreeNode(3000, 0));

        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.getHeaderData().setTimestamp("2022-11-18 15:01:29.304");
        committeeBlock.getStructureMap().get(0).put(vk1, "192.168.1.106");
        committeeBlock.getStructureMap().get(0).put(vk2, "192.168.1.116");

        committeeBlock.getStakingMap().put(new StakingData(1, 10.0), new KademliaData(new SecurityAuditProofs(address1, vk1, ecKeyPair1.getPublicKey(), signatureData1), new NettyConnectionInfo("192.168.1.106", KademliaConfiguration.PORT)));
        committeeBlock.getStakingMap().put(new StakingData(2, 13.0), new KademliaData(new SecurityAuditProofs(address2, vk2, ecKeyPair2.getPublicKey(), signatureData2), new NettyConnectionInfo("192.168.1.116", KademliaConfiguration.PORT)));

        CachedLatestBlocks.getInstance().setCommitteeBlock(committeeBlock);
        CachedLeaderIndex.getInstance().setCommitteePositionLeader(0);
    }

    @Test
    public void test() {
        IDatabase<String, CommitteeBlock> database = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);
        // database.delete_db();
        //  CachedLatestBlocks.getInstance().getCommitteeBlock().getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        CachedLatestBlocks.getInstance().getCommitteeBlock().setDifficulty(112);
        CachedLatestBlocks.getInstance().getCommitteeBlock().setHash("hash");
        CachedLatestBlocks.getInstance().getCommitteeBlock().setGeneration(0);
        CachedLatestBlocks.getInstance().getCommitteeBlock().setHeight(0);
        database.save(CachedLatestBlocks.getInstance().getCommitteeBlock().getHash(), CachedLatestBlocks.getInstance().getCommitteeBlock());

        int finish = database.findDBsize();
        Map<String, CommitteeBlock> block_entries = database.seekBetweenRange(0, finish);

        database.delete_db();
    }

    @Test
    public void treemap_database_test() throws Exception {
        IDatabase<String, byte[]> tree_datasbase = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getPatriciaTreeZoneInstance(0));

        Type fluentType = new TypeToken<MemoryTreePool>() {
        }.getType();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        SerializationUtil valueMapper = new SerializationUtil<>(fluentType, list);

        String address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        PatriciaTreeNode treeNode = new PatriciaTreeNode(2, 1);
        TreeFactory.getMemoryTree(1).store(address, treeNode);
        MemoryTreePool m = (MemoryTreePool) TreeFactory.getMemoryTree(1);

        //m.getByaddress(address);
        //use only special
        byte[] bt = valueMapper.encode_special(m, SerializationUtils.serialize(m).length);
        tree_datasbase.save("patricia_tree_root", bt);
        MemoryTreePool copy = (MemoryTreePool) valueMapper.decode(tree_datasbase.findByKey("patricia_tree_root").get());


        //copy.store(address, treeNode);
        Option<PatriciaTreeNode> pat = copy.getByaddress(address);

        assertEquals(treeNode, pat.get());
        assertEquals(m, copy);
    }
}
