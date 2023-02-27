package io.Adrestus.protocol;

import io.Adrestus.config.TransactionConfigOptions;
import io.Adrestus.core.RingBuffer.handler.transactions.SignatureEventHandler;
import io.Adrestus.core.RingBuffer.publisher.TransactionEventPublisher;
import io.Adrestus.core.Transaction;
import io.Adrestus.crypto.elliptic.ECDSASignatureData;
import io.Adrestus.crypto.elliptic.mapper.SignatureDataSerializer;
import io.Adrestus.network.IPFinder;
import io.Adrestus.network.TCPTransactionConsumer;
import io.Adrestus.network.TransactionChannelHandler;
import io.Adrestus.util.SerializationUtil;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TransactionTask extends AdrestusTask {
    private static Logger LOG = LoggerFactory.getLogger(TransactionTask.class);
    private SerializationUtil<Transaction> serenc;
    private final TransactionEventPublisher publisher;
    private TCPTransactionConsumer<byte[]> receive;
    private TransactionChannelHandler transactionChannelHandler;
    int counter = 0;
    private ArrayList<Transaction> list = new ArrayList<>();

    public TransactionTask() {
        super();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECDSASignatureData.class, ctx -> new SignatureDataSerializer()));
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
                .mergeEventsAndPassThen(new SignatureEventHandler(SignatureEventHandler.SignatureBehaviorType.SIMPLE_TRANSACTIONS));
        publisher.start();
    }

    public void callBackReceive() {
        this.receive = x -> {
            try {
                Transaction transaction = serenc.decode(x);
                counter++;
                //System.out.println("Server Message:" + transaction.toString());
                System.out.println("s " + counter);
                // publisher.publish(transaction);
                if (list.contains(transaction))
                    System.out.println("edw " + transaction);
                else
                    list.add(transaction);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    @SneakyThrows
    @Override
    public void execute() {
        transactionChannelHandler = new TransactionChannelHandler<byte[]>(IPFinder.getLocal_address(), TransactionConfigOptions.TRANSACTION_PORT);
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
