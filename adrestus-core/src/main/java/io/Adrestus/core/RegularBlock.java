package io.Adrestus.core;

import com.google.common.primitives.Ints;
import com.google.common.reflect.TypeToken;
import io.Adrestus.MemoryTreePool;
import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.MerkleNode;
import io.Adrestus.Trie.MerkleTreeOptimizedImp;
import io.Adrestus.Trie.PatriciaTreeTransactionType;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.config.RewardConfiguration;
import io.Adrestus.config.SocketConfigOptions;
import io.Adrestus.core.Resourses.*;
import io.Adrestus.core.RewardMechanism.CachedRewardMapData;
import io.Adrestus.core.RewardMechanism.Request;
import io.Adrestus.core.RewardMechanism.RequestType;
import io.Adrestus.core.RewardMechanism.RewardChainBuilder;
import io.Adrestus.core.Util.BlockSizeCalculator;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.elliptic.mapper.BigDecimalSerializer;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomSerializerTreeMap;
import io.Adrestus.crypto.elliptic.mapper.StakingData;
import io.Adrestus.network.AsyncService;
import io.Adrestus.network.CachedEventLoop;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.rpc.RpcAdrestusClient;
import io.Adrestus.util.*;
import io.distributedLedger.*;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;


public class RegularBlock implements BlockForge, BlockInvent {
    private static Logger LOG = LoggerFactory.getLogger(RegularBlock.class);

    private static volatile RegularBlock instance;

    private final SerializationUtil<AbstractBlock> encode;
    private final SerializationUtil<Transaction> transaction_encode;
    private final SerializationUtil<Receipt> receipt_encode;
    private final IBlockIndex blockIndex;

    private final BlockSizeCalculator blockSizeCalculator;

    private MerkleTreeOptimizedImp tree;

    private RegularBlock() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        Type fluentType = new TypeToken<MemoryTreePool>() {
        }.getType();
        this.encode = new SerializationUtil<AbstractBlock>(AbstractBlock.class, list);
        this.transaction_encode = new SerializationUtil<Transaction>(Transaction.class, list);
        this.receipt_encode = new SerializationUtil<Receipt>(Receipt.class, list);
        this.blockIndex = new BlockIndex();
        this.blockSizeCalculator = new BlockSizeCalculator();
    }

    public static RegularBlock getInstance() {
        var result = instance;
        if (result == null) {
            synchronized (RegularBlock.class) {
                result = instance;
                if (result == null) {
                    result = new RegularBlock();
                    instance = result;
                }
            }
        }
        return result;
    }

    @Override
    public void forgeTransactionBlock(TransactionBlock transactionBlock) throws Exception {
        tree = new MerkleTreeOptimizedImp();
        ArrayList<MerkleNode> merkleNodeArrayList = new ArrayList<>();
        transactionBlock.getHeaderData().setPreviousHash(CachedLatestBlocks.getInstance().getTransactionBlock().getHash());
        transactionBlock.getHeaderData().setVersion(AdrestusConfiguration.version);
        transactionBlock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        transactionBlock.setStatustype(StatusType.PENDING);
        transactionBlock.setHeight(CachedLatestBlocks.getInstance().getTransactionBlock().getHeight() + 1);
        transactionBlock.setGeneration(CachedLatestBlocks.getInstance().getCommitteeBlock().getGeneration());
        transactionBlock.setViewID(CachedLatestBlocks.getInstance().getTransactionBlock().getViewID() + 1);
        transactionBlock.setZone(CachedZoneIndex.getInstance().getZoneIndex());
        transactionBlock.setBlockProposer(CachedBLSKeyPair.getInstance().getPublicKey().toRaw());
        transactionBlock.setLeaderPublicKey(CachedBLSKeyPair.getInstance().getPublicKey());
        transactionBlock.setTransactionList(new ArrayList<>(MemoryTransactionPool.getInstance().getListByZone(CachedZoneIndex.getInstance().getZoneIndex())));
        ArrayList<Transaction> todelete = new ArrayList<>(MemoryTransactionPool.getInstance().getListToDelete(CachedZoneIndex.getInstance().getZoneIndex()));
        todelete.stream().forEach(transaction -> {
            if (transaction.getZoneFrom() != CachedZoneIndex.getInstance().getZoneIndex()) {
                Thread.ofVirtual().start(() -> {
                    List<String> ips = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(transaction.getZoneFrom()).values().stream().collect(Collectors.toList());
                    var executor = new AsyncService<Long>(ips, transaction_encode.encode(transaction, 1024), SocketConfigOptions.TRANSACTION_PORT);

                    var asyncResult1 = executor.startProcess(300L);
                    final var result1 = executor.endProcess(asyncResult1);
                    executor = null;
                });
            }
        });
        MemoryTransactionPool.getInstance().delete(todelete);
        todelete.clear();

        if (!transactionBlock.getTransactionList().isEmpty()) {
            BigDecimal sum = transactionBlock.getTransactionList().parallelStream().filter(val -> !val.getType().equals(TransactionType.UNCLAIMED_FEE_REWARD)).map(Transaction::getAmountWithTransactionFee).reduce(BigDecimal.ZERO, BigDecimal::add);
            try {
                transactionBlock.getTransactionList().add(0, new UnclaimedFeeRewardTransaction(TransactionType.UNCLAIMED_FEE_REWARD, this.blockIndex.getAddressByPublicKey(CachedBLSKeyPair.getInstance().getPublicKey()), sum));
            } catch (NoSuchElementException e) {
                LOG.info("Leader Reward fee not added");
            }
        }

        transactionBlock.getTransactionList().forEach(transaction -> merkleNodeArrayList.add(new MerkleNode(transaction.getHash())));
        tree.constructTree(merkleNodeArrayList);
        transactionBlock.setMerkleRoot(tree.getRootHash());

        //##########OutBound############
        Receipt.ReceiptBlock receiptBlock = new Receipt.ReceiptBlock(transactionBlock.getHeight(), transactionBlock.getGeneration(), transactionBlock.getMerkleRoot());
        ArrayList<Receipt> receiptList = new ArrayList<>();
        for (int i = 0; i < transactionBlock.getTransactionList().size(); i++) {
            Transaction transaction = transactionBlock.getTransactionList().get(i);
            if (transaction.getZoneFrom() != transaction.getZoneTo()) {
                MerkleNode node = new MerkleNode(transaction.getHash());
                tree.build_proofs(node);
                receiptList.add(new Receipt(transaction.getZoneFrom(), transaction.getZoneTo(), receiptBlock, tree.getMerkleeproofs(), i));
            }
        }

        Map<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> outbound = receiptList
                .stream()
                .collect(Collectors.groupingBy(Receipt::getZoneTo, Collectors.groupingBy(Receipt::getReceiptBlock)));

        OutBoundRelay outBoundRelay = new OutBoundRelay(outbound);
        transactionBlock.setOutbound(outBoundRelay);
        //##########OutBound############


        //##########InBound plus send outbound to the correct zone############
        if (!MemoryReceiptPool.getInstance().getAll().isEmpty()) {
            List<Receipt> receiptList1 = MemoryReceiptPool.getInstance().getOutBoundList(CachedZoneIndex.getInstance().getZoneIndex());
            if (!receiptList1.isEmpty()) {
                Map<Integer, List<Receipt>> receiptListGrouped = receiptList1.stream().collect(Collectors.groupingBy(w -> w.getZoneTo()));
                for (Map.Entry<Integer, List<Receipt>> entry : receiptListGrouped.entrySet()) {
                    List<String> ReceiptIPWorkers = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(entry.getKey()).values().stream().collect(Collectors.toList());
                    List<byte[]> toSendReceipt = new ArrayList<>();
                    entry.getValue().stream().forEach(receipt -> toSendReceipt.add(receipt_encode.encode(receipt, 1024)));

                    if (!toSendReceipt.isEmpty()) {
                        Thread.ofVirtual().start(() -> {
                            var executor = new AsyncService<Long>(ReceiptIPWorkers, toSendReceipt, SocketConfigOptions.RECEIPT_PORT);

                            var asyncResult = executor.startListProcess(300L);
                            var result = executor.endProcess(asyncResult);
                            MemoryReceiptPool.getInstance().delete(entry.getValue());
                        });
                    }
                }
            }
            Map<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> inbound_map = (new ArrayList<Receipt>((ArrayList<Receipt>) MemoryReceiptPool.getInstance().getInboundList(CachedZoneIndex.getInstance().getZoneIndex())))
                    .stream()
                    .collect(Collectors.groupingBy(Receipt::getZoneFrom, Collectors.groupingBy(Receipt::getReceiptBlock)));
            InboundRelay inboundRelay = new InboundRelay(inbound_map);
            transactionBlock.setInbound(inboundRelay);
            CachedInboundTransactionBlocks.getInstance().generate(inboundRelay.getMap_receipts(), transactionBlock.getGeneration());
        }
        //##########InBound############

        //######################Patricia_Tree#############################################
        // MemoryTreePool S= (MemoryTreePool) TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex());
        MemoryTreePool replica = new MemoryTreePool(((MemoryTreePool) TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex())));
        if (!transactionBlock.getTransactionList().isEmpty()) {
            TreePoolConstructBlock.getInstance().visitForgeTreePool(transactionBlock, replica);
        }
        if (!transactionBlock.getInbound().getMap_receipts().isEmpty())
            transactionBlock
                    .getInbound()
                    .getMap_receipts()
                    .get(transactionBlock.getInbound().getMap_receipts().keySet().toArray()[0])
                    .entrySet()
                    .stream()
                    .forEach(entry -> {
                        entry.getValue().stream().forEach(receipt -> {
                            TransactionBlock block = CachedInboundTransactionBlocks.getInstance().retrieve(receipt.getZoneFrom(), receipt.getReceiptBlock().getHeight());
                            Transaction trx = block.getTransactionList().get(receipt.getPosition());
                            replica.deposit(PatriciaTreeTransactionType.REGULAR, trx.getFrom(), trx.getAmount(), trx.getAmountWithTransactionFee());
                        });

                    });
        if (CachedZoneIndex.getInstance().getZoneIndex() == 0 && transactionBlock.getHeight() % RewardConfiguration.BLOCK_REWARD_HEIGHT == 0) {
            RewardChainBuilder rewardChainBuilder = new RewardChainBuilder();
            rewardChainBuilder.makeRequest(new Request(RequestType.EFFECTIVE_STAKE, "EFFECTIVE_STAKE"));
            rewardChainBuilder.makeRequest(new Request(RequestType.EFFECTIVE_STAKE_RATIO, "EFFECTIVE_STAKE_RATIO"));
            rewardChainBuilder.makeRequest(new Request(RequestType.DELEGATE_WEIGHTS_CALCULATOR, "DELEGATE_WEIGHTS_CALCULATOR"));
            rewardChainBuilder.makeRequest(new Request(RequestType.VALIDATOR_REWARD_CALCULATOR, "VALIDATOR_REWARD_CALCULATOR"));
            rewardChainBuilder.makeRequest(new Request(RequestType.DELEGATE_REWARD_CALCULATOR, "DELEGATE_REWARD_CALCULATOR"));
            rewardChainBuilder.makeRequest(new Request(RequestType.REWARD_STORAGE_CALCULATOR, "REWARD_STORAGE_CALCULATOR", replica));
        }

        transactionBlock.setPatriciaMerkleRoot(replica.getRootHash());
        //######################Patricia_Tree#############################################


        this.blockSizeCalculator.setTransactionBlock(transactionBlock);
        byte[] tohash = encode.encode(transactionBlock, this.blockSizeCalculator.TransactionBlockSizeCalculator());
        transactionBlock.setHash(HashUtil.sha256_bytetoString(tohash));
//      transactionBlock.getOutbound().getMap_receipts().values().forEach(receiptBlocks -> receiptBlocks.keySet().forEach(vals -> vals.setBlock_hash(transactionBlock.getHash())));
    }

    @SneakyThrows
    @Override
    public void forgeCommitteBlock(CommitteeBlock committeeBlock) {
        IDatabase<String, CommitteeBlock> database = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);

        if (CachedKademliaNodes.getInstance().getDhtBootstrapNode() != null) {
            List<KademliaData> kademliaData = CachedKademliaNodes
                    .getInstance()
                    .getDhtBootstrapNode()
                    .getActiveNodes()
                    .stream()
                    .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparing(e -> e.getAddressData().getAddress()))), ArrayList::new));

            for (int i = 0; i < kademliaData.size(); i++) {
                committeeBlock
                        .getStakingMap()
                        .put(new StakingData(i, TreeFactory.getMemoryTree(0).getByaddress(kademliaData.get(i).getAddressData().getAddress()).get().getStaking_amount()), kademliaData.get(i));
            }
        } else if (CachedKademliaNodes.getInstance().getDhtRegularNode() != null) {
            List<KademliaData> kademliaData = CachedKademliaNodes
                    .getInstance()
                    .getDhtRegularNode()
                    .getActiveNodes()
                    .stream()
                    .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparing(e -> e.getAddressData().getAddress()))), ArrayList::new));

            for (int i = 0; i < kademliaData.size(); i++) {
                committeeBlock
                        .getStakingMap()
                        .put(new StakingData(i, TreeFactory.getMemoryTree(0).getByaddress(kademliaData.get(i).getAddressData().getAddress()).get().getStaking_amount()), kademliaData.get(i));
            }
        } else {
            committeeBlock.setStakingMap(CachedLatestBlocks.getInstance().getCommitteeBlock().getStakingMap());
        }
        committeeBlock.setCommitteeProposer(new int[committeeBlock.getStakingMap().size()]);
        committeeBlock.setGeneration(CachedLatestBlocks.getInstance().getCommitteeBlock().getGeneration() + 1);
        committeeBlock.getHeaderData().setPreviousHash(CachedLatestBlocks.getInstance().getCommitteeBlock().getHash());
        committeeBlock.setHeight(CachedLatestBlocks.getInstance().getCommitteeBlock().getHeight() + 1);
        committeeBlock.setViewID(CachedLatestBlocks.getInstance().getCommitteeBlock().getViewID() + 1);
        committeeBlock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        committeeBlock.setVRF(Hex.toHexString(CachedSecurityHeaders.getInstance().getSecurityHeader().getPRnd()));


        // ###################find difficulty##########################
        int finish = database.findDBsize();

        int n = finish;
        int summdiffuclty = 0;
        long sumtime = 0;
        Map<String, CommitteeBlock> block_entries = database.seekBetweenRange(0, finish);
        ArrayList<String> entries = new ArrayList<String>(block_entries.keySet());

        if (entries.size() == 1) {
            summdiffuclty = block_entries.get(entries.get(0)).getDifficulty();
            sumtime = 100;
        } else {
            for (int i = 0; i < entries.size(); i++) {
                if (i == entries.size() - 1)
                    break;

                long older = GetTime.GetTimestampFromString(block_entries.get(entries.get(i)).getHeaderData().getTimestamp()).getTime();
                long newer = GetTime.GetTimestampFromString(block_entries.get(entries.get(i + 1)).getHeaderData().getTimestamp()).getTime();
                sumtime = sumtime + (newer - older);
                //System.out.println("edw "+(newer - older));
                summdiffuclty = summdiffuclty + block_entries.get(entries.get(i)).getDifficulty();
                //  System.out.println("edw "+(newer - older));
            }
        }

        double d = ((double) summdiffuclty / n);
        // String s=String.format("%4d",  sumtime / n);
        double t = ((double) sumtime / n);
        //  System.out.println(t);
        int difficulty = MathOperationUtil.multiplication((int) Math.round((t) / d));
        if (difficulty < 100) {
            committeeBlock.setStatustype(StatusType.ABORT);
            throw new IllegalArgumentException("VDF difficulty is not set correct abort");
        }
        committeeBlock.setDifficulty(difficulty);
        // ###################find difficulty##########################


        committeeBlock.setVDF(Hex.toHexString(CachedSecurityHeaders.getInstance().getSecurityHeader().getRnd()));
        //committeeBlock.setVDF(Hex.toHexString(vdf.solve(Hex.decode(committeeBlock.getVRF()), committeeBlock.getDifficulty())));
        // ###################find VDF difficulty##########################

        // ###################Random assign validators##########################
        SecureRandom zone_random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        SecureRandom leader_random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        zone_random.setSeed(Hex.decode(committeeBlock.getVDF()));
        leader_random.setSeed(Hex.decode(committeeBlock.getVDF()));
        //#####RANDOM ASSIGN TO STRUCTRURE MAP ##############
        ArrayList<Integer> exclude = new ArrayList<Integer>();
        ArrayList<Integer> order = new ArrayList<Integer>();
        for (Map.Entry<StakingData, KademliaData> entry : committeeBlock.getStakingMap().entrySet()) {
            int nextInt = CustomRandom.generateRandom(zone_random, 0, committeeBlock.getStakingMap().size() - 1, exclude);
            if (!exclude.contains(nextInt)) {
                exclude.add(nextInt);
            }
            order.add(nextInt);
        }
        int zone_count = 0;
        List<Map.Entry<StakingData, KademliaData>> entryList = committeeBlock.getStakingMap().entrySet().stream().collect(Collectors.toList());
        int MAX_ZONE_SIZE = committeeBlock.getStakingMap().size() / 4;

        int j = 0;
        if (MAX_ZONE_SIZE >= 2) {
            int addition = committeeBlock.getStakingMap().size() - MathOperationUtil.closestNumber(committeeBlock.getStakingMap().size(), 4);
            while (zone_count < 4) {
                if (zone_count == 0 && addition != 0) {
                    while (j < MAX_ZONE_SIZE + addition) {
                        committeeBlock
                                .getStructureMap()
                                .get(zone_count)
                                .put(entryList.get(order.get(j)).getValue().getAddressData().getValidatorBlSPublicKey(), entryList.get(order.get(j)).getValue().getNettyConnectionInfo().getHost());
                        j++;
                    }
                } else {
                    int index_count = 0;
                    while (index_count < MAX_ZONE_SIZE) {
                        committeeBlock
                                .getStructureMap()
                                .get(zone_count)
                                .put(entryList.get(order.get(j)).getValue().getAddressData().getValidatorBlSPublicKey(), entryList.get(order.get(j)).getValue().getNettyConnectionInfo().getHost());
                        index_count++;
                        j++;
                    }
                }
                zone_count++;
            }
        } else {
            for (int i = 0; i < order.size(); i++) {
                committeeBlock
                        .getStructureMap()
                        .get(0)
                        .put(entryList.get(order.get(i)).getValue().getAddressData().getValidatorBlSPublicKey(), entryList.get(order.get(i)).getValue().getNettyConnectionInfo().getHost());
            }
        }

        //#######RANDOM ASSIGN TO STRUCTRURE MAP ##############
        int iteration = 0;
        ArrayList<Integer> replica = new ArrayList<>();
        while (iteration < committeeBlock.getStakingMap().size()) {
            int nextInt = leader_random.nextInt(committeeBlock.getStakingMap().size());
            if (!replica.contains(nextInt)) {
                replica.add(nextInt);
                iteration++;
            }
        }
        //###################Random assign validators##########################

        committeeBlock.setCommitteeProposer(Ints.toArray(replica));
        //########################################################################

        this.blockSizeCalculator.setCommitteeBlock(committeeBlock);
        String hash = HashUtil.sha256_bytetoString(encode.encode(committeeBlock, this.blockSizeCalculator.CommitteeBlockSizeCalculator()));
        committeeBlock.setHash(hash);
    }


    @SneakyThrows
    @Override
    public void InventTransactionBlock(TransactionBlock transactionBlock) {

        transactionBlock.setStatustype(StatusType.SUCCES);
        transactionBlock.getTransactionList().forEach(val -> val.setStatus(StatusType.SUCCES));


        if (!transactionBlock.getTransactionList().isEmpty()) {
            TreePoolConstructBlock.getInstance().visitInventTreePool(transactionBlock, TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()));
        }

        if (!transactionBlock.getInbound().getMap_receipts().isEmpty())
            transactionBlock
                    .getInbound()
                    .getMap_receipts()
                    .forEach((key, value) -> value
                            .entrySet()
                            .stream()
                            .forEach(entry -> {
                                for (int i = 0; i < entry.getValue().size(); i++) {
                                    Receipt receipt = entry.getValue().get(i);
                                    TransactionBlock block = CachedInboundTransactionBlocks.getInstance().retrieve(receipt.getZoneFrom(), receipt.getReceiptBlock().getHeight());
                                    Transaction trx = block.getTransactionList().get(receipt.getPosition());
                                    String rcphash = HashUtil.sha256_bytetoString(this.receipt_encode.encode(receipt, 1024));
                                    TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(trx.getTo()).get().addReceiptPosition(rcphash, CachedZoneIndex.getInstance().getZoneIndex(), transactionBlock.getHeight(), receipt.getZoneFrom(), receipt.getReceiptBlock().getHeight(), i);
                                    TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).deposit(PatriciaTreeTransactionType.REGULAR, trx.getTo(), trx.getAmount(), trx.getAmountWithTransactionFee());
                                    MemoryReceiptPool.getInstance().delete(receipt);
                                }
                            }));

        if (!transactionBlock.getOutbound().getMap_receipts().isEmpty()) {
            Thread.ofVirtual().start(() -> {
                for (Map.Entry<Integer, LinkedHashMap<Receipt.ReceiptBlock, List<Receipt>>> entry : transactionBlock.getOutbound().getMap_receipts().entrySet()) {
                    List<String> ReceiptIPWorkers = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(entry.getKey()).values().stream().collect(Collectors.toList());
                    List<byte[]> toSendReceipt = new ArrayList<>();
                    for (Map.Entry<Receipt.ReceiptBlock, List<Receipt>> entry2 : entry.getValue().entrySet()) {
                        entry2.getValue().forEach(receipt -> toSendReceipt.add(receipt_encode.encode(receipt, 1024)));
                    }
                    var executor = new AsyncService<Long>(ReceiptIPWorkers, toSendReceipt, SocketConfigOptions.RECEIPT_PORT);
                    var asyncResult = executor.startListProcess(300L);
                    var result = executor.endProcess(asyncResult);
                }
            });
        }

        if (CachedZoneIndex.getInstance().getZoneIndex() == 0 && transactionBlock.getHeight() % RewardConfiguration.BLOCK_REWARD_HEIGHT == 0) {
            RewardChainBuilder rewardChainBuilder = new RewardChainBuilder();
            rewardChainBuilder.makeRequest(new Request(RequestType.REWARD_STORAGE_CALCULATOR, "REWARD_STORAGE_CALCULATOR", TreeFactory.getMemoryTree(0)));
            CachedRewardMapData.getInstance().clearInstance();
            CachedStartHeightRewards.getInstance().setRewardsCommitteeEnabled(false);
            CachedStartHeightRewards.getInstance().setHeight(transactionBlock.getHeight());
        }
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        CountDownLatch countDownLatch = new CountDownLatch(2);
        Runnable TransactionSave = () -> {
            IDatabase<String, TransactionBlock> block_database = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
            BlockSizeCalculator blockSizeCalculator = new BlockSizeCalculator();
            blockSizeCalculator.setTransactionBlock(transactionBlock);
            block_database.save(String.valueOf(transactionBlock.getHeight()), transactionBlock, blockSizeCalculator.TransactionBlockSizeCalculator());
            countDownLatch.countDown();
        };
        Runnable TreeSave = () -> {
            IDatabase<String, byte[]> tree_database = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getPatriciaTreeZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
            tree_database.save(String.valueOf(CachedZoneIndex.getInstance().getZoneIndex()), SerializationFuryUtil.getInstance().getFury().serialize(TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex())));
            countDownLatch.countDown();
        };
        executor.submit(TransactionSave);
        executor.submit(TreeSave);
        countDownLatch.await();
        executor.shutdownNow();
        executor.close();

        if (tree != null)
            tree.clear();
        TransactionBlock clonable = transactionBlock.clone();
        CachedLatestBlocks.getInstance().setTransactionBlock(clonable);
        MemoryTransactionPool.getInstance().delete(clonable.getTransactionList());

        //Sync committee block from zone0
      /*  if (CachedZoneIndex.getInstance().getZoneIndex() != 0) {
            List<String> ips = CachedLatestBlocks
                    .getInstance()
                    .getCommitteeBlock()
                    .getStructureMap()
                    .get(0)
                    .values()
                    .stream()
                    .collect(Collectors.toList());

            ArrayList<InetSocketAddress> toConnectCommitee = new ArrayList<>();
            ips.stream().forEach(ip -> {
                try {
                    toConnectCommitee.add(new InetSocketAddress(InetAddress.getByName(ip), NetworkConfiguration.RPC_PORT));
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
            });

            RpcAdrestusClient client = null;
            label:
            try {
                IDatabase<String, CommitteeBlock> commitee_block_database = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);
                client = new RpcAdrestusClient(new CommitteeBlock(), toConnectCommitee, eventloop);
                client.connect();

                List<CommitteeBlock> commitee_blocks = client.getBlocksList(String.valueOf(CachedLatestBlocks.getInstance().getCommitteeBlock().getHeight()));

                if (commitee_blocks.isEmpty())
                    break label;
                CommitteeBlock last = commitee_blocks.get(commitee_blocks.size() - 1);

                if (last.getHeight() != CachedLatestBlocks.getInstance().getCommitteeBlock().getHeight()) {
                    commitee_block_database.save(String.valueOf(last.getHeight()), last);
                    CachedLeaderIndex.getInstance().setCommitteePositionLeader(0);
                    CachedLatestBlocks.getInstance().setCommitteeBlock(last);
                    CachedZoneIndex.getInstance().setZoneIndexInternalIP();
                    CachedEpochGeneration.getInstance().setEpoch_counter(0);
                    CachedLeaderIndex.getInstance().setTransactionPositionLeader(0);
                    //CachedLeaderIndex.getInstance().setTransactionPositionLeader(CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).size() - 1);
                }
                if (client != null)
                    client.close();
            } catch (IllegalArgumentException e) {
            }
        }*/
        CachedInboundTransactionBlocks.getInstance().clear();
    }


    //invent
    @SneakyThrows
    @Override
    public void InventCommitteBlock(CommitteeBlock committeeBlock) {
        CommitteeBlock prevblock = CachedLatestBlocks.getInstance().getCommitteeBlock().clone();
        int prevZone = Integer.valueOf(CachedZoneIndex.getInstance().getZoneIndex());
        IDatabase<String, CommitteeBlock> database = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);


        CachedLatestBlocks.getInstance().setCommitteeBlock(committeeBlock);
        CachedLeaderIndex.getInstance().setCommitteePositionLeader(0);
        CachedLeaderIndex.getInstance().setTransactionPositionLeader(0);
        CachedZoneIndex.getInstance().setZoneIndexInternalIP();

        if (prevZone == CachedZoneIndex.getInstance().getZoneIndex()) {
            CachedLeaderIndex.getInstance().setTransactionPositionLeader(0);
            committeeBlock.setStatustype(StatusType.SUCCES);
            database.save(String.valueOf(committeeBlock.getGeneration()), committeeBlock);
            return;
        }
        //sync blocks from zone of previous validators for both transaction and patricia tree blocks
        List<String> ips = prevblock.getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).values().stream().collect(Collectors.toList());
        if (ips.isEmpty()) {
            IDatabase<String, TransactionBlock> block_database = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
            IDatabase<String, byte[]> tree_database = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getPatriciaTreeZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));

            Optional<TransactionBlock> block = block_database.seekLast();
            Optional<byte[]> tree = tree_database.seekLast();

            CachedLatestBlocks.getInstance().setTransactionBlock(block.get());
            TreeFactory.setMemoryTree((MemoryTreePool) SerializationFuryUtil.getInstance().getFury().deserialize(tree.get()), CachedZoneIndex.getInstance().getZoneIndex());
        } else {
            int RPCTransactionZonePort = ZoneDatabaseFactory.getDatabaseRPCPort(CachedZoneIndex.getInstance().getZoneIndex());
            int RPCPatriciaTreeZonePort = ZoneDatabaseFactory.getDatabasePatriciaRPCPort(ZoneDatabaseFactory.getPatriciaTreeZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
            ArrayList<InetSocketAddress> toConnectTransaction = new ArrayList<>();
            ArrayList<InetSocketAddress> toConnectPatricia = new ArrayList<>();
            ips.stream().forEach(ip -> {
                try {
                    toConnectTransaction.add(new InetSocketAddress(InetAddress.getByName(ip), RPCTransactionZonePort));
                    toConnectPatricia.add(new InetSocketAddress(InetAddress.getByName(ip), RPCPatriciaTreeZonePort));
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
            });
            RpcAdrestusClient client = null;
            try {
                IDatabase<String, TransactionBlock> block_database = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
                client = new RpcAdrestusClient(new TransactionBlock(), toConnectTransaction, CachedEventLoop.getInstance().getEventloop());
                client.connect();

                Optional<TransactionBlock> block = block_database.seekLast();
                Map<String, TransactionBlock> toSave = new HashMap<>();
                List<TransactionBlock> blocks;
                if (block.isPresent()) {
                    blocks = client.getBlocksList(String.valueOf(block.get().getHeight()));
                    if (!blocks.isEmpty()) {
                        blocks.stream().skip(1).forEach(val -> toSave.put(String.valueOf(val.getHeight()), val));
                    }

                } else {
                    blocks = client.getBlocksList("");
                    if (!blocks.isEmpty()) {
                        blocks.stream().forEach(val -> toSave.put(String.valueOf(val.getHeight()), val));
                    }
                }
                block_database.saveAll(toSave);
                if (!blocks.isEmpty()) {
                    CachedLatestBlocks.getInstance().setTransactionBlock(blocks.get(blocks.size() - 1));
                    CachedLeaderIndex.getInstance().setTransactionPositionLeader(0);
                }
                if (client != null)
                    client.close();
            } catch (IllegalArgumentException e) {
            }

            try {
                IDatabase<String, byte[]> tree_database = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getPatriciaTreeZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
                client = new RpcAdrestusClient(new byte[]{}, toConnectPatricia, CachedEventLoop.getInstance().getEventloop());
                client.connect();

                List<byte[]> treeObjects = client.getPatriciaTreeList("");
                if (!treeObjects.isEmpty()) {
                    TreeFactory.setMemoryTree((MemoryTreePool) SerializationFuryUtil.getInstance().getFury().deserialize(treeObjects.get(0)), CachedZoneIndex.getInstance().getZoneIndex());
                    tree_database.save(String.valueOf(CachedZoneIndex.getInstance().getZoneIndex()), treeObjects.get(0));
                }
                if (client != null)
                    client.close();

            } catch (IllegalArgumentException e) {
            }
        }

        //find transactions that is not for this zone and sent them to the correct zone
        List<Transaction> transactionList = MemoryTransactionPool.getInstance().getListByZone(prevZone);
        List<byte[]> toSendTransaction = new ArrayList<>();
        transactionList.stream().forEach(transaction -> toSendTransaction.add(transaction_encode.encode(transaction, 1024)));
        List<String> TransactionIPWorkers = committeeBlock.getStructureMap().get(prevZone).values().stream().collect(Collectors.toList());

        if (!toSendTransaction.isEmpty()) {
            var executor = new AsyncService<Long>(TransactionIPWorkers, toSendTransaction, SocketConfigOptions.TRANSACTION_PORT);

            var asyncResult = executor.startListProcess(300L);
            var result = executor.endProcess(asyncResult);
            MemoryTransactionPool.getInstance().delete(transactionList);
        }

        //find receipts that is not for this zone and sent them to the correct zone
        List<Receipt> receiptList = MemoryReceiptPool.getInstance().getListByZone(prevZone);
        List<byte[]> toSendReceipt = new ArrayList<>();
        receiptList.stream().forEach(receipt -> toSendReceipt.add(receipt_encode.encode(receipt, 1024)));
        List<String> ReceiptIPWorkers = committeeBlock.getStructureMap().get(prevZone).values().stream().collect(Collectors.toList());

        if (!toSendReceipt.isEmpty()) {
            var executor = new AsyncService<Long>(ReceiptIPWorkers, toSendReceipt, SocketConfigOptions.RECEIPT_PORT);

            var asyncResult = executor.startListProcess(300L);
            var result = executor.endProcess(asyncResult);
            MemoryReceiptPool.getInstance().delete(receiptList);
        }

        committeeBlock.setStatustype(StatusType.SUCCES);
        database.save(String.valueOf(committeeBlock.getGeneration()), committeeBlock);

    }

}
