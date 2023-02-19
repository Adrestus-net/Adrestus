package io.Adrestus.core;

import io.Adrestus.core.RingBuffer.publisher.BlockEventPublisher;
import io.Adrestus.core.RingBuffer.publisher.TransactionEventPublisher;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RingBufferTest {

    @Test
    public void TransactionTest() throws InterruptedException {
        SerializationUtil<Transaction> serenc = new SerializationUtil<Transaction>(Transaction.class);

        Transaction transaction = new RewardsTransaction("Delegator Address");
        transaction.setStatus(StatusType.PENDING);
        transaction.setTimestamp(GetTime.GetTimeStampInString());
        transaction.setZoneFrom(0);
        transaction.setZoneTo(0);
        transaction.setAmount(100);
        transaction.setAmountWithTransactionFee(transaction.getAmount() * (10.0 / 100.0));
        transaction.setNonce(1);
        byte byf[] = serenc.encode(transaction);
        transaction.setHash(HashUtil.sha256_bytetoString(byf));
        TransactionEventPublisher publisher = new TransactionEventPublisher(1024);

        publisher.withNonceEventHandler().withHashEventHandler().mergeEvents();
        publisher.start();
        publisher.publish(transaction);
        Thread.sleep(5000);
    }


    @Test
    public void CommitBlockTest() throws InterruptedException, IOException {
        BlockEventPublisher publisher = new BlockEventPublisher(1024);
        CommitteeBlock block = new CommitteeBlock();
        block.setHash("hash");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(block);
        out.close();

        byte[] buff= SerializationUtils.serialize(block);
        CommitteeBlock copy=SerializationUtils.deserialize(buff);
        assertEquals(block, copy);
        publisher.withHashHandler().mergeEvents();
        publisher.start();
        publisher.publish(block);


        Thread.sleep(5000);
    }

    @Test
    public void TransactionBlockTest() throws InterruptedException {
        BlockEventPublisher publisher = new BlockEventPublisher(1024);
        TransactionBlock block = new TransactionBlock();
        block.setHash("hash");

        publisher.withHashHandler().mergeEventsAndPassVerifySig();
        publisher.start();
        publisher.publish(block);


        Thread.sleep(5000);
    }
}
