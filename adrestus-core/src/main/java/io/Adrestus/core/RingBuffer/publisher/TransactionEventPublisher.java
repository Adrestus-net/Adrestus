package io.Adrestus.core.RingBuffer.publisher;

import com.google.common.base.Objects;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.TimeoutException;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.core.RingBuffer.Publisher;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import io.Adrestus.core.RingBuffer.factory.TransactionEventFactory;
import io.Adrestus.core.RingBuffer.handler.transactions.*;
import io.Adrestus.core.Transaction;
import io.Adrestus.util.BufferCapacity;
import io.Adrestus.util.ThreadCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TransactionEventPublisher implements Publisher<Transaction> {
    private static Logger LOG = LoggerFactory.getLogger(TransactionEventPublisher.class);
    private ExecutorService executor;
    private final AtomicInteger droppedJobsCount = new AtomicInteger();
    private final int numberOfWorkers;
    private final int jobQueueSize;
    private final int bufferSize;
    private final Disruptor<TransactionEvent> disruptor;
    private final AtomicBoolean isRunning;
    private final ThreadCalculator threadCalculator;
    private final List<TransactionEventHandler> group;

    private RingBuffer<TransactionEvent> ringBuffer;

    public TransactionEventPublisher(int jobQueueSize) {
        this.isRunning = new AtomicBoolean(true);
        this.threadCalculator = new ThreadCalculator();
        this.numberOfWorkers = this.threadCalculator.calculateOptimalThreadCount();
        this.jobQueueSize = jobQueueSize;
        this.bufferSize = BufferCapacity.nextPowerOf2(this.jobQueueSize);
        try {
            this.executor = Executors.newFixedThreadPool(numberOfWorkers);
        } catch (IllegalArgumentException e) {
            this.executor = Executors.newFixedThreadPool(AdrestusConfiguration.CORES);
        }
        this.group = new ArrayList<TransactionEventHandler>();
        disruptor = new Disruptor<>(new TransactionEventFactory(), bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new BlockingWaitStrategy());
        LOG.info("Script engine worker pool created with " + numberOfWorkers + " threads");
    }

    @Override
    public void start() {

        ringBuffer = disruptor.start();
    }

    @Override
    public void publish(Transaction transaction) {
        long sequence = ringBuffer.next();  // Grab the next sequence
        TransactionEvent event = ringBuffer.get(sequence); // Get the entry in the Disruptor
        event.setTransaction(transaction); // Fill with data
        ringBuffer.publish(sequence);
    }

    @Override
    public int getJobQueueSize() {
        return bufferSize - getJobQueueRemainingCapacity();
    }

    @Override
    public int getJobQueueRemainingCapacity() {
        return (int) disruptor.getRingBuffer().remainingCapacity();
    }

    @Override
    public int getDroppedJobsCount() {
        return droppedJobsCount.get();
    }

    @Override
    public void getJobSyncUntilRemainingCapacityZero() throws InterruptedException {
        while (disruptor.getRingBuffer().remainingCapacity() != bufferSize) {
            Thread.sleep(100);
        }
        try {
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.trace("Executor shutdown interrupted", e);
        } finally {
            LOG.trace("Executor shutdown completed.");
        }
    }

    @Override
    public void close() {
        LOG.trace("Shutting down executor...");
        isRunning.set(false);
        try {
            disruptor.shutdown(5, TimeUnit.SECONDS);
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException e) {
            LOG.trace("Executor shutdown interrupted", e);
        } finally {
            LOG.trace("Executor shutdown completed.");
        }
    }

    public TransactionEventPublisher withAddressSizeEventHandler() {
        group.add(new AddressSizeEventHandler());
        return this;
    }

    public TransactionEventPublisher withAmountEventHandler() {
        group.add(new AmountEventHandler());
        return this;
    }

    public TransactionEventPublisher withDelegateEventHandler() {
        group.add(new DelegateEventHandler());
        return this;
    }

    public TransactionEventPublisher withDoubleSpendEventHandler() {
        group.add(new DoubleSpendEventHandler());
        return this;
    }

    public TransactionEventPublisher withHashEventHandler() {
        group.add(new HashEventHandler());
        return this;
    }


    public TransactionEventPublisher withNonceEventHandler() {
        group.add(new NonceEventHandler());
        return this;
    }

    public TransactionEventPublisher withReplayEventHandler() {
        group.add(new ReplayEventHandler());
        return this;
    }

    public TransactionEventPublisher withRewardEventHandler() {
        group.add(new RewardEventHandler());
        return this;
    }

    public TransactionEventPublisher withStakingEventHandler() {
        group.add(new StakingEventHandler());
        return this;
    }

    public TransactionEventPublisher withTransactionFeeEventHandler() {
        group.add(new TransactionFeeEventHandler());
        return this;
    }

    public TransactionEventPublisher withTimestampEventHandler() {
        group.add(new TimestampEventHandler());
        return this;
    }

    public TransactionEventPublisher mergeEvents() {
        TransactionEventHandler[] events = new TransactionEventHandler[group.size()];
        group.toArray(events);
        disruptor.handleEventsWith(events);
        return this;
    }

    public TransactionEventPublisher mergeEventsAndPassThen(SignatureEventHandler signatureEventHandler) {
        TransactionEventHandler[] events = new TransactionEventHandler[group.size()];
        group.toArray(events);
        signatureEventHandler.setExecutorService(executor);
        disruptor.handleEventsWith(events).then(signatureEventHandler);
        return this;
    }


    public static Logger getLOG() {
        return LOG;
    }

    public static void setLOG(Logger LOG) {
        TransactionEventPublisher.LOG = LOG;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public int getNumberOfWorkers() {
        return numberOfWorkers;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public Disruptor<TransactionEvent> getDisruptor() {
        return disruptor;
    }

    public AtomicBoolean getIsRunning() {
        return isRunning;
    }

    public ThreadCalculator getThreadCalculator() {
        return threadCalculator;
    }

    public List<TransactionEventHandler> getGroup() {
        return group;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionEventPublisher publisher = (TransactionEventPublisher) o;
        return numberOfWorkers == publisher.numberOfWorkers && jobQueueSize == publisher.jobQueueSize && bufferSize == publisher.bufferSize && Objects.equal(droppedJobsCount, publisher.droppedJobsCount) && Objects.equal(executor, publisher.executor) && Objects.equal(disruptor, publisher.disruptor) && Objects.equal(isRunning, publisher.isRunning) && Objects.equal(threadCalculator, publisher.threadCalculator) && Objects.equal(group, publisher.group);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(droppedJobsCount, executor, numberOfWorkers, jobQueueSize, bufferSize, disruptor, isRunning, threadCalculator, group);
    }

    @Override
    public String toString() {
        return "TransactionEventPublisher{" +
                "droppedJobsCount=" + droppedJobsCount +
                ", executor=" + executor +
                ", numberOfWorkers=" + numberOfWorkers +
                ", jobQueueSize=" + jobQueueSize +
                ", bufferSize=" + bufferSize +
                ", disruptor=" + disruptor +
                ", isRunning=" + isRunning +
                ", threadCalculator=" + threadCalculator +
                ", group=" + group +
                '}';
    }
}
