package io.Adrestus.core.RingBuffer.handler.blocks;

import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import io.Adrestus.core.RingBuffer.handler.transactions.SignatureEventHandler;
import io.Adrestus.core.RingBuffer.publisher.TransactionEventPublisher;
import io.Adrestus.core.TransactionBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class TransactionsSignatureEventHandler implements BlockEventHandler<AbstractBlockEvent> {
    private static Logger LOG = LoggerFactory.getLogger(TransactionsSignatureEventHandler.class);
    private static final int JOB_QUEUE_SIZE = 1024;

    private final TransactionEventPublisher publisher;

    public TransactionsSignatureEventHandler() {
        publisher = new TransactionEventPublisher(JOB_QUEUE_SIZE);
    }

    @Override
    public void onEvent(AbstractBlockEvent blockEvent, long l, boolean b) throws Exception {
        try {
            TransactionBlock block = (TransactionBlock) blockEvent.getBlock();

            if (block.getTransactionList().isEmpty()) {
                LOG.info("Empty Transaction List");
                return;
            }


            CountDownLatch latch = new CountDownLatch(block.getTransactionList().size());

            publisher
                    .withAddressSizeEventHandler()
                    .withDelegateEventHandler()
                    .withDoubleSpendEventHandler()
                    .withHashEventHandler()
                    .withReplayEventHandler()
                    .withRewardEventHandler()
                    .withStakingEventHandler()
                    .withTransactionFeeEventHandler()
                    .withNonceEventHandler()
                    .withAmountEventHandler()
                    .mergeEventsAndPassThen(new SignatureEventHandler(SignatureEventHandler.SignatureBehaviorType.BLOCK_TRANSACTIONS, latch));

            publisher.start();
            block.getTransactionList().stream().forEach(publisher::publish);
            latch.await();
            publisher.close();

        } catch (NullPointerException ex) {
            LOG.info("Block is empty");
        }
    }
}