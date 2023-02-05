package io.Adrestus.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NetworkConfigurationTest {

    @Test
    public void test() {
        assertEquals(8082, NetworkConfiguration.RPC_PORT);
        assertEquals(8083, NetworkConfiguration.RPC_PORT2);
    }
}
