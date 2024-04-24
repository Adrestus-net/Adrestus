package io.Adrestus.core.RingBuffer.publisher;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.TimeoutException;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import io.Adrestus.core.ReceiptBlock;
import io.Adrestus.core.RingBuffer.Publisher;
import io.Adrestus.core.RingBuffer.event.ReceiptBlockEvent;
import io.Adrestus.core.RingBuffer.factory.ReceiptBlockEventFactory;
import io.Adrestus.core.RingBuffer.handler.receipts.*;
import io.Adrestus.util.BufferCapacity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ReceiptEventPublisher implements Publisher<ReceiptBlock> {
    private static Logger LOG = LoggerFactory.getLogger(ReceiptEventPublisher.class);
    private final AtomicInteger droppedJobsCount = new AtomicInteger();
    private final Disruptor<ReceiptBlockEvent> disruptor;
    private final int bufferSize;
    private final int jobQueueSize;
    private final AtomicBoolean isRunning;
    private final List<ReceiptEventHandler> group;

    public ReceiptEventPublisher(int jobQueueSize) {
        this.isRunning = new AtomicBoolean(true);
        this.jobQueueSize = jobQueueSize;
        this.bufferSize = BufferCapacity.nextPowerOf2(this.jobQueueSize);
        this.group = new ArrayList<ReceiptEventHandler>();
        disruptor = new Disruptor<ReceiptBlockEvent>(new ReceiptBlockEventFactory(), bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new BlockingWaitStrategy());
    }


    public ReceiptEventPublisher withEmptyEventHandler() {
        group.add(new EmptyEventHandler());
        return this;
    }


    public ReceiptEventPublisher withPublicKeyEventHandler() {
        group.add(new PublicKeyEventHandler());
        return this;
    }

    public ReceiptEventPublisher withSignatureEventHandler() {
        group.add(new SignatureEventHandler());
        return this;
    }


    public ReceiptEventPublisher withZoneFromEventHandler() {
        group.add(new ZoneFromEventHandler());
        return this;
    }


    public ReceiptEventPublisher withGenerationEventHandler() {
        group.add(new GenerationEventHandler());
        return this;
    }


    public ReceiptEventPublisher withHeightEventHandler() {
        group.add(new HeightEventHandler());
        return this;
    }

    public ReceiptEventPublisher withOutboundMerkleEventHandler() {
        group.add(new OutboundMerkleEventHandler());
        return this;
    }

    public ReceiptEventPublisher withZoneEventHandler() {
        group.add(new ZoneToEventHandler());
        return this;
    }


    public ReceiptEventPublisher withReplayEventHandler() {
        group.add(new ReplayEventHandler());
        return this;
    }

    public ReceiptEventPublisher mergeEvents() {
        ReceiptEventHandler[] events = new ReceiptEventHandler[group.size()];
        group.toArray(events);
        disruptor.handleEventsWith(events).then(new ReceiptInsertEventHandler()).then(new ReceiptClearingEventHandler());
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
    public void publish(ReceiptBlock receiptBlock) {
        long sequence = this.disruptor.getRingBuffer().next();  // Grab the next sequence
        try {
            ReceiptBlockEvent event = (ReceiptBlockEvent) this.disruptor.getRingBuffer().get(sequence); // Get the entry in the Disruptor
            event.setReceiptBlock(receiptBlock);
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
    public void getJobSyncUntilRemainingCapacityZero() throws InterruptedException {
        while (disruptor.getRingBuffer().remainingCapacity() != bufferSize) {
            Thread.sleep(20);
        }
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
