package io.Adrestus.core.Resourses;

import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.core.RingBuffer.handler.transactions.DuplicateEventHandler;
import io.Adrestus.core.RingBuffer.handler.transactions.SameOriginEventHandler;
import io.Adrestus.core.RingBuffer.handler.transactions.SignatureEventHandler;
import io.Adrestus.core.RingBuffer.publisher.TransactionEventPublisher;
import io.Adrestus.core.Transaction;
import lombok.SneakyThrows;

public class MemoryRingBuffer {
    private static volatile MemoryRingBuffer instance;
    private final TransactionEventPublisher publisher;

    private MemoryRingBuffer() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        this.publisher = new TransactionEventPublisher(AdrestusConfiguration.TRANSACTIONS_QUEUE_SIZE);
    }

    public static MemoryRingBuffer getInstance() {
        var result = instance;
        if (result == null) {
            synchronized (MemoryRingBuffer.class) {
                result = instance;
                if (result == null) {
                    instance = result = new MemoryRingBuffer();
                }
            }
        }
        return result;
    }

    public void setup() {
        publisher
                .withAddressSizeEventHandler()
                .withTypeEventHandler()
                .withAmountEventHandler()
                .withDoubleSpendEventHandler()
                .withHashEventHandler()
                .withNonceEventHandler()
                .withReplayEventHandler()
                .withStakingEventHandler()
                .withTransactionFeeEventHandler()
                .withTimestampEventHandler()
                .withZoneEventHandler()
                .AddMergeEventsAndPassThen(new SignatureEventHandler(SignatureEventHandler.SignatureBehaviorType.SIMPLE_TRANSACTIONS), new DuplicateEventHandler(), new SameOriginEventHandler());
        publisher.start();
    }

    public void publish(Transaction transaction) {
        this.publisher.publish(transaction);
    }

    @SneakyThrows
    public void close() {
        publisher.getJobSyncUntilRemainingCapacityZero();
        publisher.close();
        instance = null;
    }
}
