package io.Adrestus.core.RewardMechanism;

import io.Adrestus.TreeFactory;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.crypto.elliptic.mapper.StakingData;
import io.Adrestus.p2p.kademlia.repository.KademliaData;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

public class DelegateWeightsCalculator implements RewardHandler {
    @Override
    public boolean canHandleRequest(Request req) {
        return req.getRequestType() == RequestType.DELEGATE_WEIGHTS_CALCULATOR;
    }

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    public void handle(Request req) {
        req.markHandled();
        TreeMap<StakingData, KademliaData> map = CachedLatestBlocks.getInstance().getCommitteeBlock().getStakingMap();
        map.values().stream().forEach(data -> {
            String validator_address = data.getAddressData().getAddress();
            double validator_stake = TreeFactory.getMemoryTree(0).getByaddress(validator_address).get().getStaking_amount();
            Map<String, Double> delegation = TreeFactory.getMemoryTree(0).getByaddress(data.getAddressData().getAddress()).get().getDelegation();
            for (Map.Entry<String, Double> entry : delegation.entrySet()) {
                CachedRewardMapData.getInstance().getEffective_stakes_map().get(validator_address).getDelegate_stake().put(entry.getKey(), new DelegateObject(new BigDecimal(entry.getValue() / validator_stake), BigDecimal.ZERO));
            }
        });
    }

    @Override
    public String name() {
        return "DELEGATE_WEIGHTS_CALCULATOR";
    }
}
