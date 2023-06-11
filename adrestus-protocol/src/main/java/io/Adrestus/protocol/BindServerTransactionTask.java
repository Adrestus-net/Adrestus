package io.Adrestus.protocol;

import io.Adrestus.config.SocketConfigOptions;
import io.Adrestus.core.Resourses.MemoryTransactionPool;
import io.Adrestus.core.RingBuffer.handler.transactions.SignatureEventHandler;
import io.Adrestus.core.RingBuffer.publisher.TransactionEventPublisher;
import io.Adrestus.core.Transaction;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.network.IPFinder;
import io.Adrestus.network.TCPTransactionConsumer;
import io.Adrestus.network.TransactionChannelHandler;
import io.Adrestus.util.SerializationUtil;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class BindServerTransactionTask extends AdrestusTask {
    private static Logger LOG = LoggerFactory.getLogger(BindServerTransactionTask.class);
    private SerializationUtil<Transaction> serenc;
    private final TransactionEventPublisher publisher;
    private TCPTransactionConsumer<byte[]> receive;
    private TransactionChannelHandler transactionChannelHandler;

    public BindServerTransactionTask() {
        super();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        this.serenc = new SerializationUtil<Transaction>(Transaction.class, list);
        this.publisher = new TransactionEventPublisher(2048);
        this.callBackReceive();
        this.setup();
    }

    public void setup() {
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
                .withSameOriginEventHandler()
                .withZoneEventHandler()
                .withDuplicateEventHandler()
                .withBroadcastTransactionHandler()
                .mergeEventsAndPassThen(new SignatureEventHandler(SignatureEventHandler.SignatureBehaviorType.SIMPLE_TRANSACTIONS));
        publisher.start();
    }

    public void callBackReceive() {
        this.receive = x -> {
            try {
                Transaction transaction = serenc.decode(x);
                // System.out.println(transaction.toString());
                if (!MemoryTransactionPool.getInstance().checkAdressExists(transaction)) {
                    publisher.publish(transaction);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    @SneakyThrows
    @Override
    public void execute() {
        transactionChannelHandler = new TransactionChannelHandler<byte[]>(IPFinder.getLocal_address(), SocketConfigOptions.TRANSACTION_PORT);
        transactionChannelHandler.BindServerAndReceive(receive);
        LOG.info("Transaction: TransactionChannelHandler " + IPFinder.getLocal_address());
    }

    @SneakyThrows
    @Override
    public void close() {
        if (transactionChannelHandler != null) {
            transactionChannelHandler.close();
            transactionChannelHandler = null;
        }
        if (publisher != null) {
            publisher.getJobSyncUntilRemainingCapacityZero();
            publisher.close();
        }

    }
}
