package io.Adrestus.core.Resourses;

import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.RingBuffer.publisher.BlockEventPublisher;
import lombok.Getter;
import lombok.SneakyThrows;

@Getter
public class CachedCommitteeBlockEventPublisher {
    private static volatile CachedCommitteeBlockEventPublisher instance;

    private final BlockEventPublisher publisher;

    private CachedCommitteeBlockEventPublisher() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        this.publisher = new BlockEventPublisher(AdrestusConfiguration.BLOCK_QUEUE_SIZE);
        this.publisher
                .withHashHandler()
                .withHeaderEventHandler()
                .withTimestampEventHandler()
                .withDuplicateHandler()
                .withHeightEventHandler()
                .withViewIDEventHandler()
                .withSortedStakingEventHandler()
                .withMinimumStakingEventHandler()
                .withVerifyDifficultyEventHandler()
                .withVerifyVDFEventHandler()
                .withVRFEventHandler()
                .withRandomizedEventHandler()
                .withLeaderRandomnessEventHandler()
                .mergeEvents();

        this.publisher.start();
    }

    public static CachedCommitteeBlockEventPublisher getInstance() {
        var result = instance;
        if (result == null) {
            synchronized (CachedCommitteeBlockEventPublisher.class) {
                result = instance;
                if (result == null) {
                    result = new CachedCommitteeBlockEventPublisher();
                    instance = result;
                }
            }
        }
        return result;
    }

    public void publish(CommitteeBlock committeeBlock) {
        this.publisher.publish(committeeBlock);
    }

    @SneakyThrows
    public void close() {
        publisher.getJobSyncUntilRemainingCapacityZero();
        publisher.close();
        instance = null;
    }
}
