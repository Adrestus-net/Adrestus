package io.Adrestus.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AdrestusConfigurationTest {
    @Test
    public void test() {
        assertEquals(0x00, AdrestusConfiguration.version);
        assertEquals(64 * 1024, AdrestusConfiguration.BUFFER_SIZE);
        assertEquals(2048, AdrestusConfiguration.PIERRZAK_BIT);
        assertEquals(Runtime.getRuntime().availableProcessors(), AdrestusConfiguration.CORES);
        assertEquals(10000000, AdrestusConfiguration.MAXIMU_BLOCK_SIZE);
        assertEquals(100, AdrestusConfiguration.INIT_VDF_DIFFICULTY);
        assertEquals(3, AdrestusConfiguration.MAX_ZONES);
        assertEquals("SHA1PRNG", AdrestusConfiguration.ALGORITHM);
        assertEquals("SUN", AdrestusConfiguration.PROVIDER);

        assertEquals("EC", AdrestusConfiguration.SIGN_ALGORITHM);
        assertEquals("BC", AdrestusConfiguration.SIGN_PROVIDER);
        assertEquals("secp256k1", AdrestusConfiguration.SIGN_CURVE);
    }
}
