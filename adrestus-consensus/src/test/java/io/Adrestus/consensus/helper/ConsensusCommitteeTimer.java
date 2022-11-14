package io.Adrestus.consensus.helper;

import io.Adrestus.config.ConsensusConfiguration;
import io.Adrestus.consensus.ConsensusManager;
import io.Adrestus.consensus.ConsensusMessage;
import io.Adrestus.consensus.ConsensusRoleType;
import io.Adrestus.consensus.ConsensusType;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedLeaderIndex;
import io.Adrestus.core.Resourses.MemoryPool;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.util.GetTime;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

public class ConsensusCommitteeTimer {
    private static Logger LOG = LoggerFactory.getLogger(ConsensusCommitteeTimer.class);
    private final ConsensusTask task;
    private final CountDownLatch latch;
    private final ConsensusManager consensusManager;
    private Timer timer;

    public ConsensusCommitteeTimer(CountDownLatch latch) {
        this.consensusManager = new ConsensusManager(false);
        this.timer = new Timer(ConsensusConfiguration.CONSENSUS);
        this.task = new ConsensusTask();
        this.latch = latch;
        this.timer.scheduleAtFixedRate(task, ConsensusConfiguration.CONSENSUS_COMMITTEE_TIMER, ConsensusConfiguration.CONSENSUS_COMMITTEE_TIMER);
    }

    public void InitFirstBlock() throws InterruptedException {
        IDatabase<String, CommitteeBlock> database = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB);
        CachedLatestBlocks.getInstance().getCommitteeBlock().getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        CachedLatestBlocks.getInstance().getCommitteeBlock().setDifficulty(112);
        CachedLatestBlocks.getInstance().getCommitteeBlock().setHash("hash");
        CachedLatestBlocks.getInstance().getCommitteeBlock().setGeneration(0);
        CachedLatestBlocks.getInstance().getCommitteeBlock().setHeight(0);
        database.save(CachedLatestBlocks.getInstance().getCommitteeBlock().getHash(),CachedLatestBlocks.getInstance().getCommitteeBlock());
    }
    public void close(){
        timer.cancel();
        task.cancel();
    }
    protected final class ConsensusTask extends TimerTask {

        @SneakyThrows
        @Override
        public void run() {
            timer.cancel();
            ConsensusMessage<CommitteeBlock> consensusMessage = new ConsensusMessage<>(new CommitteeBlock());
            int index = CachedLatestBlocks.getInstance().getCommitteeBlock().getPublicKeyIndex(0, CachedBLSKeyPair.getInstance().getPublicKey());

            CachedLeaderIndex.getInstance().setCommitteePositionLeader(0);
            if (index==0) {
                LOG.info("ORGANIZER State");
                consensusManager.changeStateTo(ConsensusRoleType.SUPERVISOR);
                var organizerphase = consensusManager.getRole().manufacturePhases(ConsensusType.COMMITTEE_BLOCK);
                organizerphase.AnnouncePhase(consensusMessage);
                organizerphase.PreparePhase(consensusMessage);
                organizerphase.CommitPhase(consensusMessage);
            } else {
                LOG.info("VALIDATOR State");
                consensusManager.changeStateTo(ConsensusRoleType.VALIDATOR);
                var validatorphase = consensusManager.getRole().manufacturePhases(ConsensusType.COMMITTEE_BLOCK);
                validatorphase.AnnouncePhase(consensusMessage);
                validatorphase.PreparePhase(consensusMessage);
                validatorphase.CommitPhase(consensusMessage);
            }
            latch.countDown();

            timer = new Timer(ConsensusConfiguration.CONSENSUS);
            timer.scheduleAtFixedRate(this, ConsensusConfiguration.CONSENSUS_TIMER, ConsensusConfiguration.CONSENSUS_TIMER);
        }
    }
}
