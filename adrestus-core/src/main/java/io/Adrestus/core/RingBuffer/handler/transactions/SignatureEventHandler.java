package io.Adrestus.core.RingBuffer.handler.transactions;

import io.Adrestus.core.*;
import io.Adrestus.core.Resourses.CacheTemporalTransactionPool;
import io.Adrestus.core.Resourses.MemoryTransactionPool;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.elliptic.ECDSASign;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public class SignatureEventHandler extends TransactionEventHandler implements TransactionUnitVisitor {
    private static Logger LOG = LoggerFactory.getLogger(SignatureEventHandler.class);

    private final ECDSASign ecdsaSign;
    private final SignatureBehaviorType type;


    private CountDownLatch latch;


    public SignatureEventHandler() {
        this.ecdsaSign = new ECDSASign();
        this.type = SignatureBehaviorType.SIMPLE_TRANSACTIONS;
    }

    public SignatureEventHandler(SignatureBehaviorType type, CountDownLatch latch) {
        this.type = type;
        this.ecdsaSign = new ECDSASign();
        this.latch = latch;
    }

    public SignatureEventHandler(SignatureBehaviorType type) {
        this.type = type;
        this.ecdsaSign = new ECDSASign();
        this.latch = null;
    }


    public CountDownLatch getLatch() {
        return latch;
    }

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }


    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        Transaction transaction = transactionEvent.getTransaction();

        if (transaction.getStatus().equals(StatusType.BUFFERED)) {
            return;
        }
        if (transaction.getStatus().equals(StatusType.ABORT)) {
            Optional.of("Transaction Marked with status ABORT").ifPresent(val -> {
                LOG.info(val);
                transaction.infos(val);
            });
            latch.countDown();
            return;
        }

        transaction.accept(this);
    }

    @Override
    public void visit(RegularTransaction regularTransaction) {
        FinalizeTask task = new FinalizeTask(regularTransaction, regularTransaction.getFrom());
        Thread.ofVirtual().start(task);
    }

    @Override
    public void visit(RewardsTransaction rewardsTransaction) {
        FinalizeTask task = new FinalizeTask(rewardsTransaction, rewardsTransaction.getRecipientAddress());
        Thread.ofVirtual().start(task);
    }

    @Override
    public void visit(StakingTransaction stakingTransaction) {
        FinalizeTask task = new FinalizeTask(stakingTransaction, stakingTransaction.getValidatorAddress());
        Thread.ofVirtual().start(task);
    }

    @Override
    public void visit(DelegateTransaction delegateTransaction) {
        FinalizeTask task = new FinalizeTask(delegateTransaction, delegateTransaction.getDelegatorAddress());
        Thread.ofVirtual().start(task);
    }

    @Override
    public void visit(UnclaimedFeeRewardTransaction unclaimedFeeRewardTransaction) {
        latch.countDown();
    }

    @Override
    public void visit(UnDelegateTransaction unDelegateTransaction) {
        FinalizeTask task = new FinalizeTask(unDelegateTransaction, unDelegateTransaction.getDelegatorAddress());
        Thread.ofVirtual().start(task);
    }

    @Override
    public void visit(UnstakingTransaction unstakingTransaction) {
        FinalizeTask task = new FinalizeTask(unstakingTransaction, unstakingTransaction.getValidatorAddress());
        Thread.ofVirtual().start(task);
    }


    private class FinalizeTask implements Runnable {
        private final Transaction transaction;
        private final String address;

        public FinalizeTask(Transaction transaction, String address) {
            this.transaction = transaction;
            this.address = address;
        }

        public Transaction getTransaction() {
            return transaction;
        }


        @SneakyThrows
        @Override
        public void run() {
            boolean verify = ecdsaSign.secp256r1Verify(transaction.getHash().getBytes(StandardCharsets.UTF_8), transaction.getXAxis(),transaction.getYAxis(), transaction.getSignature());
            if (!verify) {
                Optional.of("Transaction Wallet signature is not valid ABORT").ifPresent(val -> {
                    LOG.info(val);
                    transaction.infos(val);
                });
                latch.countDown();
                transaction.setStatus(StatusType.ABORT);
                MemoryTransactionPool.getInstance().delete(transaction);
                return;
            }
            if (type.equals(SignatureBehaviorType.BLOCK_TRANSACTIONS)) {
                latch.countDown();
                return;
            }
            // LOG.info("Transaction signature is  valid: " + transaction.getHash());
            if (MemoryTransactionPool.getInstance().checkAdressExists(transaction)) {
                CacheTemporalTransactionPool.getInstance().add(transaction);
            } else {
                MemoryTransactionPool.getInstance().add(transaction);
            }
            latch.countDown();
        }
    }

    public enum SignatureBehaviorType {
        BLOCK_TRANSACTIONS("BLOCK_TRANSACTIONS"),
        SIMPLE_TRANSACTIONS("SIMPLE_TRANSACTIONS");

        private final String title;

        SignatureBehaviorType(String title) {
            this.title = title;
        }
    }

}
