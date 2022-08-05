package io.Adrestus.core;

import io.Adrestus.core.RingBuffer.publisher.TransactionEventPublisher;
import org.junit.jupiter.api.Test;

public class RingBufferTest {

    @Test
    public void TransactionTest() throws InterruptedException {
        TransactionEventPublisher publisher = new TransactionEventPublisher(1024);
        Transaction tr = new RewardsTransaction("Delegator Address");
        tr.setAmount(100);
        publisher.start();
        publisher.publish(tr);
        Thread.sleep(20000);
    }
}
