package io.Adrestus.core.RingBuffer.handler.transactions;

import io.Adrestus.core.Resourses.MemoryPool;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.Transaction;
import io.Adrestus.crypto.elliptic.ECDSASign;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

public class SignatureEventHandler extends TransactionEventHandler {
    private static Logger LOG = LoggerFactory.getLogger(SignatureEventHandler.class);

    private final ECDSASign ecdsaSign;
    private final SignatureBehaviorType type;


    private ExecutorService executorService;
    private CountDownLatch latch;
    private Transaction transaction;

    public SignatureEventHandler(ExecutorService executorService) {
        this.executorService = executorService;
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

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        transaction = transactionEvent.getTransaction();
        if (transaction.getStatus().equals(StatusType.ABORT)) {
            LOG.info("Transaction Marked with status ABORT");
            if (type.equals(SignatureBehaviorType.BLOCK_TRANSACTIONS))
                latch.countDown();
            return;
        }
        FinalizeTask task = new FinalizeTask();
        executorService.submit(task);
    }


    private class FinalizeTask implements Runnable {


        public FinalizeTask() {
        }

        public Transaction getTransaction() {
            return transaction;
        }


        @SneakyThrows
        @Override
        public void run() {
            if (!ecdsaSign.secp256Verify(Hex.decode(transaction.getHash()), transaction.getFrom(), transaction.getSignature())) {
                LOG.info("Transaction signature is not valid ABORT");
                if (type.equals(SignatureBehaviorType.BLOCK_TRANSACTIONS))
                    latch.countDown();
                transaction.setStatus(StatusType.ABORT);
                MemoryPool.getInstance().delete(transaction);
                return;
            }
            if (type.equals(SignatureBehaviorType.BLOCK_TRANSACTIONS)) {
                LOG.info("Transaction signature is  valid");
                latch.countDown();
                return;
            }
            LOG.info("Transaction signature is  valid");
            MemoryPool.getInstance().add(transaction);
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
