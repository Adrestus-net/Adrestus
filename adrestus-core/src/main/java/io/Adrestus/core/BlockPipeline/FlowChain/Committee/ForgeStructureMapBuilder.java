package io.Adrestus.core.BlockPipeline.FlowChain.Committee;

import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.core.BlockPipeline.BlockRequest;
import io.Adrestus.core.BlockPipeline.BlockRequestHandler;
import io.Adrestus.core.BlockPipeline.BlockRequestType;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.crypto.elliptic.mapper.StakingData;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.util.CustomRandom;
import io.Adrestus.util.MathOperationUtil;
import lombok.SneakyThrows;
import org.spongycastle.util.encoders.Hex;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ForgeStructureMapBuilder implements BlockRequestHandler<CommitteeBlock> {

    @Override
    public boolean canHandleRequest(BlockRequest<CommitteeBlock> req) {
        return req.getRequestType() == BlockRequestType.FORGE_STRUCTURE_MAP_BUILDER;
    }

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    @SneakyThrows
    public void process(BlockRequest<CommitteeBlock> blockRequest) {
        //#####RANDOM ASSIGN TO STRUCTRURE MAP ##############
        SecureRandom zone_random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        zone_random.setSeed(Hex.decode(blockRequest.getBlock().getVDF()));

        ArrayList<Integer> exclude = new ArrayList<Integer>();
        ArrayList<Integer> order = new ArrayList<Integer>();
        for (Map.Entry<StakingData, KademliaData> entry : blockRequest.getBlock().getStakingMap().entrySet()) {
            int nextInt = CustomRandom.generateRandom(zone_random, 0, blockRequest.getBlock().getStakingMap().size() - 1, exclude);
            if (!exclude.contains(nextInt)) {
                exclude.add(nextInt);
            }
            order.add(nextInt);
        }
        int zone_count = 0;
        List<Map.Entry<StakingData, KademliaData>> entryList = blockRequest.getBlock().getStakingMap().entrySet().stream().collect(Collectors.toList());
        int MAX_ZONE_SIZE = blockRequest.getBlock().getStakingMap().size() / 4;

        int j = 0;
        if (MAX_ZONE_SIZE >= 2) {
            int addition = blockRequest.getBlock().getStakingMap().size() - MathOperationUtil.closestNumber(blockRequest.getBlock().getStakingMap().size(), 4);
            while (zone_count < 4) {
                if (zone_count == 0 && addition != 0) {
                    while (j < MAX_ZONE_SIZE + addition) {
                        blockRequest.getBlock()
                                .getStructureMap()
                                .get(zone_count)
                                .put(entryList.get(order.get(j)).getValue().getAddressData().getValidatorBlSPublicKey(), entryList.get(order.get(j)).getValue().getNettyConnectionInfo().getHost());
                        j++;
                    }
                } else {
                    int index_count = 0;
                    while (index_count < MAX_ZONE_SIZE) {
                        blockRequest.getBlock()
                                .getStructureMap()
                                .get(zone_count)
                                .put(entryList.get(order.get(j)).getValue().getAddressData().getValidatorBlSPublicKey(), entryList.get(order.get(j)).getValue().getNettyConnectionInfo().getHost());
                        index_count++;
                        j++;
                    }
                }
                zone_count++;
            }
        } else {
            for (int i = 0; i < order.size(); i++) {
                blockRequest.getBlock()
                        .getStructureMap()
                        .get(0)
                        .put(entryList.get(order.get(i)).getValue().getAddressData().getValidatorBlSPublicKey(), entryList.get(order.get(i)).getValue().getNettyConnectionInfo().getHost());
            }
        }
        //#######RANDOM ASSIGN TO STRUCTRURE MAP ##############
    }

    @Override
    public String name() {
        return "ForgeStructureMapBuilder";
    }

    @Override
    public void clear(BlockRequest<CommitteeBlock> blockRequest) {
        blockRequest.clear();
    }
}
