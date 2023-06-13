package io.Adrestus.protocol;

import io.Adrestus.config.SocketConfigOptions;
import io.Adrestus.core.Resourses.CacheTemporalTransactionPool;
import io.Adrestus.core.Resourses.MemoryRingBuffer;
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
import java.util.concurrent.atomic.AtomicInteger;

public class BindServerTransactionTask extends AdrestusTask {
    private static Logger LOG = LoggerFactory.getLogger(BindServerTransactionTask.class);
    private SerializationUtil<Transaction> serenc;
    private TCPTransactionConsumer<byte[]> receive;
    private TransactionChannelHandler transactionChannelHandler;

    public BindServerTransactionTask() {
        super();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        this.serenc = new SerializationUtil<Transaction>(Transaction.class, list);
        this.callBackReceive();
        MemoryRingBuffer.getInstance().setup();
        CacheTemporalTransactionPool.getInstance().setup(true);
    }


    public void callBackReceive() {
        AtomicInteger count= new AtomicInteger();
        this.receive = x -> {
            try {
                Transaction transaction = (Transaction) serenc.decode(x).clone();
                MemoryRingBuffer.getInstance().publish(transaction);
                count.getAndIncrement();
                System.out.println(count.get());
               // MemoryTransactionPool.getInstance().add(transaction);
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
        if (MemoryRingBuffer.getInstance() != null) {
            MemoryRingBuffer.getInstance().close();
        }

    }
}
