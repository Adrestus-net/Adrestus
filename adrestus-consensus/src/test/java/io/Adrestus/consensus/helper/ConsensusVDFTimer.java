package io.Adrestus.consensus.helper;

import io.Adrestus.config.ConsensusConfiguration;
import io.Adrestus.consensus.ConsensusManager;
import io.Adrestus.consensus.ConsensusMessage;
import io.Adrestus.consensus.ConsensusRoleType;
import io.Adrestus.consensus.ConsensusType;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedLeaderIndex;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.vdf.VDFMessage;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

public class ConsensusVDFTimer {
    private static Logger LOG = LoggerFactory.getLogger(ConsensusVDFTimer.class);
    private final ConsensusTask task;
    private final CountDownLatch latch;
    private final ConsensusManager consensusManager;
    private Timer timer;

    public ConsensusVDFTimer(CountDownLatch latch) {
        this.consensusManager = new ConsensusManager(false);
        this.timer = new Timer(ConsensusConfiguration.CONSENSUS);
        this.task = new ConsensusTask();
        this.latch = latch;
        this.timer.scheduleAtFixedRate(task, ConsensusConfiguration.CONSENSUS_COMMITTEE_TIMER, ConsensusConfiguration.CONSENSUS_COMMITTEE_TIMER);
    }

    protected final class ConsensusTask extends TimerTask {

        @SneakyThrows
        @Override
        public void run() {
            ConsensusMessage<VDFMessage> consensusMessage = new ConsensusMessage<>(new VDFMessage());
            int index = CachedLatestBlocks.getInstance().getCommitteeBlock().getPublicKeyIndex(0, CachedBLSKeyPair.getInstance().getPublicKey());

            CachedLeaderIndex.getInstance().setCommitteePositionLeader(0);
            if (index == 0) {
                LOG.info("ORGANIZER State");
                consensusManager.changeStateTo(ConsensusRoleType.SUPERVISOR);
                var organizerphase = consensusManager.getRole().manufacturePhases(ConsensusType.VDF);
                organizerphase.InitialSetup();
                organizerphase.AnnouncePhase(consensusMessage);
                organizerphase.PreparePhase(consensusMessage);
                organizerphase.CommitPhase(consensusMessage);
            } else {
                LOG.info("VALIDATOR State");
                consensusManager.changeStateTo(ConsensusRoleType.VALIDATOR);
                var validatorphase = consensusManager.getRole().manufacturePhases(ConsensusType.VDF);
                validatorphase.InitialSetup();
                validatorphase.AnnouncePhase(consensusMessage);
                validatorphase.PreparePhase(consensusMessage);
                validatorphase.CommitPhase(consensusMessage);
            }
            latch.countDown();
        }
    }
}
