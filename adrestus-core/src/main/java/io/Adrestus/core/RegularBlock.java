package io.Adrestus.core;

import com.google.common.primitives.Ints;
import io.Adrestus.MemoryTreePool;
import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.MerkleNode;
import io.Adrestus.Trie.MerkleTreeImp;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.core.Resourses.*;
import io.Adrestus.core.RingBuffer.publisher.BlockEventPublisher;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomSerializerTreeMap;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.util.CustomRandom;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.MathOperationUtil;
import io.Adrestus.util.SerializationUtil;
import io.distributedLedger.*;
import lombok.SneakyThrows;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;


public class RegularBlock implements BlockForge, BlockInvent {
    private static Logger LOG = LoggerFactory.getLogger(RegularBlock.class);

    private final SerializationUtil<AbstractBlock> encode;

    public RegularBlock() {
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        encode = new SerializationUtil<AbstractBlock>(AbstractBlock.class, list);
    }

    @Override
    public void forgeTransactionBlock(TransactionBlock transactionBlock) throws Exception {
        CachedReceiptSemaphore.getInstance().getSemaphore().acquire();

        BlockEventPublisher publisher = new BlockEventPublisher(1024);


        publisher
                .withDuplicateHandler()
                .withGenerationHandler()
                .withHashHandler()
                .withHeaderEventHandler()
                .withHeightEventHandler()
                .withTimestampEventHandler()
                .withTransactionMerkleeEventHandler()
                .mergeEventsAndPassVerifySig();


        MerkleTreeImp tree = new MerkleTreeImp();
        ArrayList<MerkleNode> merkleNodeArrayList = new ArrayList<>();
        transactionBlock.getHeaderData().setPreviousHash(CachedLatestBlocks.getInstance().getTransactionBlock().getHash());
        transactionBlock.getHeaderData().setVersion(AdrestusConfiguration.version);
        transactionBlock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        transactionBlock.setStatustype(StatusType.PENDING);
        transactionBlock.setHeight(CachedLatestBlocks.getInstance().getTransactionBlock().getHeight() + 1);
        transactionBlock.setGeneration(CachedLatestBlocks.getInstance().getCommitteeBlock().getGeneration());
        transactionBlock.setViewID(CachedLatestBlocks.getInstance().getTransactionBlock().getViewID() + 1);
        transactionBlock.setZone(CachedZoneIndex.getInstance().getZoneIndex());
        transactionBlock.setLeaderPublicKey(CachedBLSKeyPair.getInstance().getPublicKey());
        transactionBlock.setTransactionList(MemoryTransactionPool.getInstance().getAll());
        transactionBlock.getTransactionList().stream().forEach(x -> {
            merkleNodeArrayList.add(new MerkleNode(x.getHash()));
        });
        tree.my_generate2(merkleNodeArrayList);
        transactionBlock.setMerkleRoot(tree.getRootHash());


        //######################Patricia_Tree#############################################
        // MemoryTreePool S= (MemoryTreePool) TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex());
        MemoryTreePool replica = new MemoryTreePool(((MemoryTreePool) TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex())));
        if (!transactionBlock.getTransactionList().isEmpty()) {
            for (int i = 0; i < transactionBlock.getTransactionList().size(); i++) {
                Transaction transaction = transactionBlock.getTransactionList().get(i);
                if ((transaction.getZoneFrom() == CachedZoneIndex.getInstance().getZoneIndex()) && (transaction.getZoneTo() == CachedZoneIndex.getInstance().getZoneIndex())) {
                    replica.withdrawReplica(transaction.getFrom(), transaction.getAmount(), replica);
                    replica.depositReplica(transaction.getTo(), transaction.getAmount(), replica);
                } else {
                    replica.withdrawReplica(transaction.getFrom(), transaction.getAmount(), replica);
                }
            }
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
                            replica.depositReplica(receipt.getAddress(), receipt.getAmount(), replica);
                        });

                    });
        transactionBlock.setPatriciaMerkleRoot(replica.getRootHash());
        //######################Patricia_Tree#############################################

        //##########OutBound############
        Receipt.ReceiptBlock receiptBlock = new Receipt.ReceiptBlock(transactionBlock.getHash(), transactionBlock.getHeight(), transactionBlock.getGeneration(), transactionBlock.getMerkleRoot());
        ArrayList<Receipt> receiptList = new ArrayList<>();
        for (int i = 0; i < transactionBlock.getTransactionList().size(); i++) {
            Transaction transaction = transactionBlock.getTransactionList().get(i);
            MerkleNode node = new MerkleNode(transaction.getHash());
            tree.build_proofs2(merkleNodeArrayList, node);
            if (transaction.getZoneFrom() != transaction.getZoneTo())
                receiptList.add(new Receipt(transaction.getZoneFrom(), transaction.getZoneTo(), receiptBlock, new RegularTransaction(transaction.getHash()), i, tree.getMerkleeproofs(), transaction.getTo(), transaction.getAmount()));
        }

        Map<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> outbound = receiptList
                .stream()
                .collect(Collectors.groupingBy(Receipt::getZoneTo, Collectors.groupingBy(Receipt::getReceiptBlock, Collectors.mapping(Receipt::merge, Collectors.toList()))));

        OutBoundRelay outBoundRelay = new OutBoundRelay(outbound);
        transactionBlock.setOutbound(outBoundRelay);
        //##########OutBound############


        //##########InBound############
        if (!MemoryReceiptPool.getInstance().getAll().isEmpty()) {
            Map<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> inbound_map = ((ArrayList<Receipt>) MemoryReceiptPool.getInstance().getAll())
                    .stream()
                    .collect(Collectors.groupingBy(Receipt::getZoneFrom, Collectors.groupingBy(Receipt::getReceiptBlock, Collectors.mapping(Receipt::merge, Collectors.toList()))));
            InboundRelay inboundRelay = new InboundRelay(inbound_map);
            transactionBlock.setInbound(inboundRelay);
        }


        //##########InBound############

        try {
            byte[] tohash = encode.encode(transactionBlock);
            transactionBlock.setHash(HashUtil.sha256_bytetoString(tohash));
            publisher.start();
            publisher.publish(transactionBlock);
            publisher.getJobSyncUntilRemainingCapacityZero();

        } finally {
            publisher.close();
        }

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
                    .collect(Collectors.toList());
            kademliaData.stream().forEach(val -> committeeBlock
                    .getStakingMap()
                    .put(TreeFactory.getMemoryTree(0).getByaddress(val.getAddressData().getAddress()).get().getStaking_amount(), val));
        } else {
            committeeBlock.setStakingMap(CachedLatestBlocks.getInstance().getCommitteeBlock().getStakingMap());
        }
        committeeBlock.setCommitteeProposer(new int[committeeBlock.getStakingMap().size()]);
        committeeBlock.setGeneration(CachedLatestBlocks.getInstance().getCommitteeBlock().getGeneration() + 1);
        committeeBlock.getHeaderData().setPreviousHash(CachedLatestBlocks.getInstance().getCommitteeBlock().getHash());
        committeeBlock.setHeight(CachedLatestBlocks.getInstance().getCommitteeBlock().getHeight() + 1);
        committeeBlock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        committeeBlock.setVRF(Hex.toHexString(CachedSecurityHeaders.getInstance().getSecurityHeader().getpRnd()));


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
        for (Map.Entry<Double, KademliaData> entry : committeeBlock.getStakingMap().entrySet()) {
            int nextInt = CustomRandom.generateRandom(zone_random, 0, committeeBlock.getStakingMap().size() - 1, exclude);
            if (!exclude.contains(nextInt)) {
                exclude.add(nextInt);
            }
            order.add(nextInt);
        }
        int zone_count = 0;
        List<Map.Entry<Double, KademliaData>> entryList = committeeBlock.getStakingMap().entrySet().stream().collect(Collectors.toList());
        int MAX_ZONE_SIZE = committeeBlock.getStakingMap().size() / 4;

        if (MAX_ZONE_SIZE >= 2) {
            int j = 0;
            while (zone_count < 4) {
                int index_count = 0;
                if (committeeBlock.getStakingMap().size() % 4 != 0 && zone_count == 0) {
                    while (index_count < committeeBlock.getStakingMap().size() - 3) {
                        committeeBlock
                                .getStructureMap()
                                .get(zone_count)
                                .put(entryList.get(order.get(j)).getValue().getAddressData().getValidatorBlSPublicKey(), entryList.get(order.get(j)).getValue().getNettyConnectionInfo().getHost());
                        index_count++;
                        j++;
                    }
                    zone_count++;
                }
                index_count = 0;
                while (index_count < MAX_ZONE_SIZE) {
                    committeeBlock
                            .getStructureMap()
                            .get(zone_count)
                            .put(entryList.get(order.get(j)).getValue().getAddressData().getValidatorBlSPublicKey(), entryList.get(order.get(j)).getValue().getNettyConnectionInfo().getHost());
                    index_count++;
                    j++;
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
        String hash = HashUtil.sha256_bytetoString(encode.encode(committeeBlock));
        committeeBlock.setHash(hash);
    }

    @Override
    public void InventTransactionBlock(TransactionBlock transactionBlock) {

        IDatabase<String, TransactionBlock> block_database = new DatabaseFactory(String.class, TransactionBlock.class)
                .getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
        IDatabase<String, byte[]> tree_datasbase = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getPatriciaTreeZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));


        block_database.save(transactionBlock.getHash(), transactionBlock);
        tree_datasbase.save(transactionBlock.getHash(), SerializationUtils.serialize(TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex())));


        MemoryTransactionPool.getInstance().delete(transactionBlock.getTransactionList());

        if (!transactionBlock.getTransactionList().isEmpty()) {
            for (int i = 0; i < transactionBlock.getTransactionList().size(); i++) {
                Transaction transaction = transactionBlock.getTransactionList().get(i);
                if ((transaction.getZoneFrom() == CachedZoneIndex.getInstance().getZoneIndex()) && (transaction.getZoneTo() == CachedZoneIndex.getInstance().getZoneIndex())) {
                    TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).withdraw(transaction.getFrom(), transaction.getAmount(), TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()));
                    TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).deposit(transaction.getTo(), transaction.getAmount(), TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()));
                } else {
                    TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).withdraw(transaction.getFrom(), transaction.getAmount(), TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()));
                }
            }
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
                            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).deposit(receipt.getAddress(), receipt.getAmount(), TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()));
                            MemoryReceiptPool.getInstance().delete(receipt);
                        });

                    });

        CachedLatestBlocks.getInstance().setTransactionBlock(transactionBlock);
        CachedReceiptSemaphore.getInstance().getSemaphore().release();
    }

    @Override
    public void InventCommitteBlock(CommitteeBlock committeeBlock) {

    }

    //invent
}
