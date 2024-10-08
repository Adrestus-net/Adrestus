package io.Adrestus.core.RewardMechanism;

import io.Adrestus.config.RewardConfiguration;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Map;

public class RewardPrecisionCalculator implements RewardHandler {


    public RewardPrecisionCalculator() {
    }

    @Override
    public boolean canHandleRequest(Request req) {
        return req.getRequestType() == RequestType.REWARD_PRECISION_CALCULATOR;
    }

    @Override
    public int getPriority() {
        return 6;
    }

    @Override
    public void handle(Request req) {
        req.markHandled();
        CachedRewardMapData.getInstance().getEffective_stakes_map().values().forEach(RewardPrecisionCalculator::applyPrecision);
    }

    private static RewardObject applyPrecision(RewardObject rewardObject) {
        rewardObject.setEffective_stake(rewardObject.getEffective_stake().setScale(RewardConfiguration.DECIMAL_PRECISION, RewardConfiguration.ROUNDING));
        rewardObject.setEffective_stake_ratio(rewardObject.getEffective_stake_ratio().setScale(RewardConfiguration.DECIMAL_PRECISION,RewardConfiguration.ROUNDING));
        rewardObject.setUnreal_reward(rewardObject.getUnreal_reward().setScale(RewardConfiguration.DECIMAL_PRECISION, RewardConfiguration.ROUNDING));
        rewardObject.setReal_reward(rewardObject.getReal_reward().setScale(RewardConfiguration.DECIMAL_PRECISION, RewardConfiguration.ROUNDING));
        rewardObject.getDelegate_stake().values().forEach(RewardPrecisionCalculator::applyPrecision);
        return rewardObject;
    }

    private static DelegateObject applyPrecision(DelegateObject delegateObject) {
        delegateObject.setReward(delegateObject.getReward().setScale(RewardConfiguration.DECIMAL_PRECISION, RewardConfiguration.ROUNDING));
        delegateObject.setWeights(delegateObject.getWeights().setScale(RewardConfiguration.DECIMAL_PRECISION, RewardConfiguration.ROUNDING));
        return delegateObject;
    }

    @Override
    public String name() {
        return "REWARD_PRECISION_CALCULATOR";
    }
}
