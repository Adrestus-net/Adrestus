package io.Adrestus.config;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KademliaConfigurationTest {

    @Test
    public void test() {
        assertEquals(8081, KademliaConfiguration.PORT);
        assertEquals("04082909431bc6c2adc38d791d132828e1ee3a034bc60b2d37944005b1175b1e9b4c7783eeb1b53b330bce60e18ae85a33145db8cae9d7df6297bec8b0ac5f355f87bed6e55b40962a9ecfa69464aeb36c8265ebb3385214cb5a5961598cb7b365", KademliaConfiguration.BLSPublicKeyHex);
        assertEquals("127.0.0.1", KademliaConfiguration.LOCAL_NODE_IP);
        assertEquals(8080, KademliaConfiguration.BootstrapNodePORT);
        assertEquals(new BigInteger("15949"), KademliaConfiguration.BootstrapNodeID);
        assertEquals(4 * 1000, KademliaConfiguration.STORE_DELAY);
        assertEquals(5, KademliaConfiguration.KADEMLIA_GET_TIMEOUT);

        assertEquals(4, KademliaConfiguration.IDENTIFIER_SIZE);
        assertEquals(10, KademliaConfiguration.BUCKET_SIZE);
        assertEquals(1, KademliaConfiguration.MAXIMUM_STORE_AND_LOOKUP_TIMEOUT_VALUE);
        assertEquals(TimeUnit.SECONDS, KademliaConfiguration.MAXIMUM_STORE_AND_LOOKUP_TIMEOUT_TIME_UNIT);
        assertEquals(true, KademliaConfiguration.ENABLED_FIRST_STORE_REQUEST_FORCE_PASS);
        assertEquals(TimeUnit.SECONDS, KademliaConfiguration.PING_SCHEDULE_TIME_UNIT);

        assertEquals(2, KademliaConfiguration.PING_SCHEDULE_TIME_VALUE);
        assertEquals(10, KademliaConfiguration.ALPHA);
        assertEquals(20, KademliaConfiguration.FIND_NODE_SIZE);
        assertEquals(20, KademliaConfiguration.MAXIMUM_LAST_SEEN_AGE_TO_CONSIDER_ALIVE);
        assertEquals(20, KademliaConfiguration.DHT_EXECUTOR_POOL_SIZE);
        assertEquals(5, KademliaConfiguration.DHT_SCHEDULED_EXECUTOR_POOL_SIZE);
        assertEquals(1, KademliaConfiguration.SCHEDULED_EXECUTOR_POOL_SIZE);
    }
}
