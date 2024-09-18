package io.Adrestus.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StakingConfigurationTest {

    @Test
    public void test() {
        assertEquals(100, StakingConfiguration.MINIMUM_STAKING);
        assertEquals(0.85, StakingConfiguration.C);
    }
}
