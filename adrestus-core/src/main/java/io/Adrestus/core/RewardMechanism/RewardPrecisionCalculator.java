package io.Adrestus.core.RewardMechanism;

import io.Adrestus.config.RewardConfiguration;

import java.math.BigDecimal;
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
        return 6;
    }

    @Override
    public void handle(Request req) {
        req.markHandled();
        Map<String,RewardObject> maps=CachedRewardMapData.getInstance().getEffective_stakes_map();
        CachedRewardMapData.getInstance().getEffective_stakes_map().values().forEach(RewardPrecisionCalculator::applyPrecision);
    }

    private static RewardObject applyPrecision(RewardObject rewardObject) {

        rewardObject.setEffective_stake(rewardObject.getEffective_stake().setScale(RewardConfiguration.DECIMAL_PRECISION, RoundingMode.HALF_UP));
        rewardObject.setEffective_stake_ratio(rewardObject.getEffective_stake_ratio().setScale(RewardConfiguration.DECIMAL_PRECISION, RoundingMode.HALF_UP));
        rewardObject.setUnreal_reward(rewardObject.getUnreal_reward().setScale(RewardConfiguration.DECIMAL_PRECISION, RoundingMode.HALF_UP));
        rewardObject.setReal_reward(rewardObject.getReal_reward().setScale(RewardConfiguration.DECIMAL_PRECISION, RoundingMode.HALF_UP));
        rewardObject.getDelegate_stake().values().forEach(RewardPrecisionCalculator::applyPrecision);
        return rewardObject;
    }

    private static DelegateObject applyPrecision(DelegateObject delegateObject) {
        delegateObject.setReward(delegateObject.getReward().setScale(RewardConfiguration.DECIMAL_PRECISION, RoundingMode.HALF_UP));
        delegateObject.setWeights(delegateObject.getWeights().setScale(RewardConfiguration.DECIMAL_PRECISION, RoundingMode.HALF_UP));
        return delegateObject;
    }

    @Override
    public String name() {
        return "REWARD_PRECISION_CALCULATOR";
    }
}
