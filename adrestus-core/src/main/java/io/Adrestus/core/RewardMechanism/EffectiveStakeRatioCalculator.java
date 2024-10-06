package io.Adrestus.core.RewardMechanism;

import io.Adrestus.config.RewardConfiguration;

import java.math.BigDecimal;
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
        BigDecimal effective_sum = CachedRewardMapData.getInstance().getEffective_stakes_map().values().stream().map(RewardObject::getEffective_stake).reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
        for (Map.Entry<String, RewardObject> entry : CachedRewardMapData.getInstance().getEffective_stakes_map().entrySet()) {
            entry.getValue().setEffective_stake_ratio(entry.getValue().getEffective_stake().divide(effective_sum, RewardConfiguration.DECIMAL_PRECISION, RewardConfiguration.ROUNDING));
        }
    }

    @Override
    public String name() {
        return "EffectiveStakeCommand";
    }
}
