package io.Adrestus.core.RingBuffer.handler.transactions;

import com.lmax.disruptor.EventHandler;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import io.Adrestus.core.Transaction;
import io.Adrestus.core.TransactionStatus;
import io.Adrestus.crypto.elliptic.ECDSASign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.util.concurrent.ExecutorService;

public class SignatureEventHandler implements EventHandler<TransactionEvent> {
    private static Logger LOG = LoggerFactory.getLogger(SignatureEventHandler.class);
    private final ExecutorService executorService;
    private final ECDSASign ecdsaSign;

    public SignatureEventHandler(ExecutorService executorService) {
        this.executorService = executorService;
        this.ecdsaSign = new ECDSASign();
    }

    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        Transaction transaction = transactionEvent.getTransaction();
        FinalizeTask task = new FinalizeTask();
        task.setTransaction((Transaction) transaction.clone());
        executorService.submit(task);
    }


    private class FinalizeTask implements Runnable {
        private Transaction transaction;

        public FinalizeTask() {
        }

        public Transaction getTransaction() {
            return transaction;
        }

        public void setTransaction(Transaction transaction) {
            this.transaction = transaction;
        }

        @Override
        public void run() {
            if (!ecdsaSign.secp256Verify(Hex.decode(transaction.getHash()), transaction.getFrom(), transaction.getSignature())) {
                LOG.info("Transaction signature is not valid ABORT");
                transaction.setStatus(TransactionStatus.ABORT);
                return;
            }

        }
    }
}
