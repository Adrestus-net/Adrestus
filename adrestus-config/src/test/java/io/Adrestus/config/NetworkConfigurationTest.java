package io.Adrestus.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NetworkConfigurationTest {

    @Test
    public void test() {
        assertEquals(8082, NetworkConfiguration.RPC_PORT);
        assertEquals(8083, NetworkConfiguration.ZONE0_RPC_PORT);
        assertEquals(8084, NetworkConfiguration.ZONE1_RPC_PORT);
        assertEquals(8085, NetworkConfiguration.ZONE2_RPC_PORT);
        assertEquals(8086, NetworkConfiguration.ZONE3_RPC_PORT);


        assertEquals(7083, NetworkConfiguration.PATRICIATREE_ZONE0_RPC_PORT);
        assertEquals(7084, NetworkConfiguration.PATRICIATREE_ZONE1_RPC_PORT);
        assertEquals(7085, NetworkConfiguration.PATRICIATREE_ZONE2_RPC_PORT);
        assertEquals(7086, NetworkConfiguration.PATRICIATREE_ZONE3_RPC_PORT);
    }
}
