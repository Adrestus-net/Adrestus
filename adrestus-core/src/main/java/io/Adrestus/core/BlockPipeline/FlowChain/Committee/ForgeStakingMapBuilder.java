package io.Adrestus.core.BlockPipeline.FlowChain.Committee;

import io.Adrestus.TreeFactory;
import io.Adrestus.core.BlockPipeline.BlockRequest;
import io.Adrestus.core.BlockPipeline.BlockRequestHandler;
import io.Adrestus.core.BlockPipeline.BlockRequestType;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.Resourses.CachedKademliaNodes;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.crypto.elliptic.mapper.StakingData;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

public class ForgeStakingMapBuilder implements BlockRequestHandler<CommitteeBlock> {


    @Override
    public boolean canHandleRequest(BlockRequest<CommitteeBlock> req) {
        return req.getRequestType() == BlockRequestType.FORGE_STAKING_MAP_BUILDER;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    @SneakyThrows
    public void process(BlockRequest<CommitteeBlock> blockRequest) {
        if (CachedKademliaNodes.getInstance().getDhtBootstrapNode() != null) {
            List<KademliaData> kademliaData = CachedKademliaNodes
                    .getInstance()
                    .getDhtBootstrapNode()
                    .getActiveNodes()
                    .stream()
                    .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparing(e -> e.getAddressData().getAddress()))), ArrayList::new));

            for (int i = 0; i < kademliaData.size(); i++) {
                blockRequest.getBlock()
                        .getStakingMap()
                        .put(new StakingData(i, TreeFactory.getMemoryTree(0).getByaddress(kademliaData.get(i).getAddressData().getAddress()).get().getStaking_amount()), kademliaData.get(i));
            }
        } else if (CachedKademliaNodes.getInstance().getDhtRegularNode() != null) {
            List<KademliaData> kademliaData = CachedKademliaNodes
                    .getInstance()
                    .getDhtRegularNode()
                    .getActiveNodes()
                    .stream()
                    .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparing(e -> e.getAddressData().getAddress()))), ArrayList::new));

            for (int i = 0; i < kademliaData.size(); i++) {
                blockRequest.getBlock()
                        .getStakingMap()
                        .put(new StakingData(i, TreeFactory.getMemoryTree(0).getByaddress(kademliaData.get(i).getAddressData().getAddress()).get().getStaking_amount()), kademliaData.get(i));
            }
        } else {
            blockRequest.getBlock().setStakingMap(CachedLatestBlocks.getInstance().getCommitteeBlock().getStakingMap());
        }
    }

    @Override
    public String name() {
        return "ForgeStakingMapBuilder";
    }

    @Override
    public void clear(BlockRequest<CommitteeBlock> blockRequest) {
        blockRequest.clear();
    }
}
