package io.Adrestus.consensus.helper;

import io.Adrestus.config.ConsensusConfiguration;
import io.Adrestus.consensus.ConsensusManager;
import io.Adrestus.consensus.ConsensusMessage;
import io.Adrestus.consensus.ConsensusRoleType;
import io.Adrestus.consensus.ConsensusType;
import io.Adrestus.core.RegularTransaction;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.MemoryPool;
import io.Adrestus.core.RingBuffer.handler.transactions.SignatureEventHandler;
import io.Adrestus.core.RingBuffer.publisher.TransactionEventPublisher;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.Transaction;
import io.Adrestus.core.TransactionBlock;
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

public class ConsensusTransactionTimer {
    private static Logger LOG = LoggerFactory.getLogger(ConsensusTransactionTimer.class);
    private static SerializationUtil<Transaction> serenc = new SerializationUtil<Transaction>(Transaction.class);
    private static ECDSASign ecdsaSign = new ECDSASign();
    private Timer timer;
    private final ConsensusTask task;
    private final CountDownLatch latch;
    private final ConsensusManager consensusManager;
    public final ArrayList<String> addreses;
    private final ArrayList<ECKeyPair> keypair;

    public ConsensusTransactionTimer(CountDownLatch latch, ArrayList<String> addreses, ArrayList<ECKeyPair> keypair) {
        this.addreses = addreses;
        this.keypair = keypair;
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

    private void chooser() throws InterruptedException {
        ArrayList<BLSPublicKey> keyList = new ArrayList<BLSPublicKey>(CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).keySet());
        if (CachedBLSKeyPair.getInstance().getPublicKey().equals(keyList.get(0))) {
            SaveTransactions(0, 1);
        } else if (CachedBLSKeyPair.getInstance().getPublicKey().equals(keyList.get(1))) {
            SaveTransactions(2, 3);
        } else if (CachedBLSKeyPair.getInstance().getPublicKey().equals(keyList.get(2))) {
            SaveTransactions(4, 5);
        } else if (CachedBLSKeyPair.getInstance().getPublicKey().equals(keyList.get(3))) {
            SaveTransactions(6, 7);
        } else if (CachedBLSKeyPair.getInstance().getPublicKey().equals(keyList.get(4))) {
            SaveTransactions(8, 8);
        } else if (CachedBLSKeyPair.getInstance().getPublicKey().equals(keyList.get(5))) {
            SaveTransactions(9, 9);
        }

    }

    private void SaveTransactions(int start, int stop) throws InterruptedException {
        TransactionEventPublisher publisher = new TransactionEventPublisher(1024);

        publisher
                .withAddressSizeEventHandler()
                .withAmountEventHandler()
                .withDelegateEventHandler()
                .withDoubleSpendEventHandler()
                .withHashEventHandler()
                .withNonceEventHandler()
                .withReplayEventHandler()
                .withRewardEventHandler()
                .withStakingEventHandler()
                .withTransactionFeeEventHandler()
                .withTimestampEventHandler()
                .mergeEventsAndPassThen(new SignatureEventHandler(SignatureEventHandler.SignatureBehaviorType.SIMPLE_TRANSACTIONS));
        publisher.start();

        for (int i = start; i <= stop; i++) {
            Transaction transaction = new RegularTransaction();
            transaction.setFrom(addreses.get(i));
            transaction.setTo(addreses.get(i + 1));
            transaction.setStatus(StatusType.PENDING);
            transaction.setTimestamp(GetTime.GetTimeStampInString());
            Thread.sleep(10);
            transaction.setZoneFrom(0);
            transaction.setZoneTo(0);
            transaction.setAmount(100);
            transaction.setAmountWithTransactionFee(transaction.getAmount() * (10.0 / 100.0));
            transaction.setNonce(1);
            byte byf[] = serenc.encode(transaction);
            transaction.setHash(HashUtil.sha256_bytetoString(byf));

            SignatureData signatureData = ecdsaSign.secp256SignMessage(Hex.decode(transaction.getHash()), keypair.get(i));
            transaction.setSignature(signatureData);
            //MemoryPool.getInstance().add(transaction);
            publisher.publish(transaction);
            Thread.sleep(100);
        }
        publisher.getJobSyncUntilRemainingCapacityZero();
        publisher.close();
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
            ConsensusMessage<TransactionBlock> consensusMessage = new ConsensusMessage<>(new TransactionBlock());
            int target = CachedLatestBlocks.getInstance().getCommitteeBlock().getPublicKeyIndex(1, CachedBLSKeyPair.getInstance().getPublicKey());
            int current = CachedLatestBlocks.getInstance().getCommitteeBlock().getPublicKeyIndex(1, CachedLatestBlocks.getInstance().getTransactionBlock().getLeaderPublicKey());

            if (target == current + 1 || (target == 0 && current == CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).size() - 1)) {
                LOG.info("ORGANIZER State");
                chooser();
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
            MemoryPool.getInstance().clear();
            timer = new Timer(ConsensusConfiguration.CONSENSUS);
            timer.scheduleAtFixedRate(new ConsensusTask(), ConsensusConfiguration.CONSENSUS_TIMER, ConsensusConfiguration.CONSENSUS_TIMER);
        }
   /*     @SneakyThrows
        @Override
        public void run() {
            timer.cancel();
            ConsensusMessage<TransactionBlock> consensusMessage = new ConsensusMessage<>(new TransactionBlock());
            if (CachedLatestBlocks.getInstance().getTransactionBlock().getTransactionProposer().equals(CachedBLSKeyPair.getInstance().getPublicKey().toRaw())) {
                consensusManager.changeStateTo(ConsensusRoleType.ORGANIZER);
                var organizerphase = consensusManager.getRole().manufacturePhases(ConsensusType.TRANSACTION_BLOCK);
                organizerphase.AnnouncePhase(consensusMessage);
                organizerphase.PreparePhase(consensusMessage);
                organizerphase.CommitPhase(consensusMessage);
            } else {
                consensusManager.changeStateTo(ConsensusRoleType.VALIDATOR);
                var validatorphase = consensusManager.getRole().manufacturePhases(ConsensusType.TRANSACTION_BLOCK);
                validatorphase.AnnouncePhase(consensusMessage);
                validatorphase.PreparePhase(consensusMessage);
                validatorphase.CommitPhase(consensusMessage);
            }

            ArrayList<BLSPublicKey> copy = new ArrayList<>(CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).keySet());
            for (int i = 0; i < copy.size(); i++) {
                if (CachedLatestBlocks.getInstance().getTransactionBlock().getTransactionProposer().equals(copy.get(i).toRaw())) {
                    if (i == copy.size()-1)
                        CachedLatestBlocks.getInstance().getTransactionBlock().setTransactionProposer(copy.get(0).toRaw());
                    else
                        CachedLatestBlocks.getInstance().getTransactionBlock().setTransactionProposer(copy.get(i + 1).toRaw());
                    break;
                }
            }
            latch.countDown();
            timer = new Timer(ConsensusConfiguration.CONSENSUS);
            timer.scheduleAtFixedRate(new ConsensusTask(), ConsensusConfiguration.CONSENSUS_TIMER, ConsensusConfiguration.CONSENSUS_TIMER);
        }*/

        @Override
        public boolean cancel() {
            super.cancel();
            return true;
        }
    }

}