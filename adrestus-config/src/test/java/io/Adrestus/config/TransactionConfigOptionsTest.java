package io.Adrestus.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransactionConfigOptionsTest {

    @Test
    public void test() {
        assertEquals(4556, TransactionConfigOptions.TRANSACTION_PORT);
        assertEquals(4557, TransactionConfigOptions.RECEIPT_PORT);
    }
}
