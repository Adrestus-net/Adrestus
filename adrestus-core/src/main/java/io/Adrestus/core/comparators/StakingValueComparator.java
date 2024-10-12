package io.Adrestus.core.comparators;

import io.Adrestus.crypto.elliptic.mapper.StakingData;

import java.io.Serializable;
import java.util.Comparator;

public class StakingValueComparator implements Comparator<StakingData>, Serializable {
    @Override
    public int compare(StakingData a, StakingData b) {
        if (a.getStake().compareTo(b.getStake()) >= 0) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}
