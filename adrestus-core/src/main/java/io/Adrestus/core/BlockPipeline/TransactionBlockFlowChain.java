package io.Adrestus.core.BlockPipeline;

import io.Adrestus.core.BlockPipeline.FlowChain.Transaction.*;
import io.Adrestus.core.TransactionBlock;

import java.util.ArrayList;
import java.util.List;

public class TransactionBlockFlowChain extends BlockFlowChain<TransactionBlock> {
    private final List<BlockRequest<TransactionBlock>> blockInventRequests;
    private final List<BlockRequest<TransactionBlock>> blockForgeRequests;

    public TransactionBlockFlowChain() {
        super(List.of(
                        new ForgeInitializerBuilder(),
                        new ForgeDifferentOriginBuilder(),
                        new ForgeFeeRewardBuilder(),
                        new ForgeOutBoundBuilder(),
                        new ForgeInBoundBuilder(),
                        new ForgePatriciaTreeBuilder(),
                        new ForgeSerializerBlockBuilder()),
                List.of(new InventInitializerBuilder(),
                        new InventTreePoolBuilder(),
                        new InventInBoundBuilder(),
                        new InventOutBoundBuilder(),
                        new InventRewardsBuilder(),
                        new InventDatabaseStorageBuilder(),
                        new InventCacheBuilder()));
        this.blockInventRequests = new ArrayList<>();
        this.blockForgeRequests = new ArrayList<>();
    }


    @Override
    public void makeInventorRequest(BlockRequest<TransactionBlock> req) {
        this.blockInventRequests.add(req);
        super.makeInventorRequest(req);
    }

    @Override
    public void makeForgeHandlersRequest(BlockRequest<TransactionBlock> req) {
        this.blockForgeRequests.add(req);
        super.makeForgeHandlersRequest(req);
    }

    @Override
    public void clear() {
        this.blockInventRequests.forEach(super::cleanInventHandlersRequest);
        this.blockForgeRequests.forEach(super::cleanForgeHandlersRequest);
        this.blockInventRequests.clear();
        this.blockForgeRequests.clear();
    }
}
