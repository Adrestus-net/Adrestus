package io.Adrestus.core;

import io.Adrestus.core.RingBuffer.handler.transactions.SignatureEventHandler;
import io.Adrestus.core.RingBuffer.publisher.BlockEventPublisher;
import io.Adrestus.core.RingBuffer.publisher.TransactionEventPublisher;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RingBufferTest {
   static TransactionEventPublisher publisher = new TransactionEventPublisher(1024);
    @BeforeAll
    public static void setup(){
        publisher
                .withAddressSizeEventHandler()
                .withAmountEventHandler()
                .withDelegateEventHandler()
                .withDoubleSpendEventHandler()
                .withHashEventHandler()
                .withNonceEventHandler()
                .withReplayEventHandler()
                .withRewardEventHandler()
                .withStakingEventHandler()
                .withTransactionFeeEventHandler()
                .withTimestampEventHandler()
                .withSameOriginEventHandler()
                .withZoneEventHandler()
                .withSecp256k1EventHandler()
                .withDuplicateEventHandler()
                .mergeEvents();
        publisher.start();
    }

    //CHANGE PUBLISHER START NOT TO INSTATIED IN CONSENUS BLOCK CAUSE ADDS OVERHEAD
    @Test
    public void TransactionTest() throws InterruptedException {
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        SerializationUtil<Transaction> serenc = new SerializationUtil<Transaction>(Transaction.class, list);

        Transaction transaction = new RewardsTransaction("Delegator Address");
        transaction.setFrom("address1");
        transaction.setStatus(StatusType.PENDING);
        transaction.setTimestamp(GetTime.GetTimeStampInString());
        transaction.setZoneFrom(0);
        transaction.setZoneTo(0);
        transaction.setAmount(100);
        transaction.setAmountWithTransactionFee(transaction.getAmount() * (10.0 / 100.0));
        transaction.setNonce(1);
        byte byf[] = serenc.encode(transaction);
        transaction.setHash(HashUtil.sha256_bytetoString(byf));
        publisher.publish(transaction);
        //publisher.getJobSyncUntilRemainingCapacityZero();
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

        byte[] buff = SerializationUtils.serialize(block);
        CommitteeBlock copy = SerializationUtils.deserialize(buff);
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
