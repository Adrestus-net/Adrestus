package io.Adrestus.util;

import io.Adrestus.config.ConsensusConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransitionTest {

    @Test
    public void test() {
        ConsensusConfiguration.EPOCH_TRANSITION = 7;


        assertEquals(4, EpochTransitionFinder.countloops(17));

        assertEquals(4, EpochTransitionFinder.countloops(14, 3));
    }
}
