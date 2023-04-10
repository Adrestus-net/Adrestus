package io.Adrestus.util;

import io.Adrestus.config.ConsensusConfiguration;
import io.Adrestus.util.bytes.Bytes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransitionTest {

    @Test
    public void test(){
        ConsensusConfiguration.EPOCH_TRANSITION=7;


        assertEquals(3, EpochTransitionFinder.countloops(18));

        assertEquals(4, EpochTransitionFinder.countloops(14,3));
    }
}
