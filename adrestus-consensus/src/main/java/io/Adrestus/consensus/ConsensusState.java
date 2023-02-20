package io.Adrestus.consensus;

import io.Adrestus.config.ConsensusConfiguration;
import io.Adrestus.consensus.ChangeView.ChangeViewCommitteeState;
import io.Adrestus.consensus.ChangeView.ChangeViewTransactionState;
import io.Adrestus.core.BlockIndex;
import io.Adrestus.core.IBlockIndex;
import io.Adrestus.core.Resourses.CachedEpochGeneration;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
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

public class ConsensusState extends ConsensusDataState {
    private static Logger LOG = LoggerFactory.getLogger(ConsensusState.class);
    private static final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private static final Lock r = rwl.readLock();
    private static final Lock w = rwl.writeLock();

    private static Timer transaction_block_timer;
    private static Timer committee_block_timer;
    private static CountDownLatch latch;
    private static IBlockIndex blockIndex;

    public ConsensusState() {
        this.transaction_block_timer = new Timer(ConsensusConfiguration.CONSENSUS);
        this.committee_block_timer = new Timer(ConsensusConfiguration.CONSENSUS);
    }

    public ConsensusState(CountDownLatch latch) {
        this.blockIndex = new BlockIndex();
        this.transaction_block_timer = new Timer(ConsensusConfiguration.CONSENSUS);
        this.committee_block_timer = new Timer(ConsensusConfiguration.CONSENSUS);
        this.latch = latch;
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

        public CommitteeBlockConsensusTask() {
            committee_state = new ConsensusVRFState();
            committee_state.onEnterState(blockIndex.getPublicKeyByIndex(0, 0));
            CachedLeaderIndex.getInstance().setCommitteePositionLeader(0);
        }

        public CommitteeBlockConsensusTask(AbstractState state) {
            if (committee_state == null) {
                committee_state = new ConsensusVRFState();
                committee_state.onEnterState(blockIndex.getPublicKeyByIndex(0, 0));
                CachedLeaderIndex.getInstance().setCommitteePositionLeader(0);
                previous_state = committee_state;
            } else {
                committee_state = state;
                previous_state = state;
            }
        }

        private void changeStateTo(AbstractState newState) {
            committee_state = newState;
        }

        public AbstractState getState() {
            return committee_state;
        }

        public void setState(AbstractState state) {
            committee_state = state;
        }

        @SneakyThrows
        @Override
        public void run() {
            w.lock();
            clear();
            try {
                boolean result = false;
                while (!result) {
                    result = committee_state.onActiveState();
                    if (!result) {
                        if (!committee_state.getClass().equals(ChangeViewCommitteeState.class)) {
                            previous_state = (AbstractState) committee_state.clone();
                        }
                        changeStateTo(new ChangeViewCommitteeState());
                        LOG.info("State changed to ChangeViewCommitteeState");
                        if (CachedLeaderIndex.getInstance().getCommitteePositionLeader() == CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).size() - 1)
                            CachedLeaderIndex.getInstance().setCommitteePositionLeader(0);
                        else {
                            CachedLeaderIndex.getInstance().setCommitteePositionLeader(CachedLeaderIndex.getInstance().getCommitteePositionLeader() + 1);
                        }
                        committee_state.onEnterState(blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), CachedLeaderIndex.getInstance().getCommitteePositionLeader()));
                    } else {
                        if (committee_state.getClass().equals(ChangeViewCommitteeState.class)) {
                            committee_state = previous_state;
                            committee_state.onEnterState(blockIndex.getPublicKeyByIndex(0, CachedLeaderIndex.getInstance().getCommitteePositionLeader()));
                        } else if (committee_state.getClass().equals(ConsensusCommitteeBlockState.class)) {
                            CachedLeaderIndex.getInstance().setCommitteePositionLeader(0);
                            changeStateTo(new ConsensusVRFState());
                            committee_state.onEnterState(blockIndex.getPublicKeyByIndex(0, CachedLeaderIndex.getInstance().getCommitteePositionLeader()));
                        } else if (committee_state.getClass().equals(ConsensusVRFState.class)) {
                            changeStateTo(new ConsensusVDFState());
                            committee_state.onEnterState(blockIndex.getPublicKeyByIndex(0, CachedLeaderIndex.getInstance().getCommitteePositionLeader()));
                        } else if (committee_state.getClass().equals(ConsensusVDFState.class)) {
                            changeStateTo(new ConsensusCommitteeBlockState());
                            committee_state.onEnterState(blockIndex.getPublicKeyByIndex(0, CachedLeaderIndex.getInstance().getCommitteePositionLeader()));
                        }
                    }
                }
            } finally {
                latch.countDown();
                w.unlock();

                //when to change the structure of the commitee block after how much epochs
                CachedEpochGeneration.getInstance().setEpoch_counter(0);
            }
        }

        public void clear() {
            if (committee_block_timer != null) {
                committee_block_timer.cancel();
                committee_block_timer.purge();
                committee_block_timer = null;
            }
        }
    }

    protected static final class TransactionBlockConsensusTask extends TimerTask {
        private AbstractState state;

        public TransactionBlockConsensusTask() {
            this.state = new ConsensusTransactionBlockState();
            this.state.onEnterState(null);
        }

        public TransactionBlockConsensusTask(AbstractState state) {
            this.state = state;
        }

        private void changeStateTo(AbstractState newState) {
            this.state = newState;
        }

        public AbstractState getState() {
            return state;
        }

        public void setState(AbstractState state) {
            this.state = state;
        }

        @SneakyThrows
        @Override
        public void run() {
            w.lock();
            clear();
            try {
                if (CachedEpochGeneration.getInstance().getEpoch_counter() >= ConsensusConfiguration.EPOCH_TRANSITION) {
                    committee_block_timer = new Timer(ConsensusConfiguration.CONSENSUS);
                    committee_block_timer.scheduleAtFixedRate(new CommitteeBlockConsensusTask(committee_state), ConsensusConfiguration.CONSENSUS_COMMITTEE_TIMER, ConsensusConfiguration.CONSENSUS_COMMITTEE_TIMER);
                    transaction_block_timer = new Timer(ConsensusConfiguration.CONSENSUS);
                    transaction_block_timer.scheduleAtFixedRate(new TransactionBlockConsensusTask(state), ConsensusConfiguration.CHANGE_VIEW_TIMER, ConsensusConfiguration.CHANGE_VIEW_TIMER);
                } else {
                    boolean result = state.onActiveState();
                    if (result) {
                        latch.countDown();
                        if (state.getClass().equals(ConsensusTransactionBlockState.class)) {
                            state.onEnterState(null);
                        } else {
                            LOG.info("State changed to  ConsensusTransactionBlockState");
                            changeStateTo(new ConsensusTransactionBlockState());
                            state.onEnterState(null);
                        }
                        transaction_block_timer = new Timer(ConsensusConfiguration.CONSENSUS);
                        transaction_block_timer.scheduleAtFixedRate(new TransactionBlockConsensusTask(state), ConsensusConfiguration.CONSENSUS_TIMER, ConsensusConfiguration.CONSENSUS_TIMER);
                    } else {
                        changeStateTo(new ChangeViewTransactionState());
                        LOG.info("State changed to ChangeViewTransactionState");
                        if (CachedLeaderIndex.getInstance().getTransactionPositionLeader() == 0) {
                            CachedLeaderIndex.getInstance().setTransactionPositionLeader(CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).size() - 1);
                            state.onEnterState(blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), CachedLeaderIndex.getInstance().getTransactionPositionLeader()));
                        } else {
                            CachedLeaderIndex.getInstance().setTransactionPositionLeader(CachedLeaderIndex.getInstance().getTransactionPositionLeader() - 1);
                            state.onEnterState(blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), CachedLeaderIndex.getInstance().getTransactionPositionLeader()));
                        }
                        transaction_block_timer = new Timer(ConsensusConfiguration.CONSENSUS);
                        transaction_block_timer.scheduleAtFixedRate(new TransactionBlockConsensusTask(state), ConsensusConfiguration.CHANGE_VIEW_TIMER, ConsensusConfiguration.CHANGE_VIEW_TIMER);
                    }
                }
            } finally {
                w.unlock();
                CachedEpochGeneration.getInstance().setEpoch_counter(CachedEpochGeneration.getInstance().getEpoch_counter() + 1);
            }
        }

        @Override
        public boolean cancel() {
            super.cancel();
            return true;
        }

        public void clear() {
            if (transaction_block_timer != null) {
                transaction_block_timer.cancel();
                transaction_block_timer.purge();
                transaction_block_timer = null;
            }
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
