package io.Adrestus.core;

import com.google.common.reflect.TypeToken;
import io.Adrestus.MemoryTreePool;
import io.Adrestus.TreeFactory;
import io.Adrestus.config.ConsensusConfiguration;
import io.Adrestus.config.KademliaConfiguration;
import io.Adrestus.config.NetworkConfiguration;
import io.Adrestus.config.SocketConfigOptions;
import io.Adrestus.core.Resourses.*;
import io.Adrestus.mapper.MemoryTreePoolSerializer;
import io.Adrestus.network.AsyncService;
import io.Adrestus.rpc.RpcAdrestusClient;
import io.Adrestus.util.SerializationUtil;
import io.activej.eventloop.Eventloop;
import io.distributedLedger.*;
import lombok.SneakyThrows;

import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

public class BlockSync implements IBlockSync {
    private final SerializationUtil<Transaction> transaction_encode;
    private final SerializationUtil<Receipt> receipt_encode;

    private final SerializationUtil patricia_tree_wrapper;

    private final Eventloop eventloop;


    public BlockSync() {
        Type fluentType = new TypeToken<MemoryTreePool>() {
        }.getType();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        this.patricia_tree_wrapper = new SerializationUtil<>(fluentType, list);
        this.transaction_encode = new SerializationUtil<Transaction>(Transaction.class);
        this.receipt_encode = new SerializationUtil<Receipt>(Receipt.class);
        this.eventloop = Eventloop.create().withCurrentThread();
    }

    @Override
    @SneakyThrows
    public void WaitPatientlyYourPosition() {
        boolean result = false;
        do {
            RpcAdrestusClient client = new RpcAdrestusClient(new CommitteeBlock(), new InetSocketAddress(InetAddress.getByName(KademliaConfiguration.BOOTSTRAP_NODE_IP), NetworkConfiguration.RPC_PORT), eventloop);
            IDatabase<String, CommitteeBlock> committee_database = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);
            Optional<CommitteeBlock> last_block = committee_database.seekLast();
            Map<String, CommitteeBlock> toSave = new HashMap<>();
            List<CommitteeBlock> blocks;
            if (last_block.isPresent()) {
                blocks = client.getBlocksList(String.valueOf(last_block.get().getHeight()));
                if (!blocks.isEmpty()) {
                    blocks.stream().skip(1).forEach(val -> toSave.put(String.valueOf(val.getHeight()), val));
                }

            } else {
                blocks = client.getBlocksList("");
                if (!blocks.isEmpty()) {
                    blocks.stream().forEach(val -> toSave.put(String.valueOf(val.getHeight()), val));
                }
            }
            committee_database.saveAll(toSave);
            CachedLatestBlocks.getInstance().setCommitteeBlock(blocks.get(blocks.size() - 1));
            CachedLeaderIndex.getInstance().setCommitteePositionLeader(0);
            CachedZoneIndex.getInstance().setZoneIndexInternalIP();
            result = CachedZoneIndex.getInstance().isNodeExistOnBlockInternal();
            if (!result)
                Thread.sleep((ConsensusConfiguration.EPOCH_TRANSITION - 2) * ConsensusConfiguration.CONSENSUS_TIMER);
        } while (!result);

        List<String> new_ips = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).values().stream().collect(Collectors.toList());
        int RPCTransactionZonePort = ZoneDatabaseFactory.getDatabaseRPCPort(CachedZoneIndex.getInstance().getZoneIndex());
        int RPCPatriciaTreeZonePort = ZoneDatabaseFactory.getDatabasePatriciaRPCPort(ZoneDatabaseFactory.getPatriciaTreeZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
        ArrayList<InetSocketAddress> toConnectTransaction = new ArrayList<>();
        ArrayList<InetSocketAddress> toConnectPatricia = new ArrayList<>();
        new_ips.stream().forEach(ip -> {
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
            client = new RpcAdrestusClient(new TransactionBlock(), toConnectTransaction, eventloop);
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
            client = new RpcAdrestusClient(new byte[]{}, toConnectPatricia, eventloop);
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
                if (!treeObjects.isEmpty()) {
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
        List<CommitteeBlock> commitee_blocks;
        do {
            RpcAdrestusClient client = new RpcAdrestusClient(new CommitteeBlock(), toConnectCommittee, eventloop);
            client.connect();

            commitee_blocks = client.getBlocksList(String.valueOf(CachedLatestBlocks.getInstance().getCommitteeBlock().getHeight()));

            if (client != null)
                client.close();

            Thread.sleep(1000);
        } while (commitee_blocks.size() <= 1);

        CachedLatestBlocks.getInstance().setCommitteeBlock(commitee_blocks.get(commitee_blocks.size() - 1));
        CachedLeaderIndex.getInstance().setCommitteePositionLeader(0);
        IDatabase<String, CommitteeBlock> database = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);
        database.save(String.valueOf(CachedLatestBlocks.getInstance().getCommitteeBlock().getHeight()), CachedLatestBlocks.getInstance().getCommitteeBlock());
        CachedZoneIndex.getInstance().setZoneIndexInternalIP();

        List<String> new_ips = prevblock.getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).values().stream().collect(Collectors.toList());
        int RPCTransactionZonePort = ZoneDatabaseFactory.getDatabaseRPCPort(CachedZoneIndex.getInstance().getZoneIndex());
        int RPCPatriciaTreeZonePort = ZoneDatabaseFactory.getDatabasePatriciaRPCPort(ZoneDatabaseFactory.getPatriciaTreeZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
        ArrayList<InetSocketAddress> toConnectTransaction = new ArrayList<>();
        ArrayList<InetSocketAddress> toConnectPatricia = new ArrayList<>();
        new_ips.stream().forEach(ip -> {
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
            client = new RpcAdrestusClient(new TransactionBlock(), toConnectTransaction, eventloop);
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
            client = new RpcAdrestusClient(new byte[]{}, toConnectPatricia, eventloop);
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
                if (!treeObjects.isEmpty()) {
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

        //find transactions that is not for this zone and sent them to the correct zone
        List<Transaction> transactionList = MemoryTransactionPool.getInstance().getListByZone(prevZone);
        List<byte[]> toSend = new ArrayList<>();
        transactionList.stream().forEach(transaction -> toSend.add(transaction_encode.encode(transaction, 1024)));
        List<String> iptoSend = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(prevZone).values().stream().collect(Collectors.toList());

        if (!toSend.isEmpty()) {
            var executor = new AsyncService<Long>(iptoSend, toSend, SocketConfigOptions.TRANSACTION_PORT);

            var asyncResult = executor.startListProcess(300L);
            var result = executor.endProcess(asyncResult);
        }

        //find receipts that is not for this zone and sent them to the correct zone
        List<Receipt> receiptList = MemoryReceiptPool.getInstance().getListByZone(prevZone);
        List<byte[]> toSendReceipt = new ArrayList<>();
        receiptList.stream().forEach(receipt -> toSendReceipt.add(receipt_encode.encode(receipt, 1024)));
        List<String> ReceiptIPWorkers = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(prevZone).values().stream().collect(Collectors.toList());

        if (!toSendReceipt.isEmpty()) {
            var executor = new AsyncService<Long>(ReceiptIPWorkers, toSendReceipt, SocketConfigOptions.RECEIPT_PORT);

            var asyncResult = executor.startListProcess(300L);
            var result = executor.endProcess(asyncResult);
            MemoryReceiptPool.getInstance().delete(transactionList);
        }
    }

}
