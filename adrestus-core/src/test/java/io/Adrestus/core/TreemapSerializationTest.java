package io.Adrestus.core;

import com.google.common.reflect.TypeToken;
import io.Adrestus.IMemoryTreePool;
import io.Adrestus.MemoryTreePool;
import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.Trie.PatriciaTreeTransactionType;
import io.Adrestus.Trie.StorageInfo;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.config.KademliaConfiguration;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedLeaderIndex;
import io.Adrestus.core.mapper.SerializerCoreFury;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.SecurityAuditProofs;
import io.Adrestus.crypto.WalletAddress;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.crypto.elliptic.ECDSASignatureData;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import io.Adrestus.crypto.elliptic.mapper.BigDecimalSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomFurySerializer;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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
        SerializerCoreFury.getInstance().getFury();
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

        TreeFactory.getMemoryTree(0).store(address1, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));
        TreeFactory.getMemoryTree(0).store(address2, new PatriciaTreeNode(BigDecimal.valueOf(3000), 0));

        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.getHeaderData().setTimestamp("2022-11-18 15:01:29.304");
        committeeBlock.getStructureMap().get(0).put(vk1, "192.168.1.106");
        committeeBlock.getStructureMap().get(0).put(vk2, "192.168.1.116");

        committeeBlock.getStakingMap().put(new StakingData(1, BigDecimal.valueOf(10.0)), new KademliaData(new SecurityAuditProofs(address1, vk1, ecKeyPair1.getPublicKey(), signatureData1), new NettyConnectionInfo("192.168.1.106", KademliaConfiguration.PORT)));
        committeeBlock.getStakingMap().put(new StakingData(2, BigDecimal.valueOf(13.0)), new KademliaData(new SecurityAuditProofs(address2, vk2, ecKeyPair2.getPublicKey(), signatureData2), new NettyConnectionInfo("192.168.1.116", KademliaConfiguration.PORT)));

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
        database.save(String.valueOf(CachedLatestBlocks.getInstance().getCommitteeBlock().getGeneration()), CachedLatestBlocks.getInstance().getCommitteeBlock());

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
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        SerializationUtil valueMapper = new SerializationUtil<>(fluentType, list);

        String address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        PatriciaTreeNode treeNode = new PatriciaTreeNode(BigDecimal.valueOf(2), 1);
        TreeFactory.getMemoryTree(1).store(address, treeNode);
        MemoryTreePool m = (MemoryTreePool) TreeFactory.getMemoryTree(1);

        //m.getByaddress(address);
        //use only special
        byte[] bt = valueMapper.encode_special(m, CustomFurySerializer.getInstance().getFury().serialize(m).length);
        tree_datasbase.save("patricia_tree_root", bt);
        MemoryTreePool copy = (MemoryTreePool) valueMapper.decode(tree_datasbase.findByKey("patricia_tree_root").get());


        //copy.store(address, treeNode);
        Option<PatriciaTreeNode> pat = copy.getByaddress(address);

        assertEquals(treeNode, pat.get());
        assertEquals(m, copy);

        tree_datasbase.delete_db();
    }

    @Test
    public void treemap_database_test1() throws Exception {
        IDatabase<String, byte[]> tree_datasbase = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getPatriciaTreeZoneInstance(0));

        Type fluentType = new TypeToken<MemoryTreePool>() {
        }.getType();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        SerializationUtil valueMapper = new SerializationUtil<>(fluentType, list);

        String address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        PatriciaTreeNode treeNode = new PatriciaTreeNode(BigDecimal.valueOf(2), 1);
        PatriciaTreeNode treeNode1 = new PatriciaTreeNode(BigDecimal.valueOf(12), 2);
        PatriciaTreeNode treeNode3 = new PatriciaTreeNode(BigDecimal.valueOf(123), 2);
        PatriciaTreeNode treeNode4 = new PatriciaTreeNode(BigDecimal.valueOf(15), 2);
        TreeFactory.getMemoryTree(1).store(address, treeNode);
        TreeFactory.getMemoryTree(0).store(address, treeNode1);
        TreeFactory.getMemoryTree(2).store(address, treeNode3);
        TreeFactory.getMemoryTree(3).store(address, treeNode4);
        IMemoryTreePool m = TreeFactory.getMemoryTree(3);
        IMemoryTreePool m11 = TreeFactory.getMemoryTree(1);
        IMemoryTreePool m12 = TreeFactory.getMemoryTree(0);
        IMemoryTreePool m13 = TreeFactory.getMemoryTree(2);
        TreeFactory.setMemoryTree(m, 0);
        IMemoryTreePool m2 = TreeFactory.getMemoryTree(3);
        IMemoryTreePool m21 = TreeFactory.getMemoryTree(1);
        IMemoryTreePool m31 = TreeFactory.getMemoryTree(0);
        IMemoryTreePool m41 = TreeFactory.getMemoryTree(2);
        //m.getByaddress(address);
        //use only special
        for (int i = 0; i < 100000; i++) {
            char[] chars = new char[1000];
            Arrays.fill(chars, String.valueOf(i).charAt(0));
            String str = new String(chars);
            PatriciaTreeNode patriciaTreeNode = new PatriciaTreeNode(BigDecimal.valueOf(2), 1, BigDecimal.valueOf(23453), BigDecimal.valueOf(243534), BigDecimal.valueOf(234534));
            TreeFactory.getMemoryTree(0).store(str, patriciaTreeNode);
        }

        long start = System.currentTimeMillis();
        byte[] bt = valueMapper.encode_special(TreeFactory.getMemoryTree(0), CustomFurySerializer.getInstance().getFury().serialize(TreeFactory.getMemoryTree(0)).length);
        tree_datasbase.save("patricia_tree_root", bt);
        MemoryTreePool copy = (MemoryTreePool) valueMapper.decode(tree_datasbase.findByKey("patricia_tree_root").get());
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        System.out.println("elapsed: " + timeElapsed);

        //copy.store(address, treeNode);
        Option<PatriciaTreeNode> pat = copy.getByaddress(address);

        assertEquals(treeNode4, pat.get());
        assertEquals(TreeFactory.getMemoryTree(0), copy);
        assertEquals(15, TreeFactory.getMemoryTree(3).getByaddress(address).get().getAmount().doubleValue());
        assertEquals(15, TreeFactory.getMemoryTree(0).getByaddress(address).get().getAmount().doubleValue());
        assertEquals(2, TreeFactory.getMemoryTree(1).getByaddress(address).get().getAmount().doubleValue());
        assertEquals(123, TreeFactory.getMemoryTree(2).getByaddress(address).get().getAmount().doubleValue());
        tree_datasbase.delete_db();
    }

//    @Test
//    public void treemap_database_Fury_Serialization() throws Exception {
//      IDatabase<String, byte[]> tree_datasbase = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getPatriciaTreeZoneInstance(0));
//      TreeFactory.ClearMemoryTree(0);
//
//
//        String address1 = "1";
//        String address2 = "2";
//        String address3 = "3";
//        String address4 = "4";
//        PatriciaTreeNode treeNode = new PatriciaTreeNode(BigDecimal.valueOf(2), 1);
//        PatriciaTreeNode treeNode1 = new PatriciaTreeNode(BigDecimal.valueOf(12), 2);
//        PatriciaTreeNode treeNode3 = new PatriciaTreeNode(BigDecimal.valueOf(123), 2);
//        PatriciaTreeNode treeNode4 = new PatriciaTreeNode(BigDecimal.valueOf(15), 2);
//        TreeFactory.getMemoryTree(0).store(address1, treeNode);
//        TreeFactory.getMemoryTree(0).store(address2, treeNode1);
//        TreeFactory.getMemoryTree(0).store(address3, treeNode3);
//        TreeFactory.getMemoryTree(0).store(address4, treeNode4);
//
//
//        TreeFactory.setMemoryTree((IMemoryTreePool) TreeFactory.getMemoryTree(0).clone(), 1);
//
//        byte[] bt = CustomFurySerializer.getInstance().getFury().serialize(TreeFactory.getMemoryTree(0));
//        tree_datasbase.save("patricia_tree_root", bt);
//        MemoryTreePool copy2 = (MemoryTreePool) CustomFurySerializer.getInstance().getFury().deserialize(bt);
//        MemoryTreePool copy = (MemoryTreePool) CustomFurySerializer.getInstance().getFury().deserialize(tree_datasbase.findByKey("patricia_tree_root").get());
//
//        for (int i = 0; i < 100000; i++) {
//            char[] chars = new char[1000];
//            Arrays.fill(chars, String.valueOf(i).charAt(0));
//            String str = new String(chars);
//            PatriciaTreeNode patriciaTreeNode=new PatriciaTreeNode(BigDecimal.valueOf(2), 1,BigDecimal.valueOf(23453),BigDecimal.valueOf(243534),BigDecimal.valueOf(234534));
//            TreeFactory.getMemoryTree(0).store(str, patriciaTreeNode);
//        }
//
//        long start = System.currentTimeMillis();
//        byte[] bt1 = CustomFurySerializer.getInstance().getFury().serialize(TreeFactory.getMemoryTree(0));
//        MemoryTreePool clone_bt = (MemoryTreePool) CustomFurySerializer.getInstance().getFury().deserialize(bt1);
//        long finish = System.currentTimeMillis();
//        long timeElapsed = finish - start;
//        System.out.println("elapsed: " + timeElapsed);
//        assertEquals(copy, copy2);
////
////        assertEquals(treeNode4, pat.get());
////        assertEquals(15, copy2.getByaddress(address).get().getAmount().doubleValue());
////        assertEquals(15, copy2.getByaddress(address).get().getAmount().doubleValue());
//
//        System.out.println(TreeFactory.getMemoryTree(0).getByaddress(address1).get().toString()+" "+TreeFactory.getMemoryTree(0).getRootHash());
//        System.out.println(copy2.getByaddress(address1).get().toString()+" "+copy2.getRootHash());
//
//        assertEquals(TreeFactory.getMemoryTree(0).getByaddress(address1).get(), copy2.getByaddress(address1).get());
//        assertEquals(TreeFactory.getMemoryTree(0).getByaddress(address2).get(), copy2.getByaddress(address2).get());
//        assertEquals(TreeFactory.getMemoryTree(0).getByaddress(address3).get(), copy2.getByaddress(address3).get());
//        assertEquals(TreeFactory.getMemoryTree(0).getByaddress(address4).get(), copy2.getByaddress(address4).get());
//
//        assertEquals(2, copy2.getByaddress(address1).get().getAmount().doubleValue());
//        assertEquals(12, copy2.getByaddress(address2).get().getAmount().doubleValue());
//        assertEquals(123, copy2.getByaddress(address3).get().getAmount().doubleValue());
//        assertEquals(15, copy2.getByaddress(address4).get().getAmount().doubleValue());
//
//        assertEquals(2, TreeFactory.getMemoryTree(0).getByaddress(address1).get().getAmount().doubleValue());
//        assertEquals(12, TreeFactory.getMemoryTree(0).getByaddress(address2).get().getAmount().doubleValue());
//        assertEquals(123, TreeFactory.getMemoryTree(0).getByaddress(address3).get().getAmount().doubleValue());
//        assertEquals(15, TreeFactory.getMemoryTree(0).getByaddress(address4).get().getAmount().doubleValue());
//
//        MatcherAssert.assertThat(TreeFactory.getMemoryTree(0).getPatriciaTreeImp(), Matchers.equalTo(copy.getPatriciaTreeImp()));
//        assertEquals(TreeFactory.getMemoryTree(0), copy);
//        tree_datasbase.delete_db();
//    }

    @Test
    public void treemap_database_test2() throws Exception {
        IDatabase<String, byte[]> tree_database = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getPatriciaTreeZoneInstance(0));

        Type fluentType = new TypeToken<MemoryTreePool>() {
        }.getType();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        SerializationUtil valueMapper = new SerializationUtil<>(fluentType, list);

        String address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        PatriciaTreeNode treeNode = new PatriciaTreeNode(BigDecimal.valueOf(2), 1);
        TreeFactory.getMemoryTree(1).store(address, treeNode);
        MemoryTreePool m = (MemoryTreePool) TreeFactory.getMemoryTree(1);

        //m.getByaddress(address);
        //use only special
        byte[] bt = valueMapper.encode_special(m, CustomFurySerializer.getInstance().getFury().serialize(m).length);
        tree_database.save("patricia_tree_root", bt);
        MemoryTreePool copy = (MemoryTreePool) valueMapper.decode(tree_database.findByKey("patricia_tree_root").get());

        PatriciaTreeNode treeNode2 = new PatriciaTreeNode(BigDecimal.valueOf(3), 3);
        TreeFactory.getMemoryTree(1).store("address", treeNode2);
        assertEquals(treeNode2, TreeFactory.getMemoryTree(1).getByaddress("address").get());

        //copy.store(address, treeNode);
        Option<PatriciaTreeNode> pat = copy.getByaddress(address);

        assertEquals(treeNode, pat.get());
        assertNotEquals(m, copy);

        TreeFactory.setMemoryTree(copy, 1);

        assertEquals(treeNode, TreeFactory.getMemoryTree(1).getByaddress(address).get());

        tree_database.delete_db();
    }

    @Test
    public void treemap_database_test2a() throws Exception {
        IDatabase<String, byte[]> tree_database = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getPatriciaTreeZoneInstance(0));

        Type fluentType = new TypeToken<MemoryTreePool>() {
        }.getType();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        SerializationUtil valueMapper = new SerializationUtil<>(fluentType, list);

        String address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        PatriciaTreeNode treeNode = new PatriciaTreeNode(BigDecimal.valueOf(2), 1);
        treeNode.addTransactionPosition(PatriciaTreeTransactionType.REGULAR, "1", 0, 1, 1);
        TreeFactory.getMemoryTree(1).store(address, treeNode);
        MemoryTreePool m = (MemoryTreePool) TreeFactory.getMemoryTree(1);

        //m.getByaddress(address);
        //use only special
        byte[] bt = valueMapper.encode_special(m, CustomFurySerializer.getInstance().getFury().serialize(m).length);
        tree_database.save("patricia_tree_root", bt);
        MemoryTreePool copy = (MemoryTreePool) valueMapper.decode(tree_database.findByKey("patricia_tree_root").get());

        PatriciaTreeNode treeNode2 = new PatriciaTreeNode(BigDecimal.valueOf(3), 3);
        TreeFactory.getMemoryTree(1).store("address", treeNode2);
        assertEquals(treeNode2, TreeFactory.getMemoryTree(1).getByaddress("address").get());

        //copy.store(address, treeNode);
        Option<PatriciaTreeNode> pat = copy.getByaddress(address);
        assertEquals(new StorageInfo(0, 1, 1), pat.get().retrieveTransactionInfoByHash(PatriciaTreeTransactionType.REGULAR, "1").get(0));
        assertEquals(treeNode, pat.get());
        assertNotEquals(m, copy);

        TreeFactory.setMemoryTree(copy, 1);

        assertEquals(treeNode, TreeFactory.getMemoryTree(1).getByaddress(address).get());

        tree_database.delete_db();
    }

    @Test
    public void treemap_database_test3() throws Exception {
        IDatabase<String, byte[]> tree_datasbase = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getPatriciaTreeZoneInstance(0));

        Type fluentType = new TypeToken<MemoryTreePool>() {
        }.getType();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        SerializationUtil valueMapper = new SerializationUtil<>(fluentType, list);

        String address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        PatriciaTreeNode treeNode = new PatriciaTreeNode(BigDecimal.valueOf(2), 1);
        TreeFactory.getMemoryTree(1).store(address, treeNode);
        MemoryTreePool m = (MemoryTreePool) TreeFactory.getMemoryTree(1);
        MemoryTreePool m2 = (MemoryTreePool) TreeFactory.getMemoryTree(2);
        MemoryTreePool m2clone = (MemoryTreePool) TreeFactory.getMemoryTree(2);
        assertNotEquals(m, m2);
        assertEquals(m2, m2clone);
        //m.getByaddress(address);
        //use only special
        byte[] bt = valueMapper.encode_special(m, CustomFurySerializer.getInstance().getFury().serialize(m).length);
        byte[] bt2 = valueMapper.encode_special(m2, CustomFurySerializer.getInstance().getFury().serialize(m2).length);
        tree_datasbase.save("3", bt);
        tree_datasbase.save("4", bt2);
        tree_datasbase.save("2", bt);
        tree_datasbase.save("1", bt);
        tree_datasbase.save("4", bt);
        Map<String, byte[]> copy = tree_datasbase.findBetweenRange("2");
        Optional<byte[]> res = tree_datasbase.seekLast();
        MemoryTreePool copys = (MemoryTreePool) valueMapper.decode(tree_datasbase.seekLast().get());
        tree_datasbase.delete_db();
    }
}