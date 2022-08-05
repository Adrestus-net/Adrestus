package io.Adrestus.core.RingBuffer.handler.transactions;

import com.lmax.disruptor.EventHandler;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import io.Adrestus.core.Transaction;
import io.Adrestus.core.TransactionStatus;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.util.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HashEventHandler implements EventHandler<TransactionEvent> {
    private static Logger LOG = LoggerFactory.getLogger(HashEventHandler.class);
    private final SerializationUtil<Transaction> wrapper;

    public HashEventHandler() {
        wrapper = new SerializationUtil<Transaction>(Transaction.class);
    }

    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        try {
            Transaction transaction = transactionEvent.getTransaction();
            if (transaction.getHash().length() != 32) {
                Transaction cloneable = (Transaction) transaction.clone();
                cloneable.setHash("");
                byte[] toHash = wrapper.encode(cloneable);
                String result_hash = HashUtil.sha256_bytetoString(toHash);
                if (!result_hash.equals(transaction.getHash())) {
                    LOG.info("Transaction hashes does not match");
                    transaction.setStatus(TransactionStatus.ABORT);
                }
            }

        } catch (NullPointerException ex) {
            LOG.info("Transaction is empty");
        }

    }
}
