package io.Adrestus.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransactionConfigOptionsTest {

    @Test
    public void test() {
        assertEquals(5556, TransactionConfigOptions.TRANSACTION_PORT);
    }
}
