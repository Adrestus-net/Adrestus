package io.Adrestus.consensus;

import io.Adrestus.config.ConsensusConfiguration;
import io.Adrestus.consensus.ChangeView.ChangeViewCommitteeState;
import io.Adrestus.consensus.ChangeView.ChangeViewTransactionState;
import io.Adrestus.core.*;
import io.Adrestus.core.Resourses.*;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

public class ConsensusState extends ConsensusDataState {
    private static Logger LOG = LoggerFactory.getLogger(ConsensusState.class);
    private final DefaultFactory factory;
    private static IBlockSync blockSync;
    private static Timer transaction_block_timer;
    private static Timer committee_block_timer;
    private static CountDownLatch latch;
    private static IBlockIndex blockIndex;

    private static boolean debug;

    public ConsensusState() {
        this.transaction_block_timer = new Timer(ConsensusConfiguration.CONSENSUS);
        this.committee_block_timer = new Timer(ConsensusConfiguration.CONSENSUS);
        this.factory = new DefaultFactory(new TransactionBlock(), new CommitteeBlock());
        this.blockSync = new BlockSync();
    }

    public ConsensusState(CountDownLatch latch) {
        this.blockIndex = new BlockIndex();
        this.transaction_block_timer = new Timer(ConsensusConfiguration.CONSENSUS);
        this.committee_block_timer = new Timer(ConsensusConfiguration.CONSENSUS);
        this.latch = latch;
        this.factory = new DefaultFactory(new TransactionBlock(), new CommitteeBlock());
        this.blockSync = new BlockSync();
    }

    public ConsensusState(CountDownLatch latch, boolean debug) {
        this.debug = debug;
        this.blockIndex = new BlockIndex();
        this.transaction_block_timer = new Timer(ConsensusConfiguration.CONSENSUS);
        this.committee_block_timer = new Timer(ConsensusConfiguration.CONSENSUS);
        this.latch = latch;
        this.factory = new DefaultFactory(new TransactionBlock(), new CommitteeBlock());
        this.blockSync = new BlockSync();
    }

    public static Logger getLOG() {
        return LOG;
    }

    public static void setLOG(Logger LOG) {
        ConsensusState.LOG = LOG;
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
            previous_state = committee_state;
            CachedConsensusState.getInstance().setCommitteeState(committee_state);
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
            CachedConsensusState.getInstance().setCommitteeState(committee_state);
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
            clear();
            try {
                boolean result = false;
                boolean finaly = false;
                while (!result || !finaly) {
                    result = committee_state.onActiveState();
                    if (result && committee_state.getClass().equals(ConsensusCommitteeBlockState.class)) {
                        finaly = true;
                    }
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
                        /*} else if (committee_state.getClass().equals(ConsensusCommitteeBlockState.class)) {
                            CachedLeaderIndex.getInstance().setCommitteePositionLeader(0);
                            changeStateTo(new ConsensusVRFState());
                            committee_state.onEnterState(blockIndex.getPublicKeyByIndex(0, CachedLeaderIndex.getInstance().getCommitteePositionLeader()));
                        }*/
                        } else if (committee_state.getClass().equals(ConsensusVRFState.class)) {
                            changeStateTo(new ConsensusVDFState());
                            committee_state.onEnterState(blockIndex.getPublicKeyByIndex(0, CachedLeaderIndex.getInstance().getCommitteePositionLeader()));
                        } else if (committee_state.getClass().equals(ConsensusVDFState.class)) {
                            changeStateTo(new ConsensusCommitteeBlockState());
                            committee_state.onEnterState(blockIndex.getPublicKeyByIndex(0, CachedLeaderIndex.getInstance().getCommitteePositionLeader()));
                        } else if (committee_state.getClass().equals(ConsensusCommitteeBlockState.class)) {
                            changeStateTo(new ConsensusVRFState());
                            CachedLeaderIndex.getInstance().setCommitteePositionLeader(0);
                            CachedEpochGeneration.getInstance().setEpoch_counter(0);
                            committee_state.onEnterState(blockIndex.getPublicKeyByIndex(0, CachedLeaderIndex.getInstance().getCommitteePositionLeader()));
                            clear();
                            blockSync.SyncBeaconChainState();
                            transaction_block_timer = new Timer(ConsensusConfiguration.CONSENSUS);
                            transaction_block_timer.scheduleAtFixedRate(new TransactionBlockConsensusTask(), ConsensusConfiguration.CONSENSUS_TIMER, ConsensusConfiguration.CONSENSUS_TIMER);
                        }
                    }
                }
            } finally {
                latch.countDown();
                LOG.info("Latch Countdown: " + latch.getCount());
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

    public static final class TransactionBlockConsensusTask extends TimerTask {
        private AbstractState state;

        public TransactionBlockConsensusTask() {
            this.state = new ConsensusTransactionBlockState();
            this.state.onEnterState(null);
            CachedConsensusState.getInstance().setTransactionState(state);
        }

        public TransactionBlockConsensusTask(AbstractState state) {
            this.state = state;
            CachedConsensusState.getInstance().setTransactionState(state);
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
            clear();
            if (CachedEpochGeneration.getInstance().getEpoch_counter() >= ConsensusConfiguration.EPOCH_TRANSITION) {
                if (CachedZoneIndex.getInstance().getZoneIndex() == 0) {
                    clear();
                    committee_block_timer = new Timer(ConsensusConfiguration.CONSENSUS);
                    committee_block_timer.scheduleAtFixedRate(new CommitteeBlockConsensusTask(), ConsensusConfiguration.CONSENSUS_COMMITTEE_TIMER, ConsensusConfiguration.CONSENSUS_COMMITTEE_TIMER);
                    return;
                } else {
                    if (!debug) {
                        blockSync.SyncState();
                    }
                }
            }
            state.onEnterState(blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), CachedLeaderIndex.getInstance().getTransactionPositionLeader()));
            boolean result = state.onActiveState();
            if (result) {
                latch.countDown();
                LOG.info("Latch Countdown: " + latch.getCount());
                if (state.getClass().equals(ConsensusTransactionBlockState.class)) {
                    state.onEnterState(null);
                } else {
                    LOG.info("State changed to  ConsensusTransactionBlockState");
                    changeStateTo(new ConsensusTransactionBlockState());
                    state.onEnterState(null);
                }
                CachedCheckPoint.getInstance().setCheckPointCounter(0);
                CachedEpochGeneration.getInstance().setEpoch_counter(CachedEpochGeneration.getInstance().getEpoch_counter() + 1);
                transaction_block_timer = new Timer(ConsensusConfiguration.CONSENSUS);
                transaction_block_timer.scheduleAtFixedRate(new TransactionBlockConsensusTask(state), ConsensusConfiguration.CONSENSUS_TIMER, ConsensusConfiguration.CONSENSUS_TIMER);
            } else {
                changeStateTo(new ChangeViewTransactionState());
                LOG.info("State changed to ChangeViewTransactionState");
                CachedCheckPoint.getInstance().setCheckPointCounter(CachedCheckPoint.getInstance().getCheckPointCounter() + 1);
                if (CachedCheckPoint.getInstance().getCheckPointCounter() == ConsensusConfiguration.CHANGE_VIEW_STATE_TRANSITION) {
                    LOG.info("CHANGE_VIEW_STATE_TRANSITION)");
                    blockSync.checkIfNeedsSync();
                }
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

}
