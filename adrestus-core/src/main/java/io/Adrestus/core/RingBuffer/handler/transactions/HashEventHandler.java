package io.Adrestus.core.RingBuffer.handler.transactions;

import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.Transaction;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.elliptic.SignatureData;
import io.Adrestus.util.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HashEventHandler extends TransactionEventHandler {
    private static Logger LOG = LoggerFactory.getLogger(HashEventHandler.class);
    private SerializationUtil<Transaction> wrapper;

    public HashEventHandler() {
        wrapper = new SerializationUtil<Transaction>(Transaction.class);
    }

    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        try {
            wrapper = new SerializationUtil<Transaction>(Transaction.class);
            Transaction transaction = transactionEvent.getTransaction();
            if (transaction.getHash().length() != 64) {
                LOG.info("Transaction hashes length is not valid");
                transaction.setStatus(StatusType.ABORT);
            }

            Transaction cloneable = (Transaction) transaction.clone();
            cloneable.setHash("");
            cloneable.setSignature(new SignatureData());
            byte[] toHash = wrapper.encode(cloneable);
            String result_hash = HashUtil.sha256_bytetoString(toHash);
            if (!result_hash.equals(transaction.getHash())) {
                LOG.info("Transaction hashes does not match");
                transaction.setStatus(StatusType.ABORT);
            }


        } catch (NullPointerException ex) {
            LOG.info("Transaction is empty");
        }

    }
}
