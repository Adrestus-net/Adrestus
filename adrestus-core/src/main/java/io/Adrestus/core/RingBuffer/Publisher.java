package io.Adrestus.core.RingBuffer;

public interface Publisher<T> {
    void start();

    int getJobQueueSize();

    void publish(T t);

    int getJobQueueRemainingCapacity();

    int getDroppedJobsCount();

    void getJobSyncUntilRemainingCapacityZero() throws InterruptedException;

    void close();
}
