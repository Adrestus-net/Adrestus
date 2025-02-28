package io.Adrestus.core.BlockPipeline;

import io.Adrestus.core.BlockPipeline.FlowChain.Committee.*;
import io.Adrestus.core.CommitteeBlock;

import java.util.ArrayList;
import java.util.List;

public class CommitteeBlockFlowChain extends BlockFlowChain<CommitteeBlock> {
    private final List<BlockRequest<CommitteeBlock>> blockInventRequests;
    private final List<BlockRequest<CommitteeBlock>> blockForgeRequests;

    public CommitteeBlockFlowChain() {
        super(List.of(
                        new ForgeStakingMapBuilder(),
                        new ForgeInitializerBuilder(),
                        new ForgeDifficultyBuilder(),
                        new ForgeStructureMapBuilder(),
                        new ForgeValidatorsAssignBuilder(),
                        new ForgeSerializerBlock()),
                List.of(
                        new InventSyncBlocksBuilder(),
                        new InventTrxDifferentOriginBuilder(),
                        new InventRecapDifferentOriginBuilder(),
                        new InventDatabaseStorageBuilder()));
        this.blockInventRequests = new ArrayList<>();
        this.blockForgeRequests = new ArrayList<>();
    }


    @Override
    public void makeInventorRequest(BlockRequest<CommitteeBlock> req) {
        this.blockInventRequests.add(req);
        super.makeInventorRequest(req);
    }

    @Override
    public void makeForgeHandlersRequest(BlockRequest<CommitteeBlock> req) {
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
