package io.Adrestus.core.RewardMechanism;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class RewardChainBuilder {
    private List<RewardHandler> handlers;

    public RewardChainBuilder() {
        buildChain();
    }

    private void buildChain() {
        handlers = Arrays.asList(new EffectiveStakeCalculator(),new EffectiveStakeRatioCalculator(),new DelegateWeightsCalculator(),new ValidatorRewardCalculator(),new DelegateRewardsCalculator(),new RewardStorageCalculator());
    }

    public void makeRequest(Request req) {
        handlers
                .stream()
                .sorted(Comparator.comparing(RewardHandler::getPriority))
                .filter(handler -> handler.canHandleRequest(req))
                .findFirst()
                .ifPresent(handler -> handler.handle(req));
    }
}
