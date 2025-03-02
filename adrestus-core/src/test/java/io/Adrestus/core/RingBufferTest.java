package io.Adrestus.core;

import io.Adrestus.core.RingBuffer.publisher.BlockEventPublisher;
import io.Adrestus.core.RingBuffer.publisher.TransactionEventPublisher;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.elliptic.mapper.BigDecimalSerializer;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationFuryUtil;
import io.Adrestus.util.SerializationUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RingBufferTest {
    static TransactionEventPublisher publisher = new TransactionEventPublisher(1024);

    @BeforeAll
    public static void setup() {
        publisher
                .withAddressSizeEventHandler()
                .withTypeEventHandler()
                .withAmountEventHandler()
                .withDoubleSpendEventHandler()
                .withHashEventHandler()
                .withDelegateEventHandler()
                .withNonceEventHandler()
                .withReplayEventHandler()
                .withStakingEventHandler()
                .withTransactionFeeEventHandler()
                .withTimestampEventHandler()
                .withSameOriginEventHandler()
                .withZoneEventHandler()
                .withSecp256k1EventHandler()
                .withDuplicateEventHandler()
                .withMinimumStakingEventHandler()
                .mergeEvents();
        publisher.start();
    }

    //CHANGE PUBLISHER START NOT TO INSTATIED IN CONSENUS BLOCK CAUSE ADDS OVERHEAD
    // @Test
    public void TransactionTest() throws InterruptedException {
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        SerializationUtil<Transaction> serenc = new SerializationUtil<Transaction>(Transaction.class, list);

        Transaction transaction = new RewardsTransaction("Delegator Address");
        transaction.setFrom("address1");
        transaction.setStatus(StatusType.PENDING);
        transaction.setTimestamp(GetTime.GetTimeStampInString());
        transaction.setZoneFrom(0);
        transaction.setZoneTo(0);
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setAmountWithTransactionFee(transaction.getAmount().multiply(BigDecimal.valueOf(10.0 / 100.0)));
        transaction.setNonce(1);
        byte byf[] = serenc.encode(transaction, 1024);
        transaction.setHash(HashUtil.sha256_bytetoString(byf));
        publisher.publish(transaction);
        publisher.getJobSyncUntilRemainingCapacityZero();
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

        byte[] buff = SerializationFuryUtil.getInstance().getFury().serialize(block);
        CommitteeBlock copy = (CommitteeBlock) SerializationFuryUtil.getInstance().getFury().deserialize(buff);
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
        ArrayList<Transaction> list = new ArrayList<>();
        Transaction transaction = new RegularTransaction("hash1");
        Transaction transaction2 = new RegularTransaction("hash2");
        list.add(transaction);
        block.setTransactionList(list);
        publisher.withHashHandler().mergeEventsAndPassVerifySig();
        publisher.start();


        System.out.println("wait");
        publisher.publish(block);
        publisher.getJobSyncUntilRemainingCapacityZero();
        System.out.println("finish");

        ArrayList<Transaction> list2 = new ArrayList<>();
        list2.add(transaction2);
        block.setTransactionList(list2);

        System.out.println("wait");
        publisher.publish(block);
        publisher.getJobSyncUntilRemainingCapacityZero();
        System.out.println("finish");
        Thread.sleep(5000);
    }
}
