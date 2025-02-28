package io.Adrestus.core.BlockPipeline.FlowChain.Committee;

import com.google.common.primitives.Ints;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.core.BlockPipeline.BlockRequest;
import io.Adrestus.core.BlockPipeline.BlockRequestHandler;
import io.Adrestus.core.BlockPipeline.BlockRequestType;
import io.Adrestus.core.CommitteeBlock;
import lombok.SneakyThrows;
import org.spongycastle.util.encoders.Hex;

import java.security.SecureRandom;
import java.util.ArrayList;

public class ForgeValidatorsAssignBuilder implements BlockRequestHandler<CommitteeBlock> {

    @Override
    public boolean canHandleRequest(BlockRequest<CommitteeBlock> req) {
        return req.getRequestType() == BlockRequestType.FORGE_VALIDATORS_ASSIGN_BUILDER;
    }

    @Override
    public int getPriority() {
        return 4;
    }

    @Override
    @SneakyThrows
    public void process(BlockRequest<CommitteeBlock> blockRequest) {
        //###################Random assign validators##########################
        SecureRandom leader_random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        leader_random.setSeed(Hex.decode(blockRequest.getBlock().getVDF()));

        int iteration = 0;
        ArrayList<Integer> replica = new ArrayList<>();
        while (iteration < blockRequest.getBlock().getStakingMap().size()) {
            int nextInt = leader_random.nextInt(blockRequest.getBlock().getStakingMap().size());
            if (!replica.contains(nextInt)) {
                replica.add(nextInt);
                iteration++;
            }
        }
        //###################Random assign validators##########################
        blockRequest.getBlock().setCommitteeProposer(Ints.toArray(replica));
    }

    @Override
    public String name() {
        return "InventCacheBuilder";
    }

    @Override
    public void clear(BlockRequest<CommitteeBlock> blockRequest) {
        blockRequest.clear();
    }
}
