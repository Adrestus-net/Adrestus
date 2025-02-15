package io.Adrestus.consensus.helper;

import io.Adrestus.config.ConsensusConfiguration;
import io.Adrestus.consensus.*;
import io.Adrestus.core.BlockIndex;
import io.Adrestus.core.IBlockIndex;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedLeaderIndex;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.vrf.VRFMessage;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

public class ConsensusVRFTimer {
    private static Logger LOG = LoggerFactory.getLogger(ConsensusVRFTimer.class);
    private final ConsensusTask task;
    private final CountDownLatch latch;
    private final ConsensusManager consensusManager;
    private final IBlockIndex blockIndex;
    private Timer timer;
    private int index = 1;

    public ConsensusVRFTimer(CountDownLatch latch) {
        this.latch = latch;
        this.blockIndex = new BlockIndex();
        this.consensusManager = new ConsensusManager();
        this.timer = new Timer(ConsensusConfiguration.CONSENSUS);
        this.task = new ConsensusTask();
        this.timer.scheduleAtFixedRate(task, ConsensusConfiguration.CONSENSUS_COMMITTEE_TIMER, ConsensusConfiguration.CONSENSUS_COMMITTEE_TIMER);
    }

    public Timer getTimer() {
        return timer;
    }

    public ConsensusTask getTask() {
        return task;
    }

    protected final class ConsensusTask extends TimerTask {

        @SneakyThrows
        @Override
        public void run() {
            timer.cancel();
            index++;
            CachedLatestBlocks.getInstance().getCommitteeBlock().setViewID(index);
            CachedLatestBlocks.getInstance().getCommitteeBlock().setHash(String.valueOf(index));
            VRFMessage vrfMessage = new VRFMessage();
            ConsensusMessage<VRFMessage> consensusMessage = new ConsensusMessage<>(vrfMessage);
            int index = blockIndex.getPublicKeyIndex(0, CachedBLSKeyPair.getInstance().getPublicKey());

            CachedLeaderIndex.getInstance().setCommitteePositionLeader(0);
            if (index == 0) {
                LOG.info("ORGANIZER State");
                consensusManager.changeStateTo(ConsensusRoleType.SUPERVISOR);
                var supervisorphase = (VRFConsensusPhase) consensusManager.getRole().manufacturePhases(ConsensusType.VRF);
                supervisorphase.InitialSetup();
                supervisorphase.Initialize(vrfMessage);
                supervisorphase.AggregateVRF(vrfMessage);
                supervisorphase.AnnouncePhase(consensusMessage);
                supervisorphase.PreparePhase(consensusMessage);
                supervisorphase.CommitPhase(consensusMessage);
                if (consensusMessage.getStatusType().equals(ConsensusStatusType.ABORT))
                    throw new IllegalArgumentException("Problem occured");
            } else {
                LOG.info("VALIDATOR State");
                consensusManager.changeStateTo(ConsensusRoleType.VALIDATOR);
                var validatorphase = (VRFConsensusPhase) consensusManager.getRole().manufacturePhases(ConsensusType.VRF);
                validatorphase.InitialSetup();
                validatorphase.Initialize(vrfMessage);
                validatorphase.AnnouncePhase(consensusMessage);
                validatorphase.PreparePhase(consensusMessage);
                validatorphase.CommitPhase(consensusMessage);
                if (consensusMessage.getStatusType().equals(ConsensusStatusType.ABORT))
                    throw new IllegalArgumentException("Problem occured");
            }
            latch.countDown();
            timer = new Timer(ConsensusConfiguration.CONSENSUS);
            timer.scheduleAtFixedRate(new ConsensusTask(), ConsensusConfiguration.CONSENSUS_TIMER, ConsensusConfiguration.CONSENSUS_TIMER);
        }
    }

    public void close() {
        timer.cancel();
        task.cancel();
    }
}
