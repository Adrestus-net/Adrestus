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
import io.Adrestus.core.RingBuffer.handler.blocks.*;
import io.Adrestus.util.BufferCapacity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockEventPublisher implements Publisher<AbstractBlock> {
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


    public BlockEventPublisher withDuplicateHandler() {
        group.add(new DuplicateEventHandler());
        return this;
    }

    public BlockEventPublisher withGenerationHandler() {
        group.add(new GenerationEventHandler());
        return this;
    }

    public BlockEventPublisher withPatriciaTreeEventHandler(){
        group.add(new PatriciaTreeEventHandler());
        return this;
    }

    public BlockEventPublisher withHashHandler() {
        group.add(new HashEventHandler());
        return this;
    }

    public BlockEventPublisher withSortedStakingEventHandler() {
        group.add(new SortedStakingEventHandler());
        return this;
    }

    public BlockEventPublisher withRandomizedEventHandler() {
        group.add(new RandomizedEventHandler());
        return this;
    }

    public BlockEventPublisher withMinimumStakingEventHandler() {
        group.add(new MinimumStakingEventHandler());
        return this;
    }

    public BlockEventPublisher withVRFEventHandler() {
        group.add(new VRFEventHandler());
        return this;
    }

    public BlockEventPublisher withVerifyDifficultyEventHandler() {
        group.add(new DifficultyEventHandler());
        return this;
    }

    public BlockEventPublisher withInBoundEventHandler() {
        group.add(new InBoundEventHandler());
        return this;
    }

    public BlockEventPublisher withOutBoundEventHandler() {
        group.add(new OutBoundEventHandler());
        return this;
    }

    public BlockEventPublisher withVerifyVDFEventHandler() {
        group.add(new VDFVerifyEventHandler());
        return this;
    }

    public BlockEventPublisher withHeaderEventHandler() {
        group.add(new HeaderEventHandler());
        return this;
    }

    public BlockEventPublisher withHeightEventHandler() {
        group.add(new HeightEventHandler());
        return this;
    }

    public BlockEventPublisher withTimestampEventHandler() {
        group.add(new TimeStampEventHandler());
        return this;
    }

    public BlockEventPublisher withTransactionMerkleeEventHandler() {
        group.add(new TransactionsMerkleeEventHandler());
        return this;
    }

    public BlockEventPublisher withLeaderRandomnessEventHandler() {
        group.add(new LeaderRandomnessEventHandler());
        return this;
    }


    public BlockEventPublisher mergeEvents() {
        BlockEventHandler[] events = new BlockEventHandler[group.size()];
        group.toArray(events);
        disruptor.handleEventsWith(events);
        return this;
    }

    public BlockEventPublisher mergeEventsAndPassVerifySig() {
        BlockEventHandler[] events = new BlockEventHandler[group.size()];
        group.toArray(events);
        disruptor.handleEventsWith(events).then(new TransactionsSignatureEventHandler());
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
    public void getJobSyncUntilRemainingCapacityZero() throws InterruptedException {
        while (disruptor.getRingBuffer().remainingCapacity() != bufferSize) {
            Thread.sleep(100);
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
