package io.Adrestus.consensus;

import io.Adrestus.config.ConsensusConfiguration;
import io.Adrestus.consensus.ChangeView.ChangeViewCommitteeState;
import io.Adrestus.consensus.ChangeView.ChangeViewTransactionState;
import io.Adrestus.core.BlockIndex;
import io.Adrestus.core.IBlockIndex;
import io.Adrestus.core.Resourses.CachedEpochGeneration;
import io.Adrestus.core.Resourses.CachedLeaderIndex;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConsensusState {
    private static Logger LOG = LoggerFactory.getLogger(ConsensusState.class);
    private static final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private static final Lock r = rwl.readLock();
    private static final Lock w = rwl.writeLock();

    private static AbstractState state;
    private static Timer transaction_block_timer;
    private static Timer committee_block_timer;
    private static CountDownLatch latch;
    private static IBlockIndex blockIndex;

    public ConsensusState() {
        this.transaction_block_timer = new Timer(ConsensusConfiguration.CONSENSUS);
        this.committee_block_timer = new Timer(ConsensusConfiguration.CONSENSUS);
        this.state = new ConsensusTransactionBlockState();
        this.state.onEnterState(null);
    }

    public ConsensusState(CountDownLatch latch) {
        this.state = new ConsensusTransactionBlockState();
        this.state.onEnterState(null);
        this.blockIndex = new BlockIndex();
        this.transaction_block_timer = new Timer(ConsensusConfiguration.CONSENSUS);
        this.committee_block_timer = new Timer(ConsensusConfiguration.CONSENSUS);
        this.latch = latch;
    }

    public ConsensusState(AbstractState state) {
        this.state = state;
    }

    private static void changeStateTo(AbstractState newState) {
        state = newState;
    }


    public static Logger getLOG() {
        return LOG;
    }

    public static void setLOG(Logger LOG) {
        ConsensusState.LOG = LOG;
    }

    public ReentrantReadWriteLock getRwl() {
        return rwl;
    }

    public Lock getR() {
        return r;
    }

    public Lock getW() {
        return w;
    }

    public AbstractState getState() {
        return state;
    }

    public void setState(AbstractState state) {
        this.state = state;
    }

    public Timer getTransaction_block_timer() {
        return transaction_block_timer;
    }

    public void setTransaction_block_timer(Timer transaction_block_timer) {
        this.transaction_block_timer = transaction_block_timer;
    }

    public Timer getCommittee_block_timer() {
        return committee_block_timer;
    }

    public void setCommittee_block_timer(Timer committee_block_timer) {
        this.committee_block_timer = committee_block_timer;
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    public IBlockIndex getBlockIndex() {
        return blockIndex;
    }

    public void setBlockIndex(IBlockIndex blockIndex) {
        this.blockIndex = blockIndex;
    }

    protected static final class CommitteeBlockConsensusTask extends TimerTask {
        @SneakyThrows
        @Override
        public void run() {
            committee_block_timer.cancel();
            committee_block_timer.purge();
            w.lock();
            try {
                if (CachedEpochGeneration.getInstance().getEpoch_counter() < ConsensusConfiguration.EPOCH_TRANSITION) {
                    committee_block_timer = new Timer(ConsensusConfiguration.CONSENSUS);
                    committee_block_timer.scheduleAtFixedRate(new CommitteeBlockConsensusTask(), ConsensusConfiguration.CONSENSUS_COMMITTEE_TIMER, ConsensusConfiguration.CONSENSUS_COMMITTEE_TIMER);
                } else {
                    boolean result = state.onActiveState();
                    if (result) {
                        latch.countDown();
                        committee_block_timer = new Timer(ConsensusConfiguration.CONSENSUS);
                        committee_block_timer.scheduleAtFixedRate(new CommitteeBlockConsensusTask(), ConsensusConfiguration.CONSENSUS_COMMITTEE_TIMER, ConsensusConfiguration.CONSENSUS_COMMITTEE_TIMER);
                        state.onEnterState(blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), CachedLeaderIndex.getInstance().getCommitteePositionLeader()));
                    } else {
                        committee_block_timer = new Timer(ConsensusConfiguration.CONSENSUS);
                        transaction_block_timer.scheduleAtFixedRate(new CommitteeBlockConsensusTask(), ConsensusConfiguration.CONSENSUS_COMMITTEE_TIMER, ConsensusConfiguration.CONSENSUS_COMMITTEE_TIMER);
                        changeStateTo(new ChangeViewCommitteeState());
                        LOG.info("State changed to ChangeViewCommitteeState");
                        state.onEnterState(blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), CachedLeaderIndex.getInstance().getTransactionPositionLeader()));
                    }
                }
            } finally {
                w.unlock();
            }
        }
    }

    protected static final class TransactionBlockConsensusTask extends TimerTask {


        @SneakyThrows
        @Override
        public void run() {
            transaction_block_timer.cancel();
            transaction_block_timer.purge();
            w.lock();
            try {
                if (CachedEpochGeneration.getInstance().getEpoch_counter() >= ConsensusConfiguration.EPOCH_TRANSITION) {
                    transaction_block_timer = new Timer(ConsensusConfiguration.CONSENSUS);
                    transaction_block_timer.scheduleAtFixedRate(new TransactionBlockConsensusTask(), ConsensusConfiguration.CONSENSUS_TIMER, ConsensusConfiguration.CONSENSUS_TIMER);
                } else {
                    boolean result = state.onActiveState();
                    if (result) {
                        latch.countDown();
                        transaction_block_timer = new Timer(ConsensusConfiguration.CONSENSUS);
                        transaction_block_timer.scheduleAtFixedRate(new TransactionBlockConsensusTask(), ConsensusConfiguration.CONSENSUS_TIMER, ConsensusConfiguration.CONSENSUS_TIMER);
                        if (state.getClass().equals(ConsensusTransactionBlockState.class)) {
                            state.onEnterState(null);
                        }
                        else {
                            LOG.info("State changed to  ConsensusTransactionBlockState");
                            changeStateTo(new ConsensusTransactionBlockState());
                            state.onEnterState(null);
                        }
                    } else {
                        transaction_block_timer = new Timer(ConsensusConfiguration.CONSENSUS);
                        transaction_block_timer.scheduleAtFixedRate(new TransactionBlockConsensusTask(), ConsensusConfiguration.CHANGE_VIEW_TIMER, ConsensusConfiguration.CHANGE_VIEW_TIMER);
                        changeStateTo(new ChangeViewTransactionState());
                        LOG.info("State changed to ChangeViewTransactionState");
                        state.onEnterState(blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), CachedLeaderIndex.getInstance().getTransactionPositionLeader()));
                    }
                }
            } finally {
                w.unlock();
            }
        }

        @Override
        public boolean cancel() {
            super.cancel();
            return true;
        }
    }

   /* protected static final class ChangeViewTransactionConsensusTask extends TimerTask {


        @SneakyThrows
        @Override
        public void run() {
            transaction_block_timer.cancel();
            boolean result = state.onActiveState();
            if (result) {
                latch.countDown();
                transaction_block_timer = new Timer(ConsensusConfiguration.CONSENSUS);
                transaction_block_timer.scheduleAtFixedRate(new TransactionBlockConsensusTask(), ConsensusConfiguration.CONSENSUS_TIMER, ConsensusConfiguration.CONSENSUS_TIMER);
                changeStateTo(new ConsensusTransactionBlockState());
                LOG.info("State changed to TransactionBlockState");
                state.onEnterState(blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), CachedLeaderIndex.getInstance().getTransactionPositionLeader()));
            } else {
                transaction_block_timer = new Timer(ConsensusConfiguration.CONSENSUS);
                transaction_block_timer.scheduleAtFixedRate(new ChangeViewTransactionConsensusTask(), ConsensusConfiguration.CHANGE_VIEW_TIMER, ConsensusConfiguration.CHANGE_VIEW_TIMER);
                state.onEnterState(blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), CachedLeaderIndex.getInstance().getTransactionPositionLeader()));
            }
        }

        @Override
        public boolean cancel() {
            super.cancel();
            return true;
        }
    }

    protected static final class ChangeViewCommitteeConsensusTask extends TimerTask {


        @SneakyThrows
        @Override
        public void run() {
            committee_block_timer.cancel();
            boolean result = state.onActiveState();
            if (result) {
                latch.countDown();
                committee_block_timer = new Timer(ConsensusConfiguration.CONSENSUS);
                committee_block_timer.scheduleAtFixedRate(new CommitteeBlockConsensusTask(), ConsensusConfiguration.CONSENSUS_COMMITTEE_TIMER, ConsensusConfiguration.CONSENSUS_COMMITTEE_TIMER);
                changeStateTo(new ConsensusCommitteeBlockState());
                LOG.info("State changed to CommitteeBlockState");
                state.onEnterState(blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), CachedLeaderIndex.getInstance().getCommitteePositionLeader()));
            } else {
                transaction_block_timer = new Timer(ConsensusConfiguration.CONSENSUS);
                transaction_block_timer.scheduleAtFixedRate(new ChangeViewCommitteeConsensusTask(), ConsensusConfiguration.CONSENSUS_COMMITTEE_TIMER, ConsensusConfiguration.CONSENSUS_COMMITTEE_TIMER);
                state.onEnterState(blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), CachedLeaderIndex.getInstance().getCommitteePositionLeader()));
            }
        }

        @Override
        public boolean cancel() {
            super.cancel();
            return true;
        }
    }*/
}
