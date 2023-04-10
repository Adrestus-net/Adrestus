package io.Adrestus.util;

import io.Adrestus.config.ConsensusConfiguration;

public class EpochTransitionFinder {


    public static int countloops(int height) {
        int mod = height % ConsensusConfiguration.EPOCH_TRANSITION;
        if (mod == 0)
            return 0;

        int div = height / ConsensusConfiguration.EPOCH_TRANSITION;
        int multi = (div + 1) * ConsensusConfiguration.EPOCH_TRANSITION;
        return multi - height;
    }

    public static int countloops(int start_height, int end_height) {
        int mod = (start_height + end_height) % ConsensusConfiguration.EPOCH_TRANSITION;
        if (mod == 0)
            return 0;

        int div = (start_height + end_height) / ConsensusConfiguration.EPOCH_TRANSITION;
        int multi = (div + 1) * ConsensusConfiguration.EPOCH_TRANSITION;
        return multi - (start_height + end_height);
    }
}
