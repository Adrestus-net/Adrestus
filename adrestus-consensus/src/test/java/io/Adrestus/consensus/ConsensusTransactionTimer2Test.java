package io.Adrestus.consensus;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.reflect.TypeToken;
import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.config.CacheConfigurationTest;
import io.Adrestus.config.KademliaConfiguration;
import io.Adrestus.config.SocketConfigOptions;
import io.Adrestus.consensus.helper.ConsensusTransaction2Timer;
import io.Adrestus.core.*;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.RingBuffer.publisher.ReceiptEventPublisher;
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
import io.Adrestus.crypto.elliptic.mapper.BigDecimalSerializer;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.StakingData;
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import io.Adrestus.network.CachedEventLoop;
import io.Adrestus.network.IPFinder;
import io.Adrestus.network.TCPTransactionConsumer;
import io.Adrestus.network.TransactionChannelHandler;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.rpc.RpcAdrestusClient;
import io.Adrestus.rpc.RpcAdrestusServer;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import io.activej.bytebuf.ByteBuf;
import io.activej.bytebuf.ByteBufPool;
import io.activej.eventloop.Eventloop;
import io.activej.net.socket.tcp.ITcpSocket;
import io.activej.net.socket.tcp.TcpSocket;
import io.distributedLedger.*;
import org.apache.commons.codec.binary.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.activej.reactor.Reactor.getCurrentReactor;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConsensusTransactionTimer2Test {


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

    private static SerializationUtil<Receipt> recep;
    private static IBlockIndex blockIndex;
    private static ITcpSocket socket;
    private static boolean variable = false;
    private static KademliaData kad1, kad2, kad3, kad4, kad5, kad6;
    private static ReceiptEventPublisher publisher;
    private static Cache<Integer, TransactionBlock> receiptloadingCache;

    @BeforeAll
    public static void construct() throws Exception {
        if (System.out.getClass().getName().contains("maven")) {
            return;
        }

        delete_test();
        IDatabase<String, CommitteeBlock> commit = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        serenc = new SerializationUtil<Transaction>(Transaction.class, list);
        recep = new SerializationUtil<Receipt>(Receipt.class, list);
        receiptloadingCache = Caffeine.newBuilder()
                .initialCapacity(CacheConfigurationTest.INITIAL_CAPACITY)
                .maximumSize(CacheConfigurationTest.MAXIMUM_SIZE)
                .expireAfterWrite(CacheConfigurationTest.EXPIRATION_MINUTES, TimeUnit.MINUTES)
                .expireAfterAccess(CacheConfigurationTest.EXPIRATION_MINUTES, TimeUnit.MINUTES)
                .build();
        publisher = new ReceiptEventPublisher(1024);
        publisher.
                withGenerationEventHandler().
                withHeightEventHandler().
                withOutboundMerkleEventHandler().
                withZoneEventHandler().
                withReplayEventHandler().
                withEmptyEventHandler().
                withPublicKeyEventHandler()
                .withSignatureEventHandler()
                .withZoneFromEventHandler()
                .mergeEvents();
        publisher.start();

        blockIndex = new BlockIndex();
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
        ECKeyPair ecKeyPair1 = Keys.create256r1KeyPair(random);
        random.setSeed(key2);
        ECKeyPair ecKeyPair2 = Keys.create256r1KeyPair(random);
        random.setSeed(key3);
        ECKeyPair ecKeyPair3 = Keys.create256r1KeyPair(random);
        random.setSeed(key4);
        ECKeyPair ecKeyPair4 = Keys.create256r1KeyPair(random);
        random.setSeed(key5);
        ECKeyPair ecKeyPair5 = Keys.create256r1KeyPair(random);
        random.setSeed(key6);
        ECKeyPair ecKeyPair6 = Keys.create256r1KeyPair(random);
        random.setSeed(key7);
        ECKeyPair ecKeyPair7 = Keys.create256r1KeyPair(random);
        random.setSeed(key8);
        ECKeyPair ecKeyPair8 = Keys.create256r1KeyPair(random);
        random.setSeed(key9);
        ECKeyPair ecKeyPair9 = Keys.create256r1KeyPair(random);
        random.setSeed(key10);
        ECKeyPair ecKeyPair10 = Keys.create256r1KeyPair(random);
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

        TreeFactory.getMemoryTree(1).store(adddress1, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(1).store(adddress2, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(1).store(adddress3, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(1).store(adddress4, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(1).store(adddress5, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(1).store(adddress6, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(1).store(adddress7, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(1).store(adddress8, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(1).store(adddress9, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(1).store(adddress10, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));

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

        ECDSASignatureData signatureData1 = ecdsaSign.signSecp256r1Message(HashUtil.sha256(StringUtils.getBytesUtf8(adddress1)), ecKeyPair1);
        ECDSASignatureData signatureData2 = ecdsaSign.signSecp256r1Message(HashUtil.sha256(StringUtils.getBytesUtf8(adddress2)), ecKeyPair2);
        ECDSASignatureData signatureData3 = ecdsaSign.signSecp256r1Message(HashUtil.sha256(StringUtils.getBytesUtf8(adddress3)), ecKeyPair3);
        ECDSASignatureData signatureData4 = ecdsaSign.signSecp256r1Message(HashUtil.sha256(StringUtils.getBytesUtf8(adddress4)), ecKeyPair4);
        ECDSASignatureData signatureData5 = ecdsaSign.signSecp256r1Message(HashUtil.sha256(StringUtils.getBytesUtf8(adddress5)), ecKeyPair5);
        ECDSASignatureData signatureData6 = ecdsaSign.signSecp256r1Message(HashUtil.sha256(StringUtils.getBytesUtf8(adddress6)), ecKeyPair6);

        kad1 = new KademliaData(new SecurityAuditProofs(adddress1, vk1, ecKeyPair1.getPublicKey(), signatureData1), new NettyConnectionInfo("192.168.1.106", KademliaConfiguration.PORT));
        kad2 = new KademliaData(new SecurityAuditProofs(adddress2, vk2, ecKeyPair2.getPublicKey(), signatureData2), new NettyConnectionInfo("192.168.1.113", KademliaConfiguration.PORT));
        kad3 = new KademliaData(new SecurityAuditProofs(adddress3, vk3, ecKeyPair3.getPublicKey(), signatureData3), new NettyConnectionInfo("192.168.1.116", KademliaConfiguration.PORT));
        kad4 = new KademliaData(new SecurityAuditProofs(adddress4, vk4, ecKeyPair4.getPublicKey(), signatureData4), new NettyConnectionInfo("192.168.1.110", KademliaConfiguration.PORT));
        kad5 = new KademliaData(new SecurityAuditProofs(adddress5, vk5, ecKeyPair5.getPublicKey(), signatureData5), new NettyConnectionInfo("192.168.1.112", KademliaConfiguration.PORT));
        kad6 = new KademliaData(new SecurityAuditProofs(adddress6, vk6, ecKeyPair6.getPublicKey(), signatureData6), new NettyConnectionInfo("192.168.1.115", KademliaConfiguration.PORT));
        TransactionBlock prevblock = new TransactionBlock();
        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.setGeneration(1);
        committeeBlock.setViewID(1);
        prevblock.setHeight(1);
        prevblock.setHash("hash");
        prevblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        CachedLatestBlocks.getInstance().setCommitteeBlock(committeeBlock);

        CachedLatestBlocks.getInstance().getCommitteeBlock().getStakingMap().put(new StakingData(1, BigDecimal.valueOf(10.0)), kad1);
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStakingMap().put(new StakingData(2, BigDecimal.valueOf(11.0)), kad2);
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStakingMap().put(new StakingData(3, BigDecimal.valueOf(151.0)), kad3);
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStakingMap().put(new StakingData(1, BigDecimal.valueOf(101.0)), kad4);
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStakingMap().put(new StakingData(2, BigDecimal.valueOf(111.0)), kad5);
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStakingMap().put(new StakingData(3, BigDecimal.valueOf(251.0)), kad6);

        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).put(vk1, "192.168.1.106");
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).put(vk2, "192.168.1.113");
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).put(vk3, "192.168.1.116");

        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).put(vk4, "192.168.1.110");
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).put(vk5, "192.168.1.112");
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).put(vk6, "192.168.1.115");


        commit.save(String.valueOf(CachedLatestBlocks.getInstance().getCommitteeBlock().getGeneration()), CachedLatestBlocks.getInstance().getCommitteeBlock());
        CachedZoneIndex.getInstance().setZoneIndexInternalIP();


        if (CachedZoneIndex.getInstance().getZoneIndex() == 1) {
            prevblock.setBlockProposer(vk1.toRaw());
            prevblock.setLeaderPublicKey(vk1);
        } else {
            prevblock.setBlockProposer(vk5.toRaw());
            prevblock.setLeaderPublicKey(vk5);
        }

        CachedLatestBlocks.getInstance().setTransactionBlock(prevblock);

        Socket socket1 = new Socket();
        socket1.connect(new InetSocketAddress("google.com", 80));
        String IP = socket1.getLocalAddress().getHostAddress();
        int hit = 0;
        if (!IP.substring(0, 3).equals("192")) {
            return;
        }

        RpcAdrestusServer<AbstractBlock> example = new RpcAdrestusServer<AbstractBlock>(new TransactionBlock(), ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()), IPFinder.getLocal_address(), ZoneDatabaseFactory.getDatabaseRPCPort(CachedZoneIndex.getInstance().getZoneIndex()), CachedEventLoop.getInstance().getEventloop());
        new Thread(example).start();
        CachedEventLoop.getInstance().start();

        TCPTransactionConsumer<byte[]> callback = x -> {
            if (CachedZoneIndex.getInstance().getZoneIndex() == 1) {
                return "";
            }
            Receipt receipt = recep.decode(x);
            if (receipt.getReceiptBlock() == null) {
                return "";
            }
            TransactionBlock transactionBlock = receiptloadingCache.getIfPresent(receipt.getReceiptBlock().getHeight());
            if (transactionBlock != null) {
                Transaction trx = transactionBlock.getTransactionList().get(receipt.getPosition());
                ReceiptBlock receiptBlock1 = new ReceiptBlock(StatusType.PENDING, receipt, transactionBlock, trx);
                publisher.publish(receiptBlock1);
            } else {
                List<String> ips = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(receipt.getZoneFrom()).values().stream().collect(Collectors.toList());
                ips.remove(IPFinder.getLocalIP());
                int RPCTransactionZonePort = ZoneDatabaseFactory.getDatabaseRPCPort(receipt.getZoneFrom());
                ArrayList<InetSocketAddress> toConnectTransaction = new ArrayList<>();
                ips.stream().forEach(ip -> {
                    try {
                        toConnectTransaction.add(new InetSocketAddress(InetAddress.getByName(ip), RPCTransactionZonePort));
                    } catch (UnknownHostException e) {
                        throw new RuntimeException(e);
                    }
                });
                RpcAdrestusClient client = null;
                try {
                    client = new RpcAdrestusClient(new TransactionBlock(), toConnectTransaction, CachedEventLoop.getInstance().getEventloop());
                    client.connect();

                    ArrayList<String> to_search = new ArrayList<>();
                    to_search.add(String.valueOf(receipt.getReceiptBlock().getHeight()));

                    List<TransactionBlock> currentblock = client.getBlock(to_search);
                    if (currentblock.isEmpty()) {
                        return "";
                    }

                    int index = receipt.getPosition();
                    Transaction trx = currentblock.get(currentblock.size() - 1).getTransactionList().get(index);

                    ReceiptBlock receiptBlock1 = new ReceiptBlock(StatusType.PENDING, receipt, currentblock.get(currentblock.size() - 1), trx);
                    receiptloadingCache.put(currentblock.get(currentblock.size() - 1).getHeight(), currentblock.get(currentblock.size() - 1));

                    publisher.publish(receiptBlock1);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (client != null) {
                        client.close();
                        client = null;
                    }
                }
            }
            return "";
        };

        TransactionChannelHandler transactionChannelHandler = new TransactionChannelHandler<byte[]>(IPFinder.getLocal_address());
        transactionChannelHandler.BindServerAndReceive(callback);
        (new Thread() {
            public void run() {
                Eventloop eventloop = Eventloop.builder().withCurrentThread().build();
                while (!variable) {
                    try {
                        if (!CachedLatestBlocks.getInstance().getTransactionBlock().getOutbound().getMap_receipts().isEmpty()) {
                            CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).values().forEach(ip -> {
                                eventloop.connect(new InetSocketAddress(ip, SocketConfigOptions.TRANSACTION_PORT), (socketChannel, e) -> {
                                    if (e == null) {
                                        // System.out.println("Connected to server, enter some text and send it by pressing 'Enter'.");
                                        try {
                                            socket = TcpSocket.wrapChannel(getCurrentReactor(), socketChannel, null);
                                        } catch (IOException ioException) {
                                            throw new RuntimeException(ioException);
                                        }

                                        CachedLatestBlocks.getInstance().getTransactionBlock().getOutbound().getMap_receipts().get(0).entrySet().forEach(val -> {
                                            val.getValue().stream().forEach(receipt -> {
                                                TransactionBlock transactionBlock = CachedLatestBlocks.getInstance().getTransactionBlock();
                                                if (!transactionBlock.getHash().equals("hash")) {
                                                    receipt.setReceiptBlock(new Receipt.ReceiptBlock(transactionBlock.getHeight(), transactionBlock.getGeneration(), transactionBlock.getMerkleRoot()));
                                                    byte data[] = recep.encode(receipt, 1024);
                                                    ByteBuf sizeBuf = ByteBufPool.allocate(2); // enough to serialize size 1024
                                                    sizeBuf.writeVarInt(data.length);
                                                    ByteBuf appendedBuf = ByteBufPool.append(sizeBuf, ByteBuf.wrapForReading(data));
                                                    socket.write(appendedBuf);
                                                }
                                            });
                                        });
                                        socket.close();

                                    } else {
                                        System.out.printf("Could not connect to server, make sure it is started: %s%n", e);
                                    }
                                });
                            });
                            eventloop.run();
                        }
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    @Test
    public void consensus_timer_test() throws Exception {
        if (System.out.getClass().getName().contains("maven")) {
            return;
        }
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("google.com", 80));
        String IP = socket.getLocalAddress().getHostAddress();
        int hit = 0;
        int activezones = blockIndex.getZone(IP);
        for (Map.Entry<BLSPublicKey, String> entry : CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(activezones).entrySet()) {
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
        if (hit == 0) {
            variable = true;
            return;
        }

        addreses_old = new ArrayList<>(addreses);
        //add latch 6 for zone 1
        //add latch 9 for validators on zone 0
        if (CachedZoneIndex.getInstance().getZoneIndex() == 0) {
            Thread.sleep(4300);
        }
        //first run 245,507,113,116,984,106 last 3 digits ip
        CountDownLatch latch = new CountDownLatch(6);
        ConsensusTransaction2Timer c = new ConsensusTransaction2Timer(latch, addreses, keypair);
        latch.await();
        c.close();
        if (CachedZoneIndex.getInstance().getZoneIndex() == 1) {
            Thread.sleep(9300);
        }
        variable = true;

        for (int i = 0; i < addreses.size() - 1; i++) {
            System.out.println(addreses.get(i) + " " + TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(addreses.get(i)).get().getAmount());
        }
        if (CachedZoneIndex.getInstance().getZoneIndex() == 0) {
            assertEquals(974.1999999999999, TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(addreses.get(6)).get().getAmount());
        }
        if (CachedZoneIndex.getInstance().getZoneIndex() == 0) {
            assertEquals(1027, TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(addreses.get(1)).get().getAmount());
            assertEquals(1029.7, TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(addreses.get(2)).get().getAmount());
        }
    }

    public static void delete_test() {
        IDatabase<String, LevelDBTransactionWrapper<Transaction>> transaction_database = new DatabaseFactory(String.class, Transaction.class, new TypeToken<LevelDBTransactionWrapper<Transaction>>() {
        }.getType()).getDatabase(DatabaseType.LEVEL_DB);
        IDatabase<String, LevelDBReceiptWrapper<Receipt>> receiptdatabase = new DatabaseFactory(String.class, Receipt.class, new TypeToken<LevelDBReceiptWrapper<Receipt>>() {
        }.getType()).getDatabase(DatabaseType.LEVEL_DB);
        IDatabase<String, TransactionBlock> transaction_block1 = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.ZONE_0_TRANSACTION_BLOCK);
        IDatabase<String, TransactionBlock> transaction_block2 = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.ZONE_1_TRANSACTION_BLOCK);
        IDatabase<String, TransactionBlock> transaction_block3 = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.ZONE_2_TRANSACTION_BLOCK);
        IDatabase<String, TransactionBlock> transaction_block4 = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.ZONE_3_TRANSACTION_BLOCK);
        IDatabase<String, TransactionBlock> transaction_block4a = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.ZONE_3_TRANSACTION_BLOCK);

        IDatabase<String, byte[]> patricia_tree0 = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_0);
        IDatabase<String, byte[]> patricia_tree1 = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_1);
        IDatabase<String, byte[]> patricia_tree2 = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_2);
        IDatabase<String, byte[]> patricia_tree3 = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_3);

        IDatabase<String, byte[]> patricia_tree3a = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_3);
        IDatabase<String, CommitteeBlock> commit = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);
        IDatabase<String, CommitteeBlock> commita = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);


        //ITS IMPORTANT FIRST DELETE PATRICIA TREE AND AFTER TRASNACTION BLOCKS
        patricia_tree0.delete_db();
        patricia_tree1.delete_db();
        patricia_tree2.delete_db();
        patricia_tree3.delete_db();

        transaction_block1.delete_db();
        transaction_block2.delete_db();
        transaction_block3.delete_db();
        transaction_block4.delete_db();


        commit.delete_db();
        transaction_database.delete_db();
        receiptdatabase.delete_db();
    }
}
