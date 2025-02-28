package io.Adrestus.core.BlockPipeline.FlowChain.Transaction;

import io.Adrestus.core.BlockPipeline.BlockRequest;
import io.Adrestus.core.BlockPipeline.BlockRequestHandler;
import io.Adrestus.core.BlockPipeline.BlockRequestType;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.MemoryTransactionPool;
import io.Adrestus.core.TransactionBlock;
import lombok.SneakyThrows;

public class InventCacheBuilder implements BlockRequestHandler<TransactionBlock> {


    @Override
    public boolean canHandleRequest(BlockRequest<TransactionBlock> req) {
        return req.getRequestType() == BlockRequestType.INVENT_CACHE_BUILDER;
    }

    @Override
    public int getPriority() {
        return 6;
    }

    @Override
    @SneakyThrows
    public void process(BlockRequest<TransactionBlock> blockRequest) {
        TransactionBlock clonable = blockRequest.getBlock().clone();
        CachedLatestBlocks.getInstance().setTransactionBlock(clonable);
        MemoryTransactionPool.getInstance().delete(clonable.getTransactionList());
    }

    @Override
    public String name() {
        return "InventCacheBuilder";
    }

    @Override
    public void clear(BlockRequest<TransactionBlock> blockRequest) {
        blockRequest.clear();
    }
}
