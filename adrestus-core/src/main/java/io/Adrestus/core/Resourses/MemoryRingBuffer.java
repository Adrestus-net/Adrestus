package io.Adrestus.core.Resourses;

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
        this.publisher = new TransactionEventPublisher(2048);
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
                .withZoneEventHandler()
                .withDuplicateEventHandler()
                .AddmergeEventsAndPassThen(new SameOriginEventHandler(), new SignatureEventHandler(SignatureEventHandler.SignatureBehaviorType.SIMPLE_TRANSACTIONS));
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
