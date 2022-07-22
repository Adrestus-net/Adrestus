package io.Adrestus.core;

import io.Adrestus.core.RingBuffer.publisher.TransactionEventPublisher;
import org.junit.jupiter.api.Test;

public class RingBufferTest {

    @Test
    public void TransactionTest() {
        TransactionEventPublisher publisher = new TransactionEventPublisher(10, 1024);
        Transaction tr = new RewardsTransaction("Delegator Address");
        tr.setAmount(100);
        publisher.start();
        publisher.publish(tr);
    }
}
