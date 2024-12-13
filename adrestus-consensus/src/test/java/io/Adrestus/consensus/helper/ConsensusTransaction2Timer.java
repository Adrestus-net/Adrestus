package io.Adrestus.consensus.helper;

import io.Adrestus.config.ConsensusConfiguration;
import io.Adrestus.consensus.*;
import io.Adrestus.core.*;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedLeaderIndex;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.Resourses.MemoryTransactionPool;
import io.Adrestus.core.RingBuffer.handler.transactions.SignatureEventHandler;
import io.Adrestus.core.RingBuffer.publisher.TransactionEventPublisher;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.crypto.elliptic.ECDSASignatureData;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.mapper.BigDecimalSerializer;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class ConsensusTransaction2Timer {
    private static Logger LOG = LoggerFactory.getLogger(ConsensusTransactionTimer.class);
    private static SerializationUtil<Transaction> serenc;
    private static ECDSASign ecdsaSign = new ECDSASign();
    private Timer timer;
    private final ConsensusTask task;
    private final CountDownLatch latch;
    private final ConsensusManager consensusManager;
    public final ArrayList<String> addreses;
    private final ArrayList<ECKeyPair> keypair;
    private final IBlockIndex blockIndex;
    private int nonce = 0;
    private Random r = new Random();
    private int low = 0;
    private int high = 3;

    public ConsensusTransaction2Timer(CountDownLatch latch, ArrayList<String> addreses, ArrayList<ECKeyPair> keypair) {
        this.addreses = addreses;
        this.blockIndex = new BlockIndex();
        this.keypair = keypair;
        this.consensusManager = new ConsensusManager(false);
        this.timer = new Timer(ConsensusConfiguration.CONSENSUS);
        this.task = new ConsensusTask();
        this.latch = latch;
        this.timer.scheduleAtFixedRate(task, ConsensusConfiguration.CONSENSUS_TIMER, ConsensusConfiguration.CONSENSUS_TIMER);
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        this.serenc = new SerializationUtil<Transaction>(Transaction.class, list);
    }


    public Timer getTimer() {
        return timer;
    }

    public ConsensusTask getTask() {
        return task;
    }

    private void chooser() throws InterruptedException {
        int activeZone = this.blockIndex.getZone(CachedBLSKeyPair.getInstance().getPublicKey());
        ArrayList<BLSPublicKey> keyList = new ArrayList<BLSPublicKey>(CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(activeZone).keySet());
        if (activeZone == 0) {
            if (CachedBLSKeyPair.getInstance().getPublicKey().equals(keyList.get(0))) {
                SaveTransactions(6, 6);
            } else if (CachedBLSKeyPair.getInstance().getPublicKey().equals(keyList.get(1))) {
                SaveTransactions(7, 7);
            } else if (CachedBLSKeyPair.getInstance().getPublicKey().equals(keyList.get(2))) {
                SaveTransactions(8, 8);
            }
        } else if (activeZone == 1) {
            if (CachedBLSKeyPair.getInstance().getPublicKey().equals(keyList.get(0))) {
                SaveTransactions(0, 1);
            } else if (CachedBLSKeyPair.getInstance().getPublicKey().equals(keyList.get(1))) {
                SaveTransactions(2, 3);
            } else if (CachedBLSKeyPair.getInstance().getPublicKey().equals(keyList.get(2))) {
                SaveTransactions(4, 5);
            }
        }
       /* if (CachedBLSKeyPair.getInstance().getPublicKey().equals(keyList.get(0))) {
            SaveTransactions(0, 1);
        } else if (CachedBLSKeyPair.getInstance().getPublicKey().equals(keyList.get(1))) {
            SaveTransactions(2, 3);
        } else if (CachedBLSKeyPair.getInstance().getPublicKey().equals(keyList.get(2))) {
            SaveTransactions(4, 5);
        } else if (CachedBLSKeyPair.getInstance().getPublicKey().equals(keyList.get(3))) {
            SaveTransactions2(6, 7);
        } else if (CachedBLSKeyPair.getInstance().getPublicKey().equals(keyList.get(4))) {
            SaveTransactions2(8, 8);
        } else if (CachedBLSKeyPair.getInstance().getPublicKey().equals(keyList.get(5))) {
            SaveTransactions2(9, 9);
        }*/

    }

    private void SaveTransactions(int start, int stop) throws InterruptedException {
        nonce++;
        for (int i = start; i <= stop; i++) {
            Transaction transaction = new RegularTransaction();
            transaction.setFrom(addreses.get(i));
            transaction.setTo(addreses.get(i + 1));
            transaction.setStatus(StatusType.PENDING);
            transaction.setTimestamp(GetTime.GetTimeStampInString());
            transaction.setZoneFrom(CachedZoneIndex.getInstance().getZoneIndex());
            transaction.setZoneTo(0);
            transaction.setAmount(BigDecimal.valueOf(i + 10));
            transaction.setAmountWithTransactionFee(transaction.getAmount().multiply(BigDecimal.valueOf(10.0 / 100.0)));
            transaction.setNonce(nonce);
            byte byf[] = serenc.encode(transaction, 1024);
            transaction.setHash(HashUtil.sha256_bytetoString(byf));

            ECDSASignatureData signatureData = ecdsaSign.signSecp256r1Message(transaction.getHash().getBytes(StandardCharsets.UTF_8), keypair.get(i));
            transaction.setSignature(signatureData);
            MemoryTransactionPool.getInstance().add(transaction);
            Thread.sleep(100);
        }
    }

    private void SaveTransactions2(int start, int stop) throws InterruptedException {
        nonce++;
        TransactionEventPublisher publisher = new TransactionEventPublisher(1024);
        SignatureEventHandler signatureEventHandler = new SignatureEventHandler(SignatureEventHandler.SignatureBehaviorType.SIMPLE_TRANSACTIONS, new CountDownLatch(stop + 1));
        publisher
                .withAddressSizeEventHandler()
                .withAmountEventHandler()
                .withTypeEventHandler()
                .withDoubleSpendEventHandler()
                .withHashEventHandler()
                .withDelegateEventHandler()
                .withNonceEventHandler()
                .withReplayEventHandler()
                .withStakingEventHandler()
                .withTransactionFeeEventHandler()
                .withTimestampEventHandler()
                .withSameOriginEventHandler()
                .withZoneEventHandler()
                .withDuplicateEventHandler()
                .withMinimumStakingEventHandler()
                .mergeEventsAndPassThen(signatureEventHandler);
        publisher.start();

        for (int i = start; i <= stop; i++) {
            Transaction transaction = new RegularTransaction();
            transaction.setFrom(addreses.get(i));
            transaction.setTo(addreses.get(i + 1));
            transaction.setStatus(StatusType.PENDING);
            transaction.setTimestamp(GetTime.GetTimeStampInString());
            Thread.sleep(10);
            transaction.setZoneFrom(CachedZoneIndex.getInstance().getZoneIndex());
            transaction.setZoneTo(CachedZoneIndex.getInstance().getZoneIndex());
            transaction.setAmount(BigDecimal.valueOf(i + 10));
            transaction.setAmountWithTransactionFee(transaction.getAmount().multiply(BigDecimal.valueOf(10.0 / 100.0)));
            transaction.setNonce(nonce);
            byte byf[] = serenc.encode(transaction, 1024);
            transaction.setHash(HashUtil.sha256_bytetoString(byf));

            ECDSASignatureData signatureData = ecdsaSign.signSecp256r1Message(transaction.getHash().getBytes(StandardCharsets.UTF_8), keypair.get(i));
            transaction.setSignature(signatureData);
            //MemoryPool.getInstance().add(transaction);
            publisher.publish(transaction);
            Thread.sleep(100);
        }
        publisher.getJobSyncUntilRemainingCapacityZero();
        signatureEventHandler.getLatch().await();
        publisher.close();
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
            CachedLeaderIndex.getInstance().setTransactionPositionLeader(current);
            if (target == current) {
                LOG.info("ORGANIZER State");
                chooser();
                consensusManager.changeStateTo(ConsensusRoleType.ORGANIZER);
                var organizerphase = consensusManager.getRole().manufacturePhases(ConsensusType.TRANSACTION_BLOCK);
                organizerphase.InitialSetup();
                organizerphase.DispersePhase(consensusMessage);
                organizerphase.AnnouncePhase(consensusMessage);
                organizerphase.PreparePhase(consensusMessage);
                organizerphase.CommitPhase(consensusMessage);
                if (consensusMessage.getStatusType().equals(ConsensusStatusType.ABORT))
                    throw new IllegalArgumentException("Problem occured");
            } else {
                LOG.info("VALIDATOR State");
                consensusManager.changeStateTo(ConsensusRoleType.VALIDATOR);
                var validatorphase = consensusManager.getRole().manufacturePhases(ConsensusType.TRANSACTION_BLOCK);
                validatorphase.InitialSetup();
                validatorphase.DispersePhase(consensusMessage);
                validatorphase.AnnouncePhase(consensusMessage);
                validatorphase.PreparePhase(consensusMessage);
                validatorphase.CommitPhase(consensusMessage);
                if (consensusMessage.getStatusType().equals(ConsensusStatusType.ABORT))
                    throw new IllegalArgumentException("Problem occured");
            }
            latch.countDown();
            MemoryTransactionPool.getInstance().clear();
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
