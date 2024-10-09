package io.Adrestus.protocol;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.Adrestus.config.CacheConfigurationTest;
import io.Adrestus.config.SocketConfigOptions;
import io.Adrestus.core.*;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.RingBuffer.publisher.ReceiptEventPublisher;
import io.Adrestus.crypto.elliptic.mapper.BigDecimalSerializer;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.network.CachedEventLoop;
import io.Adrestus.network.IPFinder;
import io.Adrestus.network.ReceiptChannelHandler;
import io.Adrestus.network.TCPTransactionConsumer;
import io.Adrestus.rpc.RpcAdrestusClient;
import io.Adrestus.util.SerializationUtil;
import io.distributedLedger.ZoneDatabaseFactory;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BindServerReceiptTask extends AdrestusTask {
    private static Logger LOG = LoggerFactory.getLogger(BindServerReceiptTask.class);
    private final SerializationUtil<Receipt> recep;
    private final ReceiptEventPublisher publisher;
    private final Cache<Integer, TransactionBlock> receiptloadingCache;
    private TCPTransactionConsumer<byte[]> receive;
    private ReceiptChannelHandler receiptChannelHandler;

    public BindServerReceiptTask() {
        super();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        this.recep = new SerializationUtil<Receipt>(Receipt.class, list);
        this.callBackReceive();
        this.receiptloadingCache = Caffeine.newBuilder()
                .initialCapacity(CacheConfigurationTest.INITIAL_CAPACITY)
                .maximumSize(CacheConfigurationTest.MAXIMUM_SIZE)
                .expireAfterWrite(CacheConfigurationTest.EXPIRATION_MINUTES, TimeUnit.MINUTES)
                .expireAfterAccess(CacheConfigurationTest.EXPIRATION_MINUTES, TimeUnit.MINUTES)
                .build();
        this.publisher = new ReceiptEventPublisher(1024);
        this.publisher.
                withGenerationEventHandler().
                withHeightEventHandler().
                withOutboundMerkleEventHandler().
                withZoneEventHandler().
                withReplayEventHandler().
                withEmptyEventHandler().
                withPublicKeyEventHandler()
                .withSignatureEventHandler()
                .withZoneFromEventHandler()
                .mergeEvents();
        this.publisher.start();
    }


    public void callBackReceive() {
        this.receive = x -> {
            Receipt receipt = recep.decode(x);
            if (receipt.getReceiptBlock() == null) {
                return "";
            }

            TransactionBlock transactionBlock = receiptloadingCache.getIfPresent(receipt.getReceiptBlock().getHeight());
            if (transactionBlock != null) {
                Transaction trx = transactionBlock.getTransactionList().get(receipt.getPosition());
                ReceiptBlock receiptBlock1 = new ReceiptBlock(StatusType.PENDING, receipt, transactionBlock, trx);
                publisher.publish(receiptBlock1);
            } else {
                List<String> ips = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(receipt.getZoneFrom()).values().stream().collect(Collectors.toList());
                ips.remove(IPFinder.getLocalIP());
                int RPCTransactionZonePort = ZoneDatabaseFactory.getDatabaseRPCPort(receipt.getZoneFrom());
                ArrayList<InetSocketAddress> toConnectTransaction = new ArrayList<>();
                ips.stream().forEach(ip -> {
                    try {
                        toConnectTransaction.add(new InetSocketAddress(InetAddress.getByName(ip), RPCTransactionZonePort));
                    } catch (UnknownHostException e) {
                        throw new RuntimeException(e);
                    }
                });
                RpcAdrestusClient client = null;
                try {
                    client = new RpcAdrestusClient(new TransactionBlock(), toConnectTransaction, CachedEventLoop.getInstance().getEventloop());
                    client.connect();

                    ArrayList<String> to_search = new ArrayList<>();
                    to_search.add(String.valueOf(receipt.getReceiptBlock().getHeight()));

                    List<TransactionBlock> currentblock = client.getBlock(to_search);
                    if (currentblock.isEmpty()) {
                        return "";
                    }

                    int index = receipt.getPosition();
                    Transaction trx = currentblock.get(currentblock.size() - 1).getTransactionList().get(index);

                    ReceiptBlock receiptBlock1 = new ReceiptBlock(StatusType.PENDING, receipt, currentblock.get(currentblock.size() - 1), trx);
                    this.receiptloadingCache.put(currentblock.get(currentblock.size() - 1).getHeight(), currentblock.get(currentblock.size() - 1));

                    publisher.publish(receiptBlock1);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (client != null) {
                        client.close();
                        client = null;
                    }
                }
            }
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
