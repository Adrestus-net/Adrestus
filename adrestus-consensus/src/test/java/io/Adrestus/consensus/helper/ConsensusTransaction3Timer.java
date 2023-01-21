package io.Adrestus.consensus.helper;

import io.Adrestus.config.ConsensusConfiguration;
import io.Adrestus.consensus.ConsensusManager;
import io.Adrestus.consensus.ConsensusMessage;
import io.Adrestus.consensus.ConsensusRoleType;
import io.Adrestus.consensus.ConsensusType;
import io.Adrestus.core.*;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.Resourses.MemoryTransactionPool;
import io.Adrestus.core.RingBuffer.handler.transactions.SignatureEventHandler;
import io.Adrestus.core.RingBuffer.publisher.TransactionEventPublisher;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.SignatureData;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

public class ConsensusTransaction3Timer {
    private static Logger LOG = LoggerFactory.getLogger(ConsensusTransaction3Timer.class);
    private static SerializationUtil<Transaction> serenc = new SerializationUtil<Transaction>(Transaction.class);
    private Timer timer;
    private final ConsensusTask task;
    private final CountDownLatch latch;
    private final ConsensusManager consensusManager;
    private final IBlockIndex blockIndex;
    private int nonce = 0;

    public ConsensusTransaction3Timer(CountDownLatch latch) {
        this.blockIndex = new BlockIndex();
        this.consensusManager = new ConsensusManager(false);
        this.timer = new Timer(ConsensusConfiguration.CONSENSUS);
        this.task = new ConsensusTask();
        this.latch = latch;
        this.timer.scheduleAtFixedRate(task, ConsensusConfiguration.CONSENSUS_TIMER, ConsensusConfiguration.CONSENSUS_TIMER);
    }


    public Timer getTimer() {
        return timer;
    }

    public ConsensusTask getTask() {
        return task;
    }


    public void close() {
        timer.cancel();
        task.cancel();
    }

    protected final class ConsensusTask extends TimerTask {


        @SneakyThrows
        @Override
        public void run() {
            timer.cancel();
            ConsensusMessage<TransactionBlock> consensusMessage = new ConsensusMessage<>(new TransactionBlock());
            int target = blockIndex.getPublicKeyIndex(CachedZoneIndex.getInstance().getZoneIndex(), CachedBLSKeyPair.getInstance().getPublicKey());
            int current = blockIndex.getPublicKeyIndex(CachedZoneIndex.getInstance().getZoneIndex(), CachedLatestBlocks.getInstance().getTransactionBlock().getLeaderPublicKey());

            if (target == current + 1 || (target == 0 && current == CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).size() - 1)) {
                LOG.info("ORGANIZER State");
                consensusManager.changeStateTo(ConsensusRoleType.ORGANIZER);
                var organizerphase = consensusManager.getRole().manufacturePhases(ConsensusType.TRANSACTION_BLOCK);
                organizerphase.InitialSetup();
                organizerphase.AnnouncePhase(consensusMessage);
                organizerphase.PreparePhase(consensusMessage);
                organizerphase.CommitPhase(consensusMessage);
            } else {
                LOG.info("VALIDATOR State");
                consensusManager.changeStateTo(ConsensusRoleType.VALIDATOR);
                var validatorphase = consensusManager.getRole().manufacturePhases(ConsensusType.TRANSACTION_BLOCK);
                validatorphase.InitialSetup();
                validatorphase.AnnouncePhase(consensusMessage);
                validatorphase.PreparePhase(consensusMessage);
                validatorphase.CommitPhase(consensusMessage);
            }
            latch.countDown();
            MemoryTransactionPool.getInstance().clear();
            timer = new Timer(ConsensusConfiguration.CONSENSUS);
            timer.scheduleAtFixedRate(new ConsensusTask(), ConsensusConfiguration.CONSENSUS_TIMER, ConsensusConfiguration.CONSENSUS_TIMER);
        }

        @Override
        public boolean cancel() {
            super.cancel();
            return true;
        }
    }
}
