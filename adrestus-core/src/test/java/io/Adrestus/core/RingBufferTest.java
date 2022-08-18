package io.Adrestus.core;

import io.Adrestus.core.RingBuffer.factory.AbstractBlockEventFactory;
import io.Adrestus.core.RingBuffer.publisher.BlockEventPublisher;
import io.Adrestus.core.RingBuffer.publisher.TransactionEventPublisher;
import org.junit.jupiter.api.Test;

public class RingBufferTest {

    //@Test
    public void TransactionTest() throws InterruptedException {
        Transaction tr = new RewardsTransaction("Delegator Address");
        tr.setAmount(100);

        TransactionEventPublisher publisher = new TransactionEventPublisher(1024);

        publisher.withNonceEventHandler().withAmountEventHandler().mergeEvents();
        publisher.start();
        publisher.publish(tr);
        Thread.sleep(20000);
    }


    @Test
    public void CommitBlockTest() throws InterruptedException {
        BlockEventPublisher  publisher=new BlockEventPublisher(1024);
        CommitteeBlock block=new CommitteeBlock();
        block.setHash("hash");


        publisher.withHashHandler().mergeEvents();
        publisher.start();
        publisher.publish(block);


        Thread.sleep(20000);
    }

    //@Test
    public void TransactionBlockTest() throws InterruptedException {
        BlockEventPublisher  publisher=new BlockEventPublisher(1024);
        TransactionBlock block=new TransactionBlock();
        block.setHash("hash");

        publisher.withHashHandler().mergeEvents();
        publisher.start();
        publisher.publish(block);


        Thread.sleep(20000);
    }
}
