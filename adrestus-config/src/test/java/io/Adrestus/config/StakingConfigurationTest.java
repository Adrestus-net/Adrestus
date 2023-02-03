package io.Adrestus.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StakingConfigurationTest {

    @Test
    public void test() {
        assertEquals(1000, StakingConfiguration.MINIMUM_STAKING);
    }
}
