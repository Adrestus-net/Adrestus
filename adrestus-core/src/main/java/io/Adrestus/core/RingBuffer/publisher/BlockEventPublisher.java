package io.Adrestus.core.RingBuffer.publisher;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.TimeoutException;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import io.Adrestus.core.AbstractBlock;
import io.Adrestus.core.RingBuffer.Publisher;
import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import io.Adrestus.core.RingBuffer.factory.AbstractBlockEventFactory;
import io.Adrestus.core.RingBuffer.handler.blocks.BlockEventHandler;
import io.Adrestus.core.RingBuffer.handler.blocks.HeaderEventHandler;
import io.Adrestus.util.BufferCapacity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockEventPublisher<T> implements Publisher<AbstractBlock> {
    private static Logger LOG = LoggerFactory.getLogger(BlockEventPublisher.class);
    private final AtomicInteger droppedJobsCount = new AtomicInteger();
    private final Disruptor<AbstractBlockEvent> disruptor;
    private final int bufferSize;
    private final int jobQueueSize;
    private final AtomicBoolean isRunning;
    private final List<BlockEventHandler> group;

    public BlockEventPublisher(int jobQueueSize) {
        this.isRunning = new AtomicBoolean(true);
        this.jobQueueSize = jobQueueSize;
        this.bufferSize = BufferCapacity.nextPowerOf2(this.jobQueueSize);
        //this.group=new ArrayList<AdrestusTransactionBlockEventHandler>();
        this.group = new ArrayList<BlockEventHandler>();
        disruptor = new Disruptor<AbstractBlockEvent>(new AbstractBlockEventFactory(), bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new BlockingWaitStrategy());
    }


    public BlockEventPublisher<T> withHashHandler() {
        group.add(new HeaderEventHandler());
        return this;
    }

    public BlockEventPublisher mergeEvents() {
        BlockEventHandler[] events = new BlockEventHandler[group.size()];
        group.toArray(events);
        disruptor.handleEventsWith(events);
        return this;
    }

    @Override
    public void start() {
        disruptor.start();
    }

    @Override
    public int getJobQueueSize() {
        return bufferSize - getJobQueueRemainingCapacity();
    }

    @Override
    public void publish(AbstractBlock block) {
        long sequence = this.disruptor.getRingBuffer().next();  // Grab the next sequence
        try {
            AbstractBlockEvent event = (AbstractBlockEvent) this.disruptor.getRingBuffer().get(sequence); // Get the entry in the Disruptor
            event.setBlock(block);
        } finally {
            this.disruptor.getRingBuffer().publish(sequence);
        }
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
        } catch (TimeoutException e) {
            LOG.trace("Disruptor shutdown interrupted", e);
        } finally {
            LOG.trace("Disruptor shutdown completed.");
        }
    }

}
