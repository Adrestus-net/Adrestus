package io.Adrestus.core.RewardMechanism;

import io.Adrestus.config.RewardConfiguration;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Map;

public class RewardPrecisionCalculator implements RewardHandler {
    private static DecimalFormat reward_format;

    public RewardPrecisionCalculator() {
        reward_format = new DecimalFormat();
        reward_format.setMaximumFractionDigits(RewardConfiguration.DECIMAL_PRECISION);
        reward_format.setRoundingMode(RewardConfiguration.ROUNDING);
    }

    @Override
    public boolean canHandleRequest(Request req) {
        return req.getRequestType() == RequestType.REWARD_PRECISION_CALCULATOR;
    }

    @Override
    public int getPriority() {
        return 5;
    }

    @Override
    public void handle(Request req) {
        req.markHandled();
        CachedRewardMapData.getInstance().getEffective_stakes_map().values().forEach(RewardPrecisionCalculator::applyPrecision);
    }

    private static RewardObject applyPrecision(RewardObject rewardObject) {
        rewardObject.setEffective_stake(Double.parseDouble(reward_format.format(rewardObject.getEffective_stake())));
        rewardObject.setEffective_stake_ratio(Double.parseDouble(reward_format.format(rewardObject.getEffective_stake_ratio())));
        rewardObject.setUnreal_reward(Double.parseDouble(reward_format.format(rewardObject.getUnreal_reward())));
        rewardObject.setReal_reward(Double.parseDouble(reward_format.format(rewardObject.getReal_reward())));
        rewardObject.getDelegate_stake().values().forEach(RewardPrecisionCalculator::applyPrecision);
        return rewardObject;
    }

    private static DelegateObject applyPrecision(DelegateObject delegateObject) {
        delegateObject.setReward(Double.parseDouble(reward_format.format(delegateObject.getReward())));
        delegateObject.setWeights(Double.parseDouble(reward_format.format(delegateObject.getWeights())));
        return delegateObject;
    }

    @Override
    public String name() {
        return "REWARD_PRECISION_CALCULATOR";
    }
}
