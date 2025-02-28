package io.Adrestus.core.BlockPipeline.FlowChain.Committee;

import io.Adrestus.core.BlockPipeline.BlockRequest;
import io.Adrestus.core.BlockPipeline.BlockRequestHandler;
import io.Adrestus.core.BlockPipeline.BlockRequestType;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedSecurityHeaders;
import io.Adrestus.util.GetTime;
import lombok.SneakyThrows;
import org.spongycastle.util.encoders.Hex;

public class ForgeInitializerBuilder implements BlockRequestHandler<CommitteeBlock> {


    @Override
    public boolean canHandleRequest(BlockRequest<CommitteeBlock> req) {
        return req.getRequestType() == BlockRequestType.FORGE_INITIALIZER_BUILDER;
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    @SneakyThrows
    public void process(BlockRequest<CommitteeBlock> blockRequest) {
        blockRequest.getBlock().setCommitteeProposer(new int[blockRequest.getBlock().getStakingMap().size()]);
        blockRequest.getBlock().setGeneration(CachedLatestBlocks.getInstance().getCommitteeBlock().getGeneration() + 1);
        blockRequest.getBlock().getHeaderData().setPreviousHash(CachedLatestBlocks.getInstance().getCommitteeBlock().getHash());
        blockRequest.getBlock().setHeight(CachedLatestBlocks.getInstance().getCommitteeBlock().getHeight() + 1);
        blockRequest.getBlock().setViewID(CachedLatestBlocks.getInstance().getCommitteeBlock().getViewID() + 1);
        blockRequest.getBlock().getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        blockRequest.getBlock().setVRF(Hex.toHexString(CachedSecurityHeaders.getInstance().getSecurityHeader().getPRnd()));
    }

    @Override
    public String name() {
        return "ForgeInitializerBuilder";
    }

    @Override
    public void clear(BlockRequest<CommitteeBlock> blockRequest) {
        blockRequest.clear();
    }
}
