package io.Adrestus.core;

import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.MerkleNode;
import io.Adrestus.Trie.MerkleTreeOptimizedImp;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.config.NetworkConfiguration;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.Resourses.MemoryReceiptPool;
import io.Adrestus.core.Resourses.MemoryTransactionPool;
import io.Adrestus.core.RingBuffer.handler.transactions.SignatureEventHandler;
import io.Adrestus.core.RingBuffer.publisher.BlockEventPublisher;
import io.Adrestus.core.RingBuffer.publisher.TransactionEventPublisher;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.WalletAddress;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.crypto.elliptic.ECDSASignatureData;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import io.Adrestus.crypto.elliptic.mapper.BigDecimalSerializer;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomSerializerTreeMap;
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import io.Adrestus.network.CachedEventLoop;
import io.Adrestus.network.IPFinder;
import io.Adrestus.rpc.RpcAdrestusServer;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseInstance;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import org.junit.jupiter.api.BeforeAll;
import org.spongycastle.util.encoders.Hex;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReceiptMain {
    private static BLSPrivateKey sk1;
    private static BLSPublicKey vk1;
    private static BLSPrivateKey sk3;
    private static BLSPublicKey vk3;
    private static TransactionBlock transactionBlock;
    private static BLSPrivateKey sk2;
    private static BLSPublicKey vk2;
    private static SerializationUtil<AbstractBlock> serenc;
    private static ArrayList<MerkleNode> merkleNodeArrayList;
    private static MerkleTreeOptimizedImp tree;

    @BeforeAll
    public static void setup() throws Exception {
        CachedZoneIndex.getInstance().setZoneIndex(0);
        sk1 = new BLSPrivateKey(1);
        vk1 = new BLSPublicKey(sk1);

        sk2 = new BLSPrivateKey(2);
        vk2 = new BLSPublicKey(sk2);

        sk3 = new BLSPrivateKey(2);
        vk3 = new BLSPublicKey(sk3);

        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        serenc = new SerializationUtil<AbstractBlock>(AbstractBlock.class, list);
        TransactionEventPublisher publisher = new TransactionEventPublisher(1024);
        SignatureEventHandler signatureEventHandler = new SignatureEventHandler(SignatureEventHandler.SignatureBehaviorType.SIMPLE_TRANSACTIONS);
        publisher
                .withAddressSizeEventHandler()
                .withTypeEventHandler()
                .withAmountEventHandler()
                .withDoubleSpendEventHandler()
                .withHashEventHandler()
                .withDelegateEventHandler()
                .withNonceEventHandler()
                .withReplayEventHandler()
                .withStakingEventHandler()
                .withTransactionFeeEventHandler()
                .withTimestampEventHandler()
                .withSameOriginEventHandler()
                .withDuplicateEventHandler()
                .withMinimumStakingEventHandler()
                .mergeEventsAndPassThen(signatureEventHandler);
        publisher.start();

        SecureRandom random;
        String mnemonic_code = "fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2";
        random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(Hex.decode(mnemonic_code));

        ECDSASign ecdsaSign = new ECDSASign();

        List<SerializationUtil.Mapping> lists = new ArrayList<>();
        lists.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        SerializationUtil<Transaction> enc = new SerializationUtil<Transaction>(Transaction.class, lists);

        ArrayList<String> addreses = new ArrayList<>();
        ArrayList<ECKeyPair> keypair = new ArrayList<>();
        int version = 0x00;
        int size = 10;
        for (int i = 0; i < size; i++) {
            Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
            char[] mnemonic_sequence = mnem.create();
            char[] passphrase = "p4ssphr4se".toCharArray();
            byte[] key = mnem.createSeed(mnemonic_sequence, passphrase);
            ECKeyPair ecKeyPair = Keys.createEcKeyPair(new SecureRandom(key));
            String adddress = WalletAddress.generate_address((byte) version, ecKeyPair.getPublicKey());
            addreses.add(adddress);
            keypair.add(ecKeyPair);
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(adddress, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        }


        int j = 1;
        signatureEventHandler.setLatch(new CountDownLatch(size - 1));
        for (int i = 0; i < size - 1; i++) {
            Transaction transaction = new RegularTransaction();
            transaction.setFrom(addreses.get(i));
            transaction.setTo(addreses.get(i + 1));
            transaction.setStatus(StatusType.PENDING);
            transaction.setTimestamp(GetTime.GetTimeStampInString());
            transaction.setZoneFrom(0);
            transaction.setZoneTo(j);
            transaction.setAmount(BigDecimal.valueOf(i));
            transaction.setAmountWithTransactionFee(transaction.getAmount().multiply(BigDecimal.valueOf(10.0 / 100.0)));
            transaction.setNonce(1);
            byte byf[] = enc.encode(transaction);
            transaction.setHash(HashUtil.sha256_bytetoString(byf));
            //  await().atMost(500, TimeUnit.MILLISECONDS);

            ECDSASignatureData signatureData = ecdsaSign.secp256SignMessage(Hex.decode(transaction.getHash()), keypair.get(i));
            transaction.setSignature(signatureData);
            //MemoryPool.getInstance().add(transaction);
            publisher.publish(transaction);
            //await().atMost(1000, TimeUnit.MILLISECONDS);
            if (j == 3)
                j = 0;
            j++;
        }
        publisher.getJobSyncUntilRemainingCapacityZero();
        signatureEventHandler.getLatch().await();
        publisher.close();


        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.getHeaderData().setTimestamp("2022-11-18 15:01:29.304");
        committeeBlock.getStructureMap().get(0).put(vk1, "192.168.1.106");
        committeeBlock.getStructureMap().get(0).put(vk3, "192.168.1.112");
        committeeBlock.getStructureMap().get(0).put(vk3, "192.168.1.114");
        committeeBlock.getStructureMap().get(1).put(vk2, "192.168.1.116");
        CachedLatestBlocks.getInstance().setCommitteeBlock(committeeBlock);
        CachedZoneIndex.getInstance().setZoneIndexInternalIP();
        tree = new MerkleTreeOptimizedImp();
        transactionBlock = new TransactionBlock();
        transactionBlock.setGeneration(4);
        transactionBlock.setHeight(100);
        transactionBlock.setTransactionList(MemoryTransactionPool.getInstance().getAll());
        merkleNodeArrayList = new ArrayList<>();
        transactionBlock.getTransactionList().stream().forEach(x -> {
            merkleNodeArrayList.add(new MerkleNode(x.getHash()));
        });
        tree.constructTree(merkleNodeArrayList);
        transactionBlock.setMerkleRoot(tree.getRootHash());
        byte[] tohash = serenc.encode(transactionBlock);
        transactionBlock.setHash(HashUtil.sha256_bytetoString(tohash));
    }

    public static void main(String[] args) throws Exception {
        setup();
        IDatabase<String, TransactionBlock> database = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.ZONE_1_TRANSACTION_BLOCK);
        database.save(transactionBlock.getHash(), transactionBlock);
        //  CachedEventLoop.getInstance().setEventloop(Eventloop.create().withCurrentThread());
        // new Thread(CachedEventLoop.getInstance().getEventloop());
        RpcAdrestusServer<AbstractBlock> example = new RpcAdrestusServer<AbstractBlock>(new TransactionBlock(), DatabaseInstance.ZONE_1_TRANSACTION_BLOCK, IPFinder.getLocal_address(), NetworkConfiguration.RPC_PORT, CachedEventLoop.getInstance().getEventloop());
        new Thread(example).start();

        BlockEventPublisher publisher = new BlockEventPublisher(1024);
        CachedZoneIndex.getInstance().setZoneIndex(1);
        CachedBLSKeyPair.getInstance().setPublicKey(vk1);
        CachedBLSKeyPair.getInstance().setPrivateKey(sk1);
        String OriginalRootHash = transactionBlock.getMerkleRoot();
        Receipt.ReceiptBlock receiptBlock = new Receipt.ReceiptBlock(transactionBlock.getHeight(), transactionBlock.getGeneration(), transactionBlock.getMerkleRoot());


        for (int i = 0; i < transactionBlock.getTransactionList().size(); i++) {
            Transaction transaction = transactionBlock.getTransactionList().get(i);
            MerkleNode node = new MerkleNode(transaction.getHash());
            tree.build_proofs(node);
            if (CachedZoneIndex.getInstance().getZoneIndex() == transaction.getZoneTo())
                MemoryReceiptPool.getInstance().add(new Receipt(transaction.getZoneFrom(), transaction.getZoneTo(), receiptBlock, i, tree.getMerkleeproofs()));
        }

        Map<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> map = ((ArrayList<Receipt>) MemoryReceiptPool.getInstance().getAll())
                .stream()
                .collect(Collectors.groupingBy(Receipt::getZoneFrom, Collectors.groupingBy(Receipt::getReceiptBlock, Collectors.mapping(Receipt::merge, Collectors.toList()))));

        for (Integer key : map.keySet()) {
            map.get(key).entrySet().stream().forEach(val -> {
                val.getValue().stream().forEach(x -> assertEquals(OriginalRootHash, tree.generateRoot(x.getProofs())));
            });
        }

        InboundRelay inboundRelay = new InboundRelay(map);
        transactionBlock.setInbound(inboundRelay);

        byte[] buffer = serenc.encode(transactionBlock);
        TransactionBlock clone = (TransactionBlock) serenc.decode(buffer);
        assertEquals(transactionBlock, clone);

        publisher.withInBoundEventHandler().mergeEvents();
        publisher.start();
        while (true) {
            publisher.publish(transactionBlock);
            Thread.sleep(1000);
        }

    }
}
