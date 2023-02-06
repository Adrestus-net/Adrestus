package io.Adrestus.protocol;

import io.Adrestus.config.TransactionConfigOptions;
import io.Adrestus.core.Receipt;
import io.Adrestus.core.Resourses.MemoryReceiptPool;
import io.Adrestus.network.IPFinder;
import io.Adrestus.network.TCPTransactionConsumer;
import io.Adrestus.network.TransactionChannelHandler;
import io.Adrestus.util.SerializationUtil;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BindServerReceiptTask extends AdrestusTask {
    private static Logger LOG = LoggerFactory.getLogger(BindServerReceiptTask.class);
    private final SerializationUtil<Receipt> recep;
    private TCPTransactionConsumer<byte[]> receive;
    private TransactionChannelHandler transactionChannelHandler;
    public BindServerReceiptTask() {
        super();
        this.recep = new SerializationUtil<Receipt>(Receipt.class);
        this.callBackReceive();
    }


    public void callBackReceive() {
        this.receive = x -> {
            Receipt receipt = recep.decode(x);
            if (receipt.getReceiptBlock() != null && !receipt.getReceiptBlock().getBlock_hash().equals(""))
                MemoryReceiptPool.getInstance().add(receipt);
        };
    }

    @SneakyThrows
    @Override
    public void execute() {
        transactionChannelHandler=new TransactionChannelHandler<byte[]>(IPFinder.getLocal_address(), TransactionConfigOptions.RECEIPT_PORT);
        transactionChannelHandler.BindServerAndReceive(receive);
        LOG.info("Receipt: TransactionChannelHandler " + IPFinder.getLocal_address());
    }

    @SneakyThrows
    @Override
    public void close() {
        if(transactionChannelHandler!=null) {
            transactionChannelHandler.close();
            transactionChannelHandler = null;
        }
    }
}
