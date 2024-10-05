package io.Adrestus.core.RewardMechanism;

import java.util.Map;

public class DelegateRewardsCalculator implements RewardHandler {

    @Override
    public boolean canHandleRequest(Request req) {
        return req.getRequestType() == RequestType.DELEGATE_REWARD_CALCULATOR;
    }

    @Override
    public int getPriority() {
        return 5;
    }

    @Override
    public void handle(Request req) {
        req.markHandled();
        for (Map.Entry<String, RewardObject> validator : CachedRewardMapData.getInstance().getEffective_stakes_map().entrySet()) {
            for (Map.Entry<String, DelegateObject> delegator : validator.getValue().getDelegate_stake().entrySet()) {
                delegator.getValue().setReward(delegator.getValue().getWeights().multiply(validator.getValue().getUnreal_reward()));
            }
        }
    }

    @Override
    public String name() {
        return "DELEGATE_REWARD_CALCULATOR";
    }
}
