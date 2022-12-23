package io.Adrestus.consensus;

import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.config.NetworkConfiguration;
import io.Adrestus.config.TransactionConfigOptions;
import io.Adrestus.consensus.helper.ConsensusTransaction2Timer;
import io.Adrestus.core.*;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedReceiptSemaphore;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.Resourses.MemoryReceiptPool;
import io.Adrestus.crypto.WalletAddress;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import io.Adrestus.network.CachedEventLoop;
import io.Adrestus.network.IPFinder;
import io.Adrestus.network.TCPTransactionConsumer;
import io.Adrestus.network.TransactionChannelHandler;
import io.Adrestus.rpc.RpcAdrestusServer;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import io.activej.bytebuf.ByteBuf;
import io.activej.eventloop.Eventloop;
import io.activej.net.socket.tcp.AsyncTcpSocket;
import io.activej.net.socket.tcp.AsyncTcpSocketNio;
import io.distributedLedger.DatabaseInstance;
import io.distributedLedger.ZoneDatabaseFactory;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static io.activej.eventloop.Eventloop.getCurrentEventloop;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ConsensusTransactionTimer2Test {


    public static ArrayList<String> addreses_old;
    public static ArrayList<String> addreses;
    private static ArrayList<ECKeyPair> keypair;
    private static SerializationUtil<Transaction> serenc = new SerializationUtil<Transaction>(Transaction.class);
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

    private static SerializationUtil<Receipt> recep = new SerializationUtil<Receipt>(Receipt.class);
    private static IBlockIndex blockIndex;
    private static AsyncTcpSocket socket;

    @BeforeAll
    public static void construct() throws Exception {
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

        TreeFactory.getMemoryTree(1).store(adddress1, new PatriciaTreeNode(1000, 0));
        TreeFactory.getMemoryTree(1).store(adddress2, new PatriciaTreeNode(1000, 0));
        TreeFactory.getMemoryTree(1).store(adddress3, new PatriciaTreeNode(1000, 0));
        TreeFactory.getMemoryTree(1).store(adddress4, new PatriciaTreeNode(1000, 0));
        TreeFactory.getMemoryTree(1).store(adddress5, new PatriciaTreeNode(1000, 0));
        TreeFactory.getMemoryTree(1).store(adddress6, new PatriciaTreeNode(1000, 0));
        TreeFactory.getMemoryTree(1).store(adddress7, new PatriciaTreeNode(1000, 0));
        TreeFactory.getMemoryTree(1).store(adddress8, new PatriciaTreeNode(1000, 0));
        TreeFactory.getMemoryTree(1).store(adddress9, new PatriciaTreeNode(1000, 0));
        TreeFactory.getMemoryTree(1).store(adddress10, new PatriciaTreeNode(1000, 0));

        TreeFactory.getMemoryTree(0).store(adddress1, new PatriciaTreeNode(1000, 0));
        TreeFactory.getMemoryTree(0).store(adddress2, new PatriciaTreeNode(1000, 0));
        TreeFactory.getMemoryTree(0).store(adddress3, new PatriciaTreeNode(1000, 0));
        TreeFactory.getMemoryTree(0).store(adddress4, new PatriciaTreeNode(1000, 0));
        TreeFactory.getMemoryTree(0).store(adddress5, new PatriciaTreeNode(1000, 0));
        TreeFactory.getMemoryTree(0).store(adddress6, new PatriciaTreeNode(1000, 0));
        TreeFactory.getMemoryTree(0).store(adddress7, new PatriciaTreeNode(1000, 0));
        TreeFactory.getMemoryTree(0).store(adddress8, new PatriciaTreeNode(1000, 0));
        TreeFactory.getMemoryTree(0).store(adddress9, new PatriciaTreeNode(1000, 0));
        TreeFactory.getMemoryTree(0).store(adddress10, new PatriciaTreeNode(1000, 0));


        TransactionBlock prevblock = new TransactionBlock();
        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.setGeneration(1);
        committeeBlock.setViewID(1);
        prevblock.setHeight(1);
        prevblock.setHash("hash");
        prevblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        CachedLatestBlocks.getInstance().setCommitteeBlock(committeeBlock);

        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).put(vk1, "192.168.1.106");
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).put(vk2, "192.168.1.113");
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).put(vk3, "192.168.1.116");

        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).put(vk4, "192.168.1.110");
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).put(vk5, "192.168.1.112");
        CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).put(vk6, "192.168.1.115");


        CachedZoneIndex.getInstance().setZoneIndexInternalIP();


        prevblock.setTransactionProposer(CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).keySet().stream().findFirst().get().toRaw());
        prevblock.setLeaderPublicKey(CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).keySet().stream().findFirst().get());

        CachedLatestBlocks.getInstance().setTransactionBlock(prevblock);


        RpcAdrestusServer<AbstractBlock> example = new RpcAdrestusServer<AbstractBlock>(new TransactionBlock(), ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()), IPFinder.getLocal_address(), NetworkConfiguration.RPC_PORT, CachedEventLoop.getInstance().getEventloop());
        new Thread(example).start();

        TCPTransactionConsumer<byte[]> callback = x -> {
            Receipt receipt = recep.decode(x);
            MemoryReceiptPool.getInstance().add(receipt);
          //  System.out.println(MemoryReceiptPool.getInstance().getAll().size());
        };

        TransactionChannelHandler transactionChannelHandler = new TransactionChannelHandler<byte[]>(IPFinder.getLocal_address());
        transactionChannelHandler.BindServerAndReceive(callback);
        (new Thread() {
            public void run() {
                Eventloop eventloop = Eventloop.create().withCurrentThread();
                while (true) {
                    try {
                        CachedReceiptSemaphore.getInstance().getSemaphore().acquire();
                        if (!CachedLatestBlocks.getInstance().getTransactionBlock().getOutbound().getMap_receipts().isEmpty()) {
                            CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).values().forEach(ip -> {
                                eventloop.connect(new InetSocketAddress(ip, TransactionConfigOptions.TRANSACTION_PORT), (socketChannel, e) -> {
                                    if (e == null) {
                                        System.out.println("Connected to server, enter some text and send it by pressing 'Enter'.");
                                        try {
                                            socket = AsyncTcpSocketNio.wrapChannel(getCurrentEventloop(), socketChannel, null);
                                        } catch (IOException ioException) {
                                            throw new RuntimeException(ioException);
                                        }

                                        CachedLatestBlocks.getInstance().getTransactionBlock().getOutbound().getMap_receipts().get(0).entrySet().forEach(val -> {
                                            val.getValue().stream().forEach(receipt -> {
                                                TransactionBlock transactionBlock=CachedLatestBlocks.getInstance().getTransactionBlock();
                                                if(!transactionBlock.getHash().equals("hash")) {
                                                    receipt.setReceiptBlock(new Receipt.ReceiptBlock(transactionBlock.getHash(), transactionBlock.getHeight(), transactionBlock.getGeneration(), transactionBlock.getMerkleRoot()));
                                                    byte[] data = recep.encode(receipt);
                                                    socket.write(ByteBuf.wrapForReading(ArrayUtils.addAll(data, "\r\n".getBytes(UTF_8))));
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
                        CachedReceiptSemaphore.getInstance().getSemaphore().release();
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
        if (hit == 0)
            return;

        addreses_old = new ArrayList<>(addreses);
        CountDownLatch latch = new CountDownLatch(5);
        ConsensusTransaction2Timer c = new ConsensusTransaction2Timer(latch, addreses, keypair);
        latch.await();
        c.close();

    }
}
