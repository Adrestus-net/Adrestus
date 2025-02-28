package io.Adrestus.core.BlockPipeline.FlowChain.Transaction;

import io.Adrestus.TreeFactory;
import io.Adrestus.config.RewardConfiguration;
import io.Adrestus.core.BlockPipeline.BlockRequest;
import io.Adrestus.core.BlockPipeline.BlockRequestHandler;
import io.Adrestus.core.BlockPipeline.BlockRequestType;
import io.Adrestus.core.Resourses.CachedStartHeightRewards;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.RewardMechanism.CachedRewardMapData;
import io.Adrestus.core.RewardMechanism.Request;
import io.Adrestus.core.RewardMechanism.RequestType;
import io.Adrestus.core.RewardMechanism.RewardChainBuilder;
import io.Adrestus.core.TransactionBlock;
import lombok.SneakyThrows;

public class InventRewardsBuilder implements BlockRequestHandler<TransactionBlock> {

    @Override
    public boolean canHandleRequest(BlockRequest<TransactionBlock> req) {
        return req.getRequestType() == BlockRequestType.INVENT_REWARDS_BUILDER;
    }

    @Override
    public int getPriority() {
        return 4;
    }

    @Override
    @SneakyThrows
    public void process(BlockRequest<TransactionBlock> blockRequest) {
        if (CachedZoneIndex.getInstance().getZoneIndex() == 0 && blockRequest.getBlock().getHeight() % RewardConfiguration.BLOCK_REWARD_HEIGHT == 0) {
            RewardChainBuilder rewardChainBuilder = new RewardChainBuilder();
            rewardChainBuilder.makeRequest(new Request(RequestType.REWARD_STORAGE_CALCULATOR, "REWARD_STORAGE_CALCULATOR", TreeFactory.getMemoryTree(0)));
            CachedRewardMapData.getInstance().clearInstance();
            CachedStartHeightRewards.getInstance().setRewardsCommitteeEnabled(false);
            CachedStartHeightRewards.getInstance().setHeight(blockRequest.getBlock().getHeight());
        }

    }

    @Override
    public void clear(BlockRequest<TransactionBlock> blockRequest) {
        blockRequest.clear();
    }

    @Override
    public String name() {
        return "InventRewardsBuilder";
    }
}
