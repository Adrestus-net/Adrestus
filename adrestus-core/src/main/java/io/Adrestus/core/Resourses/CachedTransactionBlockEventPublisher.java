package io.Adrestus.core.Resourses;

import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.core.RingBuffer.publisher.BlockEventPublisher;
import io.Adrestus.core.TransactionBlock;
import lombok.Getter;
import lombok.SneakyThrows;

@Getter
public class CachedTransactionBlockEventPublisher {
    private static volatile CachedTransactionBlockEventPublisher instance;

    private final BlockEventPublisher publisher;

    private CachedTransactionBlockEventPublisher() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        this.publisher = new BlockEventPublisher(AdrestusConfiguration.BLOCK_QUEUE_SIZE);
        this.publisher
                .withDuplicateHandler()
                .withGenerationHandler()
                .withHashHandler()
                .withHeaderEventHandler()
                .withHeightEventHandler()
                .withViewIDEventHandler()
                .withTimestampEventHandler()
                .withTransactionMerkleeEventHandler()
                .withInBoundEventHandler()
                .withOutBoundEventHandler()
                .withPatriciaTreeEventHandler()
                .withPatriciaTreeHeightEventHandler()
                .mergeEvents();


        this.publisher.start();
    }

    public static CachedTransactionBlockEventPublisher getInstance() {
        var result = instance;
        if (result == null) {
            synchronized (CachedTransactionBlockEventPublisher.class) {
                result = instance;
                if (result == null) {
                    result = new CachedTransactionBlockEventPublisher();
                    instance = result;
                }
            }
        }
        return result;
    }

    public void publish(TransactionBlock transactionBlock) {
        this.publisher.publish(transactionBlock);
    }

    @SneakyThrows
    public void close() {
        publisher.getJobSyncUntilRemainingCapacityZero();
        publisher.close();
        instance = null;
    }
}
