package io.Adrestus.core.BlockPipeline.FlowChain.Committee;

import io.Adrestus.MemoryTreePool;
import io.Adrestus.TreeFactory;
import io.Adrestus.core.BlockPipeline.BlockRequest;
import io.Adrestus.core.BlockPipeline.BlockRequestHandler;
import io.Adrestus.core.BlockPipeline.BlockRequestType;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedLeaderIndex;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.network.CachedEventLoop;
import io.Adrestus.rpc.RpcAdrestusClient;
import io.Adrestus.util.SerializationFuryUtil;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import io.distributedLedger.ZoneDatabaseFactory;
import lombok.SneakyThrows;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

public class InventSyncBlocksBuilder implements BlockRequestHandler<CommitteeBlock> {


    public InventSyncBlocksBuilder() {
    }

    @Override
    public boolean canHandleRequest(BlockRequest<CommitteeBlock> req) {
        return req.getRequestType() == BlockRequestType.INVENT_SYNC_BLOCKS_BUILDER;
    }

    @Override
    public int getPriority() {
        return 5;
    }

    @Override
    @SneakyThrows
    public void process(BlockRequest<CommitteeBlock> blockRequest) {
        //sync blocks from zone of previous validators for both transaction and patricia tree blocks
        List<String> ips = blockRequest.getBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).values().stream().collect(Collectors.toList());
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
    }

    @Override
    public String name() {
        return "InventSyncBlocksBuilder";
    }

    @Override
    public void clear(BlockRequest<CommitteeBlock> blockRequest) {
        blockRequest.clear();
    }
}
