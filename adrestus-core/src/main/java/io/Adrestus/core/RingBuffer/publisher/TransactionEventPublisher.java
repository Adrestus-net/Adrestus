package io.Adrestus.core.RingBuffer.publisher;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.TimeoutException;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import io.Adrestus.core.RingBuffer.Publisher;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import io.Adrestus.core.RingBuffer.factory.TransactionEventFactory;
import io.Adrestus.core.RingBuffer.handler.transactions.SignatureEventHandler;
import io.Adrestus.core.Transaction;
import io.Adrestus.util.ThreadCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TransactionEventPublisher implements Publisher<Transaction> {
    private static Logger LOG = LoggerFactory.getLogger(TransactionEventPublisher.class);
    private final AtomicInteger droppedJobsCount = new AtomicInteger();
    private final ExecutorService executor;
    private final int numberOfWorkers;
    private final int jobQueueSize;
    private final int bufferSize;
    private final Disruptor<TransactionEvent> disruptor;
    private final AtomicBoolean isRunning;
    private final ThreadCalculator threadCalculator;

    public TransactionEventPublisher(int jobQueueSize) {
        this.isRunning = new AtomicBoolean(true);
        this.threadCalculator = new ThreadCalculator();
        this.numberOfWorkers = this.threadCalculator.calculateOptimalThreadCount();
        this.jobQueueSize = jobQueueSize;
        this.bufferSize = nextPowerOf2(this.jobQueueSize);
        this.executor = Executors.newFixedThreadPool(numberOfWorkers);
        disruptor = new Disruptor<>(new TransactionEventFactory(), bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new BlockingWaitStrategy());
        LOG.info("Script engine worker pool created with " + numberOfWorkers + " threads");
    }

    @Override
    public void start() {
        SignatureEventHandler signatureEventHandler = new SignatureEventHandler();
        disruptor.handleEventsWith(signatureEventHandler);
        disruptor.start();
    }

    @Override
    public void publish(Transaction transaction) {
        long sequence = this.disruptor.getRingBuffer().next();  // Grab the next sequence
        try {
            TransactionEvent event = this.disruptor.getRingBuffer().get(sequence); // Get the entry in the Disruptor
            event.setTransaction(transaction); // Fill with data
        } finally {
            this.disruptor.getRingBuffer().publish(sequence);
        }
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

    private int nextPowerOf2(int maxQueueCapacity) {
        int adjustedCapacity = maxQueueCapacity == 1 ? 1 : Integer.highestOneBit(maxQueueCapacity - 1) * 2;
        if (adjustedCapacity != maxQueueCapacity) {
            LOG.warn(String.format("Adjusting %d to nearest power of 2 ->  %d", maxQueueCapacity, adjustedCapacity));
        }
        return adjustedCapacity;
    }
}
