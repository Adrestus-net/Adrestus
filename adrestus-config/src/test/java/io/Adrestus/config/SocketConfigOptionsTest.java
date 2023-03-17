package io.Adrestus.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SocketConfigOptionsTest {

    @Test
    public void test() {
        assertEquals(4556, SocketConfigOptions.TRANSACTION_PORT);
        assertEquals(4557, SocketConfigOptions.RECEIPT_PORT);
    }
}
