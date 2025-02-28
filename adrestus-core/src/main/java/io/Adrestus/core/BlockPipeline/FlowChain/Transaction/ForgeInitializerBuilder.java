package io.Adrestus.core.BlockPipeline.FlowChain.Transaction;

import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.core.BlockPipeline.BlockRequest;
import io.Adrestus.core.BlockPipeline.BlockRequestHandler;
import io.Adrestus.core.BlockPipeline.BlockRequestType;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.Resourses.MemoryTransactionPool;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.util.GetTime;
import lombok.SneakyThrows;

import java.util.ArrayList;

public class ForgeInitializerBuilder implements BlockRequestHandler<TransactionBlock> {
    @Override
    public boolean canHandleRequest(BlockRequest<TransactionBlock> req) {
        return req.getRequestType() == BlockRequestType.FORGE_INITIALIZER_BUILDER;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    @SneakyThrows
    public void process(BlockRequest<TransactionBlock> blockRequest) {
        blockRequest.getBlock().getHeaderData().setPreviousHash(CachedLatestBlocks.getInstance().getTransactionBlock().getHash());
        blockRequest.getBlock().getHeaderData().setVersion(AdrestusConfiguration.version);
        blockRequest.getBlock().getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        blockRequest.getBlock().setStatustype(StatusType.PENDING);
        blockRequest.getBlock().setHeight(CachedLatestBlocks.getInstance().getTransactionBlock().getHeight() + 1);
        blockRequest.getBlock().setGeneration(CachedLatestBlocks.getInstance().getCommitteeBlock().getGeneration());
        blockRequest.getBlock().setViewID(CachedLatestBlocks.getInstance().getTransactionBlock().getViewID() + 1);
        blockRequest.getBlock().setZone(CachedZoneIndex.getInstance().getZoneIndex());
        blockRequest.getBlock().setBlockProposer(CachedBLSKeyPair.getInstance().getPublicKey().toRaw());
        blockRequest.getBlock().setLeaderPublicKey(CachedBLSKeyPair.getInstance().getPublicKey());
        blockRequest.getBlock().setTransactionList(new ArrayList<>(MemoryTransactionPool.getInstance().getListByZone(CachedZoneIndex.getInstance().getZoneIndex())));
    }

    @Override
    public void clear(BlockRequest<TransactionBlock> blockRequest) {
        blockRequest.clear();
    }

    @Override
    public String name() {
        return "ForgeInitializerBuilder";
    }
}
