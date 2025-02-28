package io.Adrestus.core.BlockPipeline.FlowChain.Transaction;

import io.Adrestus.TreeFactory;
import io.Adrestus.core.BlockPipeline.BlockRequest;
import io.Adrestus.core.BlockPipeline.BlockRequestHandler;
import io.Adrestus.core.BlockPipeline.BlockRequestType;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.core.Util.BlockSizeCalculator;
import io.Adrestus.util.SerializationFuryUtil;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import io.distributedLedger.ZoneDatabaseFactory;
import lombok.SneakyThrows;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InventDatabaseStorageBuilder implements BlockRequestHandler<TransactionBlock> {


    @Override
    public boolean canHandleRequest(BlockRequest<TransactionBlock> req) {
        return req.getRequestType() == BlockRequestType.INVENT_DATABASE_STORAGE_BUILDER;
    }

    @Override
    public int getPriority() {
        return 5;
    }

    @Override
    @SneakyThrows
    public void process(BlockRequest<TransactionBlock> blockRequest) {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        CountDownLatch countDownLatch = new CountDownLatch(2);
        Runnable TransactionSave = () -> {
            IDatabase<String, TransactionBlock> block_database = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
            BlockSizeCalculator blockSizeCalculator = new BlockSizeCalculator();
            blockSizeCalculator.setTransactionBlock(blockRequest.getBlock());
            block_database.save(String.valueOf(blockRequest.getBlock().getHeight()), blockRequest.getBlock(), blockSizeCalculator.TransactionBlockSizeCalculator());
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
        executor.shutdown();
        executor.shutdownNow();
        executor.close();
    }

    @Override
    public void clear(BlockRequest<TransactionBlock> blockRequest) {
        blockRequest.clear();
    }

    @Override
    public String name() {
        return "InventDatabaseStorageBuilder";
    }
}
