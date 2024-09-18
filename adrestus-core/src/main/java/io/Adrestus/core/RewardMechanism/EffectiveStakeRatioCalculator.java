package io.Adrestus.core.RewardMechanism;

import java.util.Map;

public class EffectiveStakeRatioCalculator implements RewardHandler {
    @Override
    public boolean canHandleRequest(Request req) {
        return req.getRequestType() == RequestType.EFFECTIVE_STAKE_RATIO;
    }

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public void handle(Request req) {
        req.markHandled();
        double effective_sum=CachedRewardMapData.getInstance().getEffective_stakes_map().values().stream().mapToDouble(RewardObject::getEffective_stake).sum();
        for(Map.Entry<String, RewardObject> entry : CachedRewardMapData.getInstance().getEffective_stakes_map().entrySet()) {
            entry.getValue().setEffective_stake_ratio(entry.getValue().getEffective_stake()/effective_sum);
        }
    }

    @Override
    public String name() {
        return "EffectiveStakeCommand";
    }
}
