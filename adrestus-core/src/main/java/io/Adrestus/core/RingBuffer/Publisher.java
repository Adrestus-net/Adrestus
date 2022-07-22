package io.Adrestus.core.RingBuffer;

import io.Adrestus.core.Transaction;

public interface Publisher<T> {
    void start();

    int getJobQueueSize();

    void publish(T t);

    int getJobQueueRemainingCapacity();

    int getDroppedJobsCount();

    void close();
}
