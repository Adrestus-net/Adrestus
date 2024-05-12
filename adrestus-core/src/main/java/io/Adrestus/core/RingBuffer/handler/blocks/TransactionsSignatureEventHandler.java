package io.Adrestus.core.RingBuffer.handler.blocks;

import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import io.Adrestus.core.RingBuffer.handler.transactions.SignatureEventHandler;
import io.Adrestus.core.RingBuffer.publisher.TransactionEventPublisher;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.Transaction;
import io.Adrestus.core.TransactionBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public class TransactionsSignatureEventHandler implements BlockEventHandler<AbstractBlockEvent> {
    private static Logger LOG = LoggerFactory.getLogger(TransactionsSignatureEventHandler.class);

    private final TransactionEventPublisher publisher;

    private final SignatureEventHandler signatureEventHandler;

    public TransactionsSignatureEventHandler() {
        this.signatureEventHandler = new SignatureEventHandler(SignatureEventHandler.SignatureBehaviorType.BLOCK_TRANSACTIONS);
        this.publisher = new TransactionEventPublisher(AdrestusConfiguration.TRANSACTIONS_QUEUE_SIZE);
        this.publisher
                .withAddressSizeEventHandler()
                .withTypeEventHandler()
                .withAmountEventHandler()
                .withHashEventHandler()
                .withNonceEventHandler()
                .withStakingEventHandler()
                .withTransactionFeeEventHandler()
                .withTimestampEventHandler()
                .withZoneEventHandler()
                .withDuplicateEventHandler()
                .withSecp256k1EventHandler()
                .mergeEventsAndPassThen(this.signatureEventHandler);

        this.publisher.start();
    }

    @Override
    public void onEvent(AbstractBlockEvent blockEvent, long l, boolean b) throws Exception {
        try {
            TransactionBlock block = (TransactionBlock) blockEvent.getBlock();
            if (block.getStatustype().equals(StatusType.ABORT)) {
                LOG.info("Status marked as invalid ABORT");
                return;
            }
            if (block.getTransactionList().isEmpty()) {
                LOG.info("Empty Transaction List");
                return;
            }


            CountDownLatch latch = new CountDownLatch(block.getTransactionList().size());
            this.signatureEventHandler.setLatch(latch);
            block.getTransactionList().forEach(publisher::publish);
            latch.await();
            Optional<Transaction> marked = block.getTransactionList().stream().filter(this::isStatusAbort).findAny();

            if (marked.isPresent())
                block.setStatustype(StatusType.ABORT);

            publisher.getJobSyncUntilRemainingCapacityZero();

        } catch (NullPointerException ex) {
            LOG.info("Block is empty");
        }
    }

    private boolean isStatusAbort(Transaction transaction) {
        return transaction.getStatus() == StatusType.ABORT;
    }

}
