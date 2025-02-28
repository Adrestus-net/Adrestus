package io.Adrestus.core.BlockPipeline.FlowChain.Transaction;

import io.Adrestus.TreeFactory;
import io.Adrestus.core.BlockPipeline.BlockRequest;
import io.Adrestus.core.BlockPipeline.BlockRequestHandler;
import io.Adrestus.core.BlockPipeline.BlockRequestType;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.core.TreePoolConstructBlock;
import lombok.SneakyThrows;

public class InventTreePoolBuilder implements BlockRequestHandler<TransactionBlock> {
    @Override
    public boolean canHandleRequest(BlockRequest<TransactionBlock> req) {
        return req.getRequestType() == BlockRequestType.INVENT_TREE_POOL_BUILDER;
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    @SneakyThrows
    public void process(BlockRequest<TransactionBlock> blockRequest) {
        if (!blockRequest.getBlock().getTransactionList().isEmpty()) {
            TreePoolConstructBlock.getInstance().visitInventTreePool(blockRequest.getBlock(), TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()));
        }
    }

    @Override
    public void clear(BlockRequest<TransactionBlock> blockRequest) {
        blockRequest.clear();
    }

    @Override
    public String name() {
        return "InventTreePoolBuilder";
    }
}
