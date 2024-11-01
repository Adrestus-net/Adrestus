package io.Adrestus.consensus;

import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.config.KademliaConfiguration;
import io.Adrestus.config.RewardConfiguration;
import io.Adrestus.consensus.helper.ConsensusTransactionTimer;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedStartHeightRewards;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.Transaction;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.SecurityAuditProofs;
import io.Adrestus.crypto.WalletAddress;
import io.Adrestus.crypto.bls.BLSSignatureData;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.BLSSignature;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.crypto.elliptic.ECDSASignatureData;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import io.Adrestus.crypto.elliptic.mapper.BigDecimalSerializer;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.StakingData;
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import io.Adrestus.network.CachedEventLoop;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import io.distributedLedger.ZoneDatabaseFactory;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConsensusTransactionTimerTest {


    public static ArrayList<String> addreses_old;
    public static ArrayList<String> addreses;
    private static ArrayList<ECKeyPair> keypair;
    private static SerializationUtil<Transaction> serenc;
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
    private static KademliaData kad1, kad2, kad3, kad4, kad5, kad6;

    @BeforeAll
    public static void construct() throws Exception {
        CachedZoneIndex.getInstance().setZoneIndex(0);
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        serenc = new SerializationUtil<Transaction>(Transaction.class, list);
        IDatabase<String, TransactionBlock> block_database = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
        block_database.delete_db();
        block_database = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
        IDatabase<String, byte[]> tree_datasbase = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getPatriciaTreeZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
        tree_datasbase.delete_db();
        tree_datasbase = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getPatriciaTreeZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));

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

        SecureRandom random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(key1);
        ECKeyPair ecKeyPair1 = Keys.createEcKeyPair(random);
        random.setSeed(key2);
        ECKeyPair ecKeyPair2 = Keys.createEcKeyPair(random);
        random.setSeed(key3);
        ECKeyPair ecKeyPair3 = Keys.createEcKeyPair(random);
        random.setSeed(key4);
        ECKeyPair ecKeyPair4 = Keys.createEcKeyPair(random);
        random.setSeed(key5);
        ECKeyPair ecKeyPair5 = Keys.createEcKeyPair(random);
        random.setSeed(key6);
        ECKeyPair ecKeyPair6 = Keys.createEcKeyPair(random);
        random.setSeed(key7);
        ECKeyPair ecKeyPair7 = Keys.createEcKeyPair(random);
        random.setSeed(key8);
        ECKeyPair ecKeyPair8 = Keys.createEcKeyPair(random);
        random.setSeed(key9);
        ECKeyPair ecKeyPair9 = Keys.createEcKeyPair(random);
        random.setSeed(key10);
        ECKeyPair ecKeyPair10 = Keys.createEcKeyPair(random);
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
        ECDSASignatureData signatureData6 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(adddress6)), ecKeyPair6);

        TreeFactory.getMemoryTree(0).store(adddress1, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0, BigDecimal.valueOf(100), BigDecimal.valueOf(40)));
        TreeFactory.getMemoryTree(0).store(adddress2, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0, BigDecimal.valueOf(200), BigDecimal.valueOf(30)));
        TreeFactory.getMemoryTree(0).store(adddress3, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0, BigDecimal.valueOf(300), BigDecimal.valueOf(20)));
        TreeFactory.getMemoryTree(0).store(adddress4, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(0).store(adddress5, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(0).store(adddress6, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(0).store(adddress7, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(0).store(adddress8, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(0).store(adddress9, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(0).store(adddress10, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));


        TransactionBlock prevblock = new TransactionBlock();
        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.setGeneration(1);
        committeeBlock.setViewID(1);
        prevblock.setHeight(1);
        prevblock.setHash("hash");
        prevblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        prevblock.setBlockProposer(vk1.toRaw());
        prevblock.setLeaderPublicKey(vk1);

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


        prevblock.getSignatureData().put(vk1, BLSSignatureData1a);
        prevblock.getSignatureData().put(vk2, BLSSignatureData2a);
        prevblock.getSignatureData().put(vk3, BLSSignatureData3a);

        Thread.sleep(1000);
        CachedStartHeightRewards.getInstance().setHeight(1);
        CachedLatestBlocks.getInstance().setCommitteeBlock(committeeBlock);
        CachedLatestBlocks.getInstance().setTransactionBlock(prevblock);
        block_database.save(String.valueOf(prevblock.getHeight()), prevblock);

        kad1 = new KademliaData(new SecurityAuditProofs(adddress1, vk1, ecKeyPair1.getPublicKey(), signatureData1), new NettyConnectionInfo("192.168.1.106", KademliaConfiguration.PORT));
        kad2 = new KademliaData(new SecurityAuditProofs(adddress2, vk2, ecKeyPair2.getPublicKey(), signatureData2), new NettyConnectionInfo("192.168.1.115", KademliaConfiguration.PORT));
        kad3 = new KademliaData(new SecurityAuditProofs(adddress3, vk3, ecKeyPair3.getPublicKey(), signatureData3), new NettyConnectionInfo("192.168.1.116", KademliaConfiguration.PORT));

        System.out.println("ADRESS: " + adddress1 + "KEY1  " + vk1.toString());
        System.out.println("ADRESS: " + adddress2 + "KEY2  " + vk2.toString());
        System.out.println("ADRESS: " + adddress3 + "KEY3  " + vk3.toString());
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStakingMap().put(new StakingData(1, BigDecimal.valueOf(100.0)), kad3);
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStakingMap().put(new StakingData(2, BigDecimal.valueOf(200.0)), kad2);
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStakingMap().put(new StakingData(3, BigDecimal.valueOf(300.0)), kad1);

        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).put(vk1, "192.168.1.106");
        //CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).put(vk2, "192.168.1.110");
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).put(vk2, "192.168.1.115");
        // CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).put(vk4, "192.168.1.115");
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).put(vk3, "192.168.1.116");
        // CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).put(vk6, "192.168.1.104");
        CachedZoneIndex.getInstance().setZoneIndexInternalIP();
    }


    @Test
    public void consensus_timer_test() throws Exception {
        if (System.getenv("MAVEN_OPTS") != null) {
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
                } else if (vk6.equals(entry.getKey())) {
                    CachedBLSKeyPair.getInstance().setPrivateKey(sk6);
                    CachedBLSKeyPair.getInstance().setPublicKey(vk6);
                }
                hit = 1;
                break;
            }
        }
        if (hit == 0)
            return;

        CachedEventLoop.getInstance().start();
        addreses_old = new ArrayList<>(addreses);
        // 5 is the deafult
        CountDownLatch latch = new CountDownLatch(5);
        ConsensusTransactionTimer c = new ConsensusTransactionTimer(latch, addreses, keypair);
        latch.await();
        c.close();

        //assertEquals(TreeFactory.getMemoryTree(1).getByaddress(addreses.get(0)).get().getAmount(), TreeFactory.getMemoryTree(1).getByaddress(addreses.get(0)).get().getAmount()-100);
        for (int i = 0; i < addreses.size() - 1; i++) {
            System.out.println(addreses.get(i) + " " + TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(addreses.get(i)).get().getAmount() + " " + TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(addreses.get(i)).get().getUnclaimed_reward());
        }

        IDatabase<String, TransactionBlock> block_database = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
        Map<String, TransactionBlock> res = block_database.seekFromStart();
        Map<String, BigDecimal> claimed = new HashMap<>();
        for (Map.Entry<String, TransactionBlock> entry : res.entrySet()) {
            BigDecimal sum = entry.getValue().getTransactionList().stream().map(Transaction::getAmountWithTransactionFee).reduce(BigDecimal.ZERO, BigDecimal::add);
            String address = CachedLatestBlocks.getInstance().getCommitteeBlock().getStakingMap().values().stream().map(KademliaData::getAddressData).filter(val -> val.getValidatorBlSPublicKey().equals(entry.getValue().getLeaderPublicKey())).findFirst().get().getAddress();
            if (claimed.get(address) == null)
                claimed.put(address, sum);
            else
                claimed.put(address, sum.add(claimed.get(address)));
        }

        //be aware that print functionality is  different it works only for latch 5
        assertEquals(1.784998, TreeFactory.getMemoryTree(0).getByaddress("ADR-ACAO-BKTC-CFKG-VXWF-PSI2-QHWR-ZIGK-CCOL-LGJN-CM3U").get().getUnclaimed_reward().subtract(claimed.get("ADR-ACAO-BKTC-CFKG-VXWF-PSI2-QHWR-ZIGK-CCOL-LGJN-CM3U")).doubleValue());
        assertEquals(1.190001, TreeFactory.getMemoryTree(0).getByaddress("ADR-AADE-ROH3-CAFV-XK5V-2NKZ-QMTG-SFMC-37W5-SHUV-2T46").get().getUnclaimed_reward().subtract(claimed.get("ADR-AADE-ROH3-CAFV-XK5V-2NKZ-QMTG-SFMC-37W5-SHUV-2T46")).doubleValue());
        assertEquals(BigDecimal.valueOf(2.473339 - 0.10).setScale(RewardConfiguration.DECIMAL_PRECISION, RewardConfiguration.ROUNDING), TreeFactory.getMemoryTree(0).getByaddress("ADR-AB2W-RIQY-LSIH-CXQQ-FGRV-AINR-57RO-NFXU-IWM5-IANJ").get().getUnclaimed_reward().subtract(claimed.get("ADR-AB2W-RIQY-LSIH-CXQQ-FGRV-AINR-57RO-NFXU-IWM5-IANJ")));

        assertEquals(1.190001, TreeFactory.getMemoryTree(0).getByaddress("ADR-AADE-ROH3-CAFV-XK5V-2NKZ-QMTG-SFMC-37W5-SHUV-2T46").get().getUnclaimed_reward().subtract(claimed.get("ADR-AADE-ROH3-CAFV-XK5V-2NKZ-QMTG-SFMC-37W5-SHUV-2T46")).doubleValue());
        assertEquals(1.784998, TreeFactory.getMemoryTree(0).getByaddress("ADR-ACAO-BKTC-CFKG-VXWF-PSI2-QHWR-ZIGK-CCOL-LGJN-CM3U").get().getUnclaimed_reward().subtract(claimed.get("ADR-ACAO-BKTC-CFKG-VXWF-PSI2-QHWR-ZIGK-CCOL-LGJN-CM3U")).doubleValue());
        assertEquals(BigDecimal.valueOf(2.473339 - 0.10).setScale(RewardConfiguration.DECIMAL_PRECISION, RewardConfiguration.ROUNDING), TreeFactory.getMemoryTree(0).getByaddress("ADR-AB2W-RIQY-LSIH-CXQQ-FGRV-AINR-57RO-NFXU-IWM5-IANJ").get().getUnclaimed_reward().subtract(claimed.get("ADR-AB2W-RIQY-LSIH-CXQQ-FGRV-AINR-57RO-NFXU-IWM5-IANJ")));

        assertEquals(989.1, TreeFactory.getMemoryTree(0).getByaddress(addreses.get(0)).get().getAmount().doubleValue());
        assertEquals(984.8, TreeFactory.getMemoryTree(0).getByaddress(addreses.get(1)).get().getAmount().doubleValue());
        assertEquals(993.4, TreeFactory.getMemoryTree(0).getByaddress(addreses.get(2)).get().getAmount().doubleValue());
        assertEquals(993.0, TreeFactory.getMemoryTree(0).getByaddress(addreses.get(3)).get().getAmount().doubleValue());
        assertEquals(1008.0, TreeFactory.getMemoryTree(0).getByaddress(addreses.get(4)).get().getAmount().doubleValue());
        assertEquals(996.1, TreeFactory.getMemoryTree(0).getByaddress(addreses.get(5)).get().getAmount().doubleValue());
        assertEquals(1013.5, TreeFactory.getMemoryTree(0).getByaddress(addreses.get(6)).get().getAmount().doubleValue());
        assertEquals(1000.0, TreeFactory.getMemoryTree(0).getByaddress(addreses.get(7)).get().getAmount().doubleValue());
        assertEquals(1000.0, TreeFactory.getMemoryTree(0).getByaddress(addreses.get(8)).get().getAmount().doubleValue());


    }
 /*   public static void setup() throws Exception {


        TransactionEventPublisher publisher = new TransactionEventPublisher(1024);

        publisher
                .withAddressSizeEventHandler()
                .withAmountEventHandler()
                .WithTypeEventHandler()
                .withDelegateEventHandler()
                .withDoubleSpendEventHandler()
                .withHashEventHandler()
                .withNonceEventHandler()
                .withReplayEventHandler()
                .withRewardEventHandler()
                .withStakingEventHandler()
                .withTransactionFeeEventHandler()
                .mergeEventsAndPassThen(new SignatureEventHandler(SignatureEventHandler.SignatureBehaviorType.SIMPLE_TRANSACTIONS));
        publisher.start();


        SecureRandom random;
        String mnemonic_code = "fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2";
        random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        random.setSeed(Hex.decode(mnemonic_code));

        ECDSASign ecdsaSign = new ECDSASign();

        SerializationUtil<Transaction> serenc = new SerializationUtil<Transaction>(Transaction.class);

        ArrayList<String> addreses = new ArrayList<>();
        ArrayList<ECKeyPair> keypair = new ArrayList<>();
        int version = 0x00;
        int size = 10;
        for (int i = 0; i < size; i++) {
            Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
            char[] mnemonic_sequence = mnem.create();
            String[] words = String.valueOf(mnemonic_sequence).split(" ");
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < words.length; j++)
                sb.append(words[j] + " ");
            System.out.println(sb.toString());
            char[] passphrase = "p4ssphr4se".toCharArray();
            byte[] key = mnem.createSeed(mnemonic_sequence, passphrase);
            ECKeyPair ecKeyPair = Keys.createEcKeyPair(new SecureRandom(key));
            String adddress = WalletAddress.generate_address((byte) version, ecKeyPair.getPublicKey());
            addreses.add(adddress);
            keypair.add(ecKeyPair);
            MemoryTreePool.getInstance().store(adddress, new PatriciaTreeNode(1000, 0));
        }


        for (int i = 0; i < size - 1; i++) {
            Transaction transaction = new RegularTransaction();
            transaction.setFrom(addreses.get(i));
            transaction.setTo(addreses.get(i + 1));
            transaction.setStatus(StatusType.PENDING);
            transaction.setTimestamp(GetTime.GetTimeStampInString());
            transaction.setZoneFrom(0);
            transaction.setZoneTo(0);
            transaction.setAmount(100);
            transaction.setAmountWithTransactionFee(transaction.getAmount() * (10.0 / 100.0));
            transaction.setNonce(1);
            byte byf[] = serenc.encode(transaction);
            transaction.setHash(HashUtil.sha256_bytetoString(byf));

            SignatureData signatureData = ecdsaSign.secp256SignMessage(Hex.decode(transaction.getHash()), keypair.get(i));
            transaction.setSignature(signatureData);
            //MemoryPool.getInstance().add(transaction);
            publisher.publish(transaction);
            Thread.sleep(1);
        }
        publisher.getJobSyncUntilRemainingCapacityZero();
        publisher.close();


        TransactionBlock prevblock = new TransactionBlock();
        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.setGeneration(1);
        committeeBlock.setViewID(1);
        prevblock.setHeight(1);
        prevblock.setHash("hash");
        prevblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        prevblock.setTransactionProposer(vk5.toRaw());
        prevblock.setLeaderPublicKey(vk5);
        CachedLatestBlocks.getInstance().setCommitteeBlock(committeeBlock);
        CachedLatestBlocks.getInstance().setTransactionBlock(prevblock);

        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).put(vk1, "192.168.1.103");
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).put(vk2, "192.168.1.104");
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).put(vk3, "192.168.1.105");
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).put(vk4, "192.168.1.106");
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).put(vk5, "192.168.1.107");
    }


    @Test
    public void consensus_timer_test() throws Exception {
        setup();
        BLSPrivateKey sk1 = new BLSPrivateKey(1);
        BLSPublicKey vk1 = new BLSPublicKey(sk1);

        BLSPrivateKey sk2 = new BLSPrivateKey(2);
        BLSPublicKey vk2 = new BLSPublicKey(sk2);

        BLSPrivateKey sk3 = new BLSPrivateKey(3);
        BLSPublicKey vk3 = new BLSPublicKey(sk3);

        BLSPrivateKey sk4 = new BLSPrivateKey(4);
        BLSPublicKey vk4 = new BLSPublicKey(sk4);


        BLSPrivateKey sk5 = new BLSPrivateKey(5);
        BLSPublicKey vk5 = new BLSPublicKey(sk5);

        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).put(vk1, "192.168.1.103");
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).put(vk2, "192.168.1.104");
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).put(vk3, "192.168.1.105");
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).put(vk4, "192.168.1.106");
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).put(vk5, "192.168.1.107");

        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("google.com", 80));
        String IP = socket.getLocalAddress().getHostAddress();
        int hit = 0;
        for (Map.Entry<BLSPublicKey, String> entry : CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).entrySet()) {
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

        TransactionBlock transactionBlock = new TransactionBlock();
        transactionBlock.setTransactionProposer(vk5.toRaw());
        transactionBlock.setLeaderPublicKey(vk5);
        transactionBlock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        CachedLatestBlocks.getInstance().setTransactionBlock(transactionBlock);
        CountDownLatch latch = new CountDownLatch(2);
        ConsensusTimer c = new ConsensusTimer(latch);
        latch.await();
        c.getTask().cancel();
        c.getTimer().cancel();
        Thread.sleep(4000);
    }*/
}
