package io.Adrestus.protocol;

import io.Adrestus.config.SocketConfigOptions;
import io.Adrestus.core.Receipt;
import io.Adrestus.core.Resourses.MemoryReceiptPool;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.network.IPFinder;
import io.Adrestus.network.ReceiptChannelHandler;
import io.Adrestus.network.TCPTransactionConsumer;
import io.Adrestus.network.TransactionChannelHandler;
import io.Adrestus.util.SerializationUtil;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class BindServerReceiptTask extends AdrestusTask {
    private static Logger LOG = LoggerFactory.getLogger(BindServerReceiptTask.class);
    private final SerializationUtil<Receipt> recep;
    private TCPTransactionConsumer<byte[]> receive;
    private ReceiptChannelHandler receiptChannelHandler;

    public BindServerReceiptTask() {
        super();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        this.recep = new SerializationUtil<Receipt>(Receipt.class, list);
        this.callBackReceive();
    }


    public void callBackReceive() {
        this.receive = x -> {
            Receipt receipt = recep.decode(x);
            if (receipt.getReceiptBlock() != null && !receipt.getReceiptBlock().getBlock_hash().equals(""))
                MemoryReceiptPool.getInstance().add(receipt);

            return "";
        };
    }

    @SneakyThrows
    @Override
    public void execute() {
        receiptChannelHandler = new ReceiptChannelHandler<byte[]>(IPFinder.getLocal_address(), SocketConfigOptions.RECEIPT_PORT);
        receiptChannelHandler.BindServerAndReceive(receive);
        LOG.info("Receipt: TransactionChannelHandler " + IPFinder.getLocal_address());
    }

    @SneakyThrows
    @Override
    public void close() {
        if (receiptChannelHandler != null) {
            receiptChannelHandler.close();
            receiptChannelHandler = null;
        }
    }
}
