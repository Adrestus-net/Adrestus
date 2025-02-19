package io.Adrestus.protocol;

import io.Adrestus.config.APIConfiguration;
import io.Adrestus.config.SocketConfigOptions;
import io.Adrestus.core.Resourses.CacheTemporalTransactionPool;
import io.Adrestus.core.Resourses.MemoryRingBuffer;
import io.Adrestus.core.Resourses.MemoryTransactionPool;
import io.Adrestus.core.Transaction;
import io.Adrestus.crypto.elliptic.mapper.BigDecimalSerializer;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.network.IPFinder;
import io.Adrestus.network.TCPTransactionConsumer;
import io.Adrestus.network.TransactionChannelHandler;
import io.Adrestus.util.SerializationUtil;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class BindServerTransactionTask extends AdrestusTask {
    private static Logger LOG = LoggerFactory.getLogger(BindServerTransactionTask.class);
    private SerializationUtil<Transaction> serenc;
    private TCPTransactionConsumer<byte[]> receive;
    private TransactionChannelHandler transactionChannelHandler;

    public BindServerTransactionTask() {
        super();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        this.serenc = new SerializationUtil<Transaction>(Transaction.class, list);
        this.callBackReceive();
        MemoryRingBuffer.getInstance().setup();
        CacheTemporalTransactionPool.getInstance().setup(true);
    }


    public void callBackReceive() {
        this.receive = x -> {
            String MSG = "";
            try {
                Transaction transaction = (Transaction) serenc.decode(x).clone();
                if (MemoryTransactionPool.getInstance().checkAdressExists(transaction)) {
                    MSG = APIConfiguration.MSG_FAILED;
                } else
                    MSG = APIConfiguration.MSG_SUCCESS;
                MemoryRingBuffer.getInstance().publish(transaction);
                //MemoryTransactionPool.getInstance().add(transaction);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return MSG;
        };
    }

    @SneakyThrows
    @Override
    public void execute() {
        try {
            transactionChannelHandler = new TransactionChannelHandler<byte[]>(IPFinder.getLocal_address(), SocketConfigOptions.TRANSACTION_PORT);
            transactionChannelHandler.BindServerAndReceive(receive);
            LOG.info("Transaction: TransactionChannelHandler " + IPFinder.getLocal_address());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    @Override
    public void close() {
        if (transactionChannelHandler != null) {
            try {
                transactionChannelHandler.close();
                transactionChannelHandler = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (MemoryRingBuffer.getInstance() != null) {
            MemoryRingBuffer.getInstance().close();
        }

    }
}
