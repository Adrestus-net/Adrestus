package io.Adrestus.consensus.helper;

import io.Adrestus.config.ConsensusConfiguration;
import io.Adrestus.consensus.ConsensusManager;
import io.Adrestus.consensus.ConsensusMessage;
import io.Adrestus.consensus.ConsensusRoleType;
import io.Adrestus.consensus.ConsensusType;
import io.Adrestus.core.BlockIndex;
import io.Adrestus.core.IBlockIndex;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedLeaderIndex;
import io.Adrestus.core.Resourses.CachedSecurityHeaders;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.vdf.VDFMessage;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

public class ConsensusVDFTimer {
    private static Logger LOG = LoggerFactory.getLogger(ConsensusVDFTimer.class);
    private final ConsensusTask task;
    private final CountDownLatch latch;
    private final ConsensusManager consensusManager;
    private final IBlockIndex blockIndex;
    private Timer timer;
    private byte[] challenge;
    private Random random;
    private int SeedCounter = 200;
    private int index = 1;

    public ConsensusVDFTimer(CountDownLatch latch) {
        this.consensusManager = new ConsensusManager();
        this.timer = new Timer(ConsensusConfiguration.CONSENSUS);
        this.blockIndex = new BlockIndex();
        this.task = new ConsensusTask();
        this.latch = latch;
        this.timer.scheduleAtFixedRate(task, ConsensusConfiguration.CONSENSUS_COMMITTEE_TIMER, ConsensusConfiguration.CONSENSUS_COMMITTEE_TIMER);
        this.challenge = new byte[20];
        this.random = new Random();
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
            random.setSeed(SeedCounter++);
            random.nextBytes(challenge);
            CachedSecurityHeaders.getInstance().getSecurityHeader().setPRnd(challenge);
            index++;
            CachedLatestBlocks.getInstance().getCommitteeBlock().setViewID(index);
            CachedLatestBlocks.getInstance().getCommitteeBlock().setHash(String.valueOf(index));
            ConsensusMessage<VDFMessage> consensusMessage = new ConsensusMessage<>(new VDFMessage());
            int index = blockIndex.getPublicKeyIndex(0, CachedBLSKeyPair.getInstance().getPublicKey());

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
            timer = new Timer(ConsensusConfiguration.CONSENSUS);
            timer.scheduleAtFixedRate(new ConsensusTask(), ConsensusConfiguration.CONSENSUS_TIMER, ConsensusConfiguration.CONSENSUS_TIMER);
        }

        public void close() {
            timer.cancel();
            task.cancel();
        }
    }
}
