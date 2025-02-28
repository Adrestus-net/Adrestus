package io.Adrestus.core;

import io.Adrestus.core.BlockPipeline.*;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedLeaderIndex;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseInstance;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RegularBlock implements BlockForge, BlockInvent {
    private static Logger LOG = LoggerFactory.getLogger(RegularBlock.class);
    private static volatile RegularBlock instance;
    private final BlockFlowChain<TransactionBlock> transactionBlockBlockFlowChain;
    private final BlockFlowChain<CommitteeBlock> committeeBlockBlockFlowChain;

    private RegularBlock() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        this.transactionBlockBlockFlowChain = new TransactionBlockFlowChain();
        this.committeeBlockBlockFlowChain = new CommitteeBlockFlowChain();
    }

    public static RegularBlock getInstance() {
        var result = instance;
        if (result == null) {
            synchronized (RegularBlock.class) {
                result = instance;
                if (result == null) {
                    result = new RegularBlock();
                    instance = result;
                }
            }
        }
        return result;
    }

    @Override
    public void forgeTransactionBlock(TransactionBlock transactionBlock) throws Exception {
        try {
            this.transactionBlockBlockFlowChain.makeForgeHandlersRequest(new BlockRequest<>(BlockRequestType.FORGE_INITIALIZER_BUILDER, transactionBlock));
            this.transactionBlockBlockFlowChain.makeForgeHandlersRequest(new BlockRequest<>(BlockRequestType.FORGE_DIFFERENT_ORIGIN_BUILDER, transactionBlock));
            this.transactionBlockBlockFlowChain.makeForgeHandlersRequest(new BlockRequest<>(BlockRequestType.FORGE_FEE_REWARD_BUILDER, transactionBlock));
            this.transactionBlockBlockFlowChain.makeForgeHandlersRequest(new BlockRequest<>(BlockRequestType.FORGE_OUTBOUND_BUILDER, transactionBlock));
            this.transactionBlockBlockFlowChain.makeForgeHandlersRequest(new BlockRequest<>(BlockRequestType.FORGE_INBOUND_BUILDER, transactionBlock));
            this.transactionBlockBlockFlowChain.makeForgeHandlersRequest(new BlockRequest<>(BlockRequestType.FORGE_PATRICIA_TREE_BUILDER, transactionBlock));
            this.transactionBlockBlockFlowChain.makeForgeHandlersRequest(new BlockRequest<>(BlockRequestType.FORGE_SERIALIZER_BLOCK_BUILDER, transactionBlock));
        } catch (Exception e) {
            LOG.error("Error while forging transaction block", e);
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    @Override
    public void forgeCommitteBlock(CommitteeBlock committeeBlock) {
        try {
            this.committeeBlockBlockFlowChain.makeForgeHandlersRequest(new BlockRequest<>(BlockRequestType.FORGE_STAKING_MAP_BUILDER, committeeBlock));
            this.committeeBlockBlockFlowChain.makeForgeHandlersRequest(new BlockRequest<>(BlockRequestType.FORGE_INITIALIZER_BUILDER, committeeBlock));
            this.committeeBlockBlockFlowChain.makeForgeHandlersRequest(new BlockRequest<>(BlockRequestType.FORGE_DIFFICULTY_BUILDER, committeeBlock));
            this.committeeBlockBlockFlowChain.makeForgeHandlersRequest(new BlockRequest<>(BlockRequestType.FORGE_STRUCTURE_MAP_BUILDER, committeeBlock));
            this.committeeBlockBlockFlowChain.makeForgeHandlersRequest(new BlockRequest<>(BlockRequestType.FORGE_VALIDATORS_ASSIGN_BUILDER, committeeBlock));
            this.committeeBlockBlockFlowChain.makeForgeHandlersRequest(new BlockRequest<>(BlockRequestType.FORGE_SERIALIZER_BLOCK, committeeBlock));
        } catch (Exception e) {
            LOG.error("Error while forging committee block", e);
            throw new RuntimeException(e);
        }
    }


    @SneakyThrows
    @Override
    public void InventTransactionBlock(TransactionBlock transactionBlock) {
        try {
            this.transactionBlockBlockFlowChain.makeInventorRequest(new BlockRequest<>(BlockRequestType.INVENT_INITIALIZER_BUILDER, transactionBlock));
            this.transactionBlockBlockFlowChain.makeInventorRequest(new BlockRequest<>(BlockRequestType.INVENT_TREE_POOL_BUILDER, transactionBlock));
            this.transactionBlockBlockFlowChain.makeInventorRequest(new BlockRequest<>(BlockRequestType.INVENT_INBOUND_BUILDER, transactionBlock));
            this.transactionBlockBlockFlowChain.makeInventorRequest(new BlockRequest<>(BlockRequestType.INVENT_OUTBOUND_BUILDER, transactionBlock));
            this.transactionBlockBlockFlowChain.makeInventorRequest(new BlockRequest<>(BlockRequestType.INVENT_REWARDS_BUILDER, transactionBlock));
            this.transactionBlockBlockFlowChain.makeInventorRequest(new BlockRequest<>(BlockRequestType.INVENT_DATABASE_STORAGE_BUILDER, transactionBlock));
            this.transactionBlockBlockFlowChain.makeInventorRequest(new BlockRequest<>(BlockRequestType.INVENT_CACHE_BUILDER, transactionBlock));
            this.transactionBlockBlockFlowChain.clear();
        } catch (Exception e) {
            LOG.error("Error while inventing transaction block", e);
            throw new RuntimeException(e);
        }
    }


    //invent
    @SneakyThrows
    @Override
    public void InventCommitteBlock(CommitteeBlock committeeBlock) {
        CommitteeBlock prevblock = CachedLatestBlocks.getInstance().getCommitteeBlock().clone();
        int prevZone = Integer.valueOf(CachedZoneIndex.getInstance().getZoneIndex());
        IDatabase<String, CommitteeBlock> database = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);


        CachedLatestBlocks.getInstance().setCommitteeBlock(committeeBlock);
        CachedLeaderIndex.getInstance().setCommitteePositionLeader(0);
        CachedLeaderIndex.getInstance().setTransactionPositionLeader(0);
        CachedZoneIndex.getInstance().setZoneIndexInternalIP();

        if (prevZone == CachedZoneIndex.getInstance().getZoneIndex()) {
            CachedLeaderIndex.getInstance().setTransactionPositionLeader(0);
            committeeBlock.setStatustype(StatusType.SUCCES);
            database.save(String.valueOf(committeeBlock.getGeneration()), committeeBlock);
            return;
        }
        try {
            this.committeeBlockBlockFlowChain.makeInventorRequest(new BlockRequest<>(BlockRequestType.INVENT_SYNC_BLOCKS_BUILDER, prevblock));
            this.committeeBlockBlockFlowChain.makeInventorRequest(new BlockRequest<>(BlockRequestType.INVENT_TRX_DIFFERENT_ORIGIN_BUILDER, committeeBlock, prevZone));
            this.committeeBlockBlockFlowChain.makeInventorRequest(new BlockRequest<>(BlockRequestType.INVENT_RECAP_DIFFERENT_ORIGIN_BUILDER, committeeBlock, prevZone));
            this.committeeBlockBlockFlowChain.makeInventorRequest(new BlockRequest<>(BlockRequestType.INVENT_DATABASE_STORAGE_BUILDER, committeeBlock));
            this.transactionBlockBlockFlowChain.clear();
        } catch (Exception e) {
            LOG.error("Error while inventing transaction block", e);
            throw new RuntimeException(e);
        }
    }
}
