package io.Adrestus.core;

import com.google.common.reflect.TypeToken;
import io.Adrestus.MemoryTreePool;
import io.Adrestus.TreeFactory;
import io.Adrestus.config.ConsensusConfiguration;
import io.Adrestus.config.KademliaConfiguration;
import io.Adrestus.config.NetworkConfiguration;
import io.Adrestus.config.SocketConfigOptions;
import io.Adrestus.core.Resourses.*;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomSerializerTreeMap;
import io.Adrestus.mapper.MemoryTreePoolSerializer;
import io.Adrestus.network.AsyncService;
import io.Adrestus.network.AsyncServiceNetworkData;
import io.Adrestus.network.CachedEventLoop;
import io.Adrestus.network.IPFinder;
import io.Adrestus.rpc.RpcAdrestusClient;
import io.Adrestus.util.SerializationUtil;
import io.distributedLedger.*;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

public class BlockSync implements IBlockSync {
    private static Logger LOG = LoggerFactory.getLogger(BlockSync.class);
    private final SerializationUtil<Transaction> transaction_encode;
    private final SerializationUtil<Receipt> receipt_encode;
    private static SerializationUtil<CachedNetworkData> serialize_cached;
    private final SerializationUtil patricia_tree_wrapper;


    public BlockSync() {
        Type fluentType = new TypeToken<MemoryTreePool>() {
        }.getType();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        List<SerializationUtil.Mapping> list2 = new ArrayList<>();
        list2.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list2.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list2.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list2.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        serialize_cached = new SerializationUtil<CachedNetworkData>(CachedNetworkData.class, list2);
        this.patricia_tree_wrapper = new SerializationUtil<>(fluentType, list);
        this.transaction_encode = new SerializationUtil<Transaction>(Transaction.class, list2);
        this.receipt_encode = new SerializationUtil<Receipt>(Receipt.class, list2);
    }

    @Override
    @SneakyThrows
    public void WaitPatientlyYourPosition() {
        boolean result = false;
        List<CommitteeBlock> blocks = null;
        do {
            IDatabase<String, CommitteeBlock> committee_database = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);
            Optional<CommitteeBlock> last_block = committee_database.seekLast();
            Map<String, CommitteeBlock> toSave = new HashMap<>();
            do {
                RpcAdrestusClient client = new RpcAdrestusClient(new CommitteeBlock(), new InetSocketAddress(InetAddress.getByName(KademliaConfiguration.BOOTSTRAP_NODE_IP), NetworkConfiguration.RPC_PORT), CachedEventLoop.getInstance().getEventloop());
                client.connect();
                try {
                    if (last_block.isPresent()) {
                        blocks = client.getBlocksList(String.valueOf(last_block.get().getHeight()));
                        if (!blocks.isEmpty() && blocks.size() > 1) {
                            blocks.stream().skip(1).forEach(val -> toSave.put(String.valueOf(val.getHeight()), val));
                        }

                    } else {
                        blocks = client.getBlocksList("");
                        if (!blocks.isEmpty()) {
                            blocks.stream().forEach(val -> toSave.put(String.valueOf(val.getHeight()), val));
                        }
                    }
                } catch (NullPointerException e) {
                    Thread.sleep(ConsensusConfiguration.CONSENSUS_WAIT_TIMEOUT);
                } finally {
                    if (client != null) {
                        client.close();
                        client = null;
                    }

                }
            } while (blocks == null);
            committee_database.saveAll(toSave);
            CachedLatestBlocks.getInstance().setCommitteeBlock(blocks.get(blocks.size() - 1));
            CachedLeaderIndex.getInstance().setCommitteePositionLeader(0);
            CachedEpochGeneration.getInstance().setEpoch_counter(0);
            CachedZoneIndex.getInstance().setZoneIndexInternalIP();
            result = CachedZoneIndex.getInstance().isNodeExistOnBlockInternal();
            if (!result)
                Thread.sleep(ConsensusConfiguration.CONSENSUS_WAIT_TIMEOUT);
        } while (!result);

        CommitteeBlock prev_block = blocks.get(blocks.size() - 2);
        List<String> old_ips = prev_block.getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).values().stream().collect(Collectors.toList());
        if (old_ips.isEmpty()) {
            IDatabase<String, TransactionBlock> block_database = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
            IDatabase<String, byte[]> tree_database = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getPatriciaTreeZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));

            Optional<TransactionBlock> block = block_database.seekLast();
            Optional<byte[]> tree = tree_database.seekLast();

            CachedLatestBlocks.getInstance().setTransactionBlock(block.get());
            TreeFactory.setMemoryTree((MemoryTreePool) patricia_tree_wrapper.decode(tree.get()), CachedZoneIndex.getInstance().getZoneIndex());
        } else {
            old_ips.remove(IPFinder.getLocalIP());
            int RPCTransactionZonePort = ZoneDatabaseFactory.getDatabaseRPCPort(CachedZoneIndex.getInstance().getZoneIndex());
            int RPCPatriciaTreeZonePort = ZoneDatabaseFactory.getDatabasePatriciaRPCPort(ZoneDatabaseFactory.getPatriciaTreeZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
            ArrayList<InetSocketAddress> toConnectTransaction = new ArrayList<>();
            ArrayList<InetSocketAddress> toConnectPatricia = new ArrayList<>();
            old_ips.stream().forEach(ip -> {
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
                List<TransactionBlock> transactionBlocks;
                if (block.isPresent()) {
                    transactionBlocks = client.getBlocksList(String.valueOf(block.get().getHeight()));
                    if (!transactionBlocks.isEmpty() && transactionBlocks.size() > 1) {
                        transactionBlocks.stream().skip(1).forEach(val -> toSave.put(String.valueOf(val.getHeight()), val));
                    }

                } else {
                    transactionBlocks = client.getBlocksList("");
                    if (!transactionBlocks.isEmpty()) {
                        transactionBlocks.stream().forEach(val -> toSave.put(String.valueOf(val.getHeight()), val));
                    }
                }

                block_database.saveAll(toSave);

                if (!transactionBlocks.isEmpty()) {
                    CachedLatestBlocks.getInstance().setTransactionBlock(transactionBlocks.get(transactionBlocks.size() - 1));
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

                Optional<byte[]> tree = tree_database.seekLast();
                List<byte[]> treeObjects;
                if (tree.isPresent()) {
                    treeObjects = client.getPatriciaTreeList(((MemoryTreePool) patricia_tree_wrapper.decode(tree.get())).getRootHash());
                } else {
                    treeObjects = client.getPatriciaTreeList("");
                }
                Map<String, byte[]> toSave = new HashMap<>();
                if (tree.isPresent()) {
                    if (!treeObjects.isEmpty() && treeObjects.size() > 1) {
                        treeObjects.stream().skip(1).forEach(val -> {
                            try {
                                toSave.put(((MemoryTreePool) patricia_tree_wrapper.decode(val)).getRootHash(), val);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                } else {
                    if (!treeObjects.isEmpty()) {
                        treeObjects.stream().forEach(val -> {
                            try {
                                toSave.put(((MemoryTreePool) patricia_tree_wrapper.decode(val)).getRootHash(), val);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                }
                tree_database.saveAll(toSave);
                if (!treeObjects.isEmpty()) {
                    TreeFactory.setMemoryTree((MemoryTreePool) patricia_tree_wrapper.decode(treeObjects.get(treeObjects.size() - 1)), CachedZoneIndex.getInstance().getZoneIndex());
                }
                if (client != null)
                    client.close();
            } catch (IllegalArgumentException e) {
            }
            //send request to receive cached Data used for consensus
            boolean bError = false;
            do {
                try {
                    //make sure you are wait for validators of different zone than 0 to sync first
                    Thread.sleep(800);

                    var ex = new AsyncServiceNetworkData<Long>(old_ips);

                    var asyncResult = ex.startProcess(300L);
                    var cached_result = ex.endProcess(asyncResult);

                    CachedNetworkData networkData = serialize_cached.decode(ex.getResult());

                    //only on this function
                    networkData.setSecurityHeader();
                } catch (NoSuchElementException ex) {
                    LOG.info("NoSuchElementException: " + ex.toString());
                    Thread.sleep(ConsensusConfiguration.CONSENSUS_WAIT_TIMEOUT);
                    bError = true;
                }
            } while (bError);
        }
    }

    @Override
    @SneakyThrows
    public void SyncState() {
        CommitteeBlock prevblock = (CommitteeBlock) CachedLatestBlocks.getInstance().getCommitteeBlock().clone();
        int prevZone = Integer.valueOf(CachedZoneIndex.getInstance().getZoneIndex());
        List<String> ips = prevblock.getStructureMap().get(0).values().stream().collect(Collectors.toList());
        ArrayList<InetSocketAddress> toConnectCommittee = new ArrayList<>();
        ips.stream().forEach(ip -> {
            try {
                toConnectCommittee.add(new InetSocketAddress(InetAddress.getByName(ip), NetworkConfiguration.RPC_PORT));
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        });

        RpcAdrestusClient client1 = new RpcAdrestusClient(new CommitteeBlock(), toConnectCommittee, CachedEventLoop.getInstance().getEventloop());
        client1.connect();

        List<CommitteeBlock> commitee_blocks = client1.getBlocksList(String.valueOf(CachedLatestBlocks.getInstance().getCommitteeBlock().getHeight()));

        if (client1 != null) {
            client1.close();
            client1 = null;
        }

        if (commitee_blocks.size() <= 1)
            return;


        CachedLatestBlocks.getInstance().setCommitteeBlock(commitee_blocks.get(commitee_blocks.size() - 1));
        CachedLeaderIndex.getInstance().setCommitteePositionLeader(0);
        CachedLeaderIndex.getInstance().setTransactionPositionLeader(0);
        CachedEpochGeneration.getInstance().setEpoch_counter(0);
        boolean isNodeExist = CachedZoneIndex.getInstance().isNodeExistOnBlockInternal();
        IDatabase<String, CommitteeBlock> database = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);
        database.save(String.valueOf(CachedLatestBlocks.getInstance().getCommitteeBlock().getHeight()), CachedLatestBlocks.getInstance().getCommitteeBlock());

        if (!isNodeExist) {
            LOG.info("Node not existed on Committee block WaitPatientlyYourPosition");
            this.WaitPatientlyYourPosition();
        }
        CachedZoneIndex.getInstance().setZoneIndexInternalIP();

        List<String> new_ips = prevblock.getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).values().stream().collect(Collectors.toList());
        if (new_ips.isEmpty()) {
            IDatabase<String, TransactionBlock> block_database = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
            IDatabase<String, byte[]> tree_database = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getPatriciaTreeZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));

            Optional<TransactionBlock> block = block_database.seekLast();
            Optional<byte[]> tree = tree_database.seekLast();

            CachedLatestBlocks.getInstance().setTransactionBlock(block.get());
            TreeFactory.setMemoryTree((MemoryTreePool) patricia_tree_wrapper.decode(tree.get()), CachedZoneIndex.getInstance().getZoneIndex());
        } else {
            new_ips.remove(IPFinder.getLocalIP());
            int RPCTransactionZonePort = ZoneDatabaseFactory.getDatabaseRPCPort(CachedZoneIndex.getInstance().getZoneIndex());
            int RPCPatriciaTreeZonePort = ZoneDatabaseFactory.getDatabasePatriciaRPCPort(ZoneDatabaseFactory.getPatriciaTreeZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
            ArrayList<InetSocketAddress> toConnectTransaction = new ArrayList<>();
            ArrayList<InetSocketAddress> toConnectPatricia = new ArrayList<>();
            ArrayList<InetSocketAddress> toConnectZoneCommittee = new ArrayList<>();
            new_ips.stream().forEach(ip -> {
                try {
                    toConnectTransaction.add(new InetSocketAddress(InetAddress.getByName(ip), RPCTransactionZonePort));
                    toConnectPatricia.add(new InetSocketAddress(InetAddress.getByName(ip), RPCPatriciaTreeZonePort));
                    toConnectZoneCommittee.add(new InetSocketAddress(InetAddress.getByName(ip), NetworkConfiguration.RPC_PORT));
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
            });

            do {
                Thread.sleep(500);
                RpcAdrestusClient client = new RpcAdrestusClient(new CommitteeBlock(), toConnectZoneCommittee, CachedEventLoop.getInstance().getEventloop());
                client.connect();

                commitee_blocks = client.getBlocksList(String.valueOf(CachedLatestBlocks.getInstance().getCommitteeBlock().getHeight()));

                if (client != null) {
                    client.close();
                    client = null;
                }
                if (!commitee_blocks.isEmpty()) {
                    if (CachedLatestBlocks.getInstance().getCommitteeBlock().getHash().equals(commitee_blocks.get(commitee_blocks.size() - 1).getHash()))
                        break;
                }
            } while (commitee_blocks.isEmpty());

            RpcAdrestusClient client = null;
            List<String> patriciaRootList = null;
            try {
                IDatabase<String, TransactionBlock> block_database = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
                client = new RpcAdrestusClient(new TransactionBlock(), toConnectTransaction, CachedEventLoop.getInstance().getEventloop());
                client.connect();

                Optional<TransactionBlock> block = block_database.seekLast();
                Map<String, TransactionBlock> toSave = new HashMap<>();
                List<TransactionBlock> blocks;
                if (block.isPresent()) {
                    blocks = client.getBlocksList(String.valueOf(block.get().getHeight()));
                    if (!blocks.isEmpty() && blocks.size() > 1) {
                        patriciaRootList = new ArrayList<>(blocks.stream().filter(val -> val.getGeneration() > CachedLatestBlocks.getInstance().getCommitteeBlock().getGeneration()).map(TransactionBlock::getHash).collect(Collectors.toList()));
                        blocks.removeIf(x -> x.getGeneration() > CachedLatestBlocks.getInstance().getCommitteeBlock().getGeneration());
                        blocks.stream().skip(1).forEach(val -> toSave.put(String.valueOf(val.getHeight()), val));
                    }

                } else {
                    blocks = client.getBlocksList("");
                    if (!blocks.isEmpty()) {
                        patriciaRootList = new ArrayList<>(blocks.stream().filter(val -> val.getGeneration() > CachedLatestBlocks.getInstance().getCommitteeBlock().getGeneration()).map(TransactionBlock::getHash).collect(Collectors.toList()));
                        blocks.removeIf(x -> x.getGeneration() > CachedLatestBlocks.getInstance().getCommitteeBlock().getGeneration());
                        blocks.stream().forEach(val -> toSave.put(String.valueOf(val.getHeight()), val));
                    }
                }

                block_database.saveAll(toSave);

                if (!blocks.isEmpty()) {
                    CachedLatestBlocks.getInstance().setTransactionBlock(blocks.get(blocks.size() - 1));
                }
                if (client != null) {
                    client.close();
                    client = null;
                }
            } catch (IllegalArgumentException e) {
            }


            try {
                IDatabase<String, byte[]> tree_database = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getPatriciaTreeZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
                client = new RpcAdrestusClient(new byte[]{}, toConnectPatricia, CachedEventLoop.getInstance().getEventloop());
                client.connect();

                Optional<byte[]> tree = tree_database.seekLast();
                List<byte[]> treeObjects;
                if (tree.isPresent()) {
                    treeObjects = client.getPatriciaTreeList(((MemoryTreePool) patricia_tree_wrapper.decode(tree.get())).getRootHash());
                } else {
                    treeObjects = client.getPatriciaTreeList("");
                }
                Map<String, byte[]> toSave = new HashMap<>();
                if (tree.isPresent()) {
                    if (!treeObjects.isEmpty() && treeObjects.size() > 1) {
                        treeObjects.stream().skip(1).forEach(val -> {
                            try {
                                toSave.put(((MemoryTreePool) patricia_tree_wrapper.decode(val)).getRootHash(), val);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                } else {
                    if (!treeObjects.isEmpty()) {
                        treeObjects.stream().forEach(val -> {
                            try {
                                toSave.put(((MemoryTreePool) patricia_tree_wrapper.decode(val)).getRootHash(), val);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                }
                List<String> finalPatriciaRootList = patriciaRootList;
                Map<String, byte[]> toCollect = toSave.entrySet().stream()
                        .filter(x -> !finalPatriciaRootList.contains(x.getKey()))
                        .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
                tree_database.saveAll(toCollect);
                byte[] current_tree = toCollect.get(CachedLatestBlocks.getInstance().getTransactionBlock().getPatriciaMerkleRoot());
                if (!toCollect.isEmpty()) {
                    TreeFactory.setMemoryTree((MemoryTreePool) patricia_tree_wrapper.decode(current_tree), CachedZoneIndex.getInstance().getZoneIndex());
                } else if (!treeObjects.isEmpty()) {
                    TreeFactory.setMemoryTree((MemoryTreePool) patricia_tree_wrapper.decode(treeObjects.get(treeObjects.size() - 1)), CachedZoneIndex.getInstance().getZoneIndex());
                }

                if (client != null) {
                    client.close();
                    client = null;
                }
            } catch (IllegalArgumentException e) {
            }
        }
        //find transactions that is not for this zone and sent them to the correct zone
        List<Transaction> transactionList = MemoryTransactionPool.getInstance().getListByZone(prevZone);
        if (!transactionList.isEmpty()) {
            List<byte[]> toSend = new ArrayList<>();
            transactionList.stream().forEach(transaction -> toSend.add(transaction_encode.encode(transaction, 1024)));
            List<String> iptoSend = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(prevZone).values().stream().collect(Collectors.toList());

            if (!toSend.isEmpty()) {
                var executor = new AsyncService<Long>(iptoSend, toSend, SocketConfigOptions.TRANSACTION_PORT);

                var asyncResult = executor.startListProcess(300L);
                var result = executor.endProcess(asyncResult);
                MemoryTransactionPool.getInstance().delete(transactionList);
            }
        }

        //find receipts that is not for this zone and sent them to the correct zone
        List<Receipt> receiptList = MemoryReceiptPool.getInstance().getListByZone(prevZone);
        if (!receiptList.isEmpty()) {
            List<byte[]> toSendReceipt = new ArrayList<>();
            receiptList.stream().forEach(receipt -> toSendReceipt.add(receipt_encode.encode(receipt, 1024)));
            List<String> ReceiptIPWorkers = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(prevZone).values().stream().collect(Collectors.toList());

            if (!toSendReceipt.isEmpty()) {
                var executor = new AsyncService<Long>(ReceiptIPWorkers, toSendReceipt, SocketConfigOptions.RECEIPT_PORT);

                var asyncResult = executor.startListProcess(300L);
                var result = executor.endProcess(asyncResult);
                MemoryReceiptPool.getInstance().delete(receiptList);
            }
        }
    }

    @Override
    @SneakyThrows
    public void checkIfNeedsSync() {
        List<String> new_ips = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).values().stream().collect(Collectors.toList());
        new_ips.remove(IPFinder.getLocalIP());
        boolean bError = false;
        do {
            try {

                var ex = new AsyncServiceNetworkData<Long>(new_ips);

                var asyncResult = ex.startProcess(300L);
                var cached_result = ex.endProcess(asyncResult);

                CachedNetworkData networkData = serialize_cached.decode(ex.getResult());
                if (!networkData.getCommitteeBlock().equals(CachedLatestBlocks.getInstance().getCommitteeBlock())) {
                    this.WaitPatientlyYourPosition();
                    return;
                } else {
                    if (networkData.isConsensus_state())
                        networkData.SetCacheData();
                }
            } catch (NoSuchElementException ex) {
                LOG.error("NoSuchElementException: " + ex.toString());
                Thread.sleep(ConsensusConfiguration.CONSENSUS_WAIT_TIMEOUT);
                bError = true;
            }
        } while (bError);
    }

    @Override
    public void syncCommitBlock() {
        List<String> ips = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).values().stream().collect(Collectors.toList());
        ArrayList<InetSocketAddress> toConnectCommittee = new ArrayList<>();
        ips.stream().forEach(ip -> {
            try {
                toConnectCommittee.add(new InetSocketAddress(InetAddress.getByName(ip), NetworkConfiguration.RPC_PORT));
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        });

        RpcAdrestusClient client1 = new RpcAdrestusClient(new CommitteeBlock(), toConnectCommittee, CachedEventLoop.getInstance().getEventloop());
        client1.connect();

        List<CommitteeBlock> commitee_blocks = client1.getBlocksList(String.valueOf(CachedLatestBlocks.getInstance().getCommitteeBlock().getHeight()));

        if (client1 != null) {
            client1.close();
            client1 = null;
        }

        if (commitee_blocks.size() <= 1)
            return;


        CachedLatestBlocks.getInstance().setCommitteeBlock(commitee_blocks.get(commitee_blocks.size() - 1));
    }

}
