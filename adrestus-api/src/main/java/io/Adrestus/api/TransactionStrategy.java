package io.Adrestus.api;

import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.config.SocketConfigOptions;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.Transaction;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.util.SerializationUtil;
import io.activej.bytebuf.ByteBuf;
import io.activej.bytebuf.ByteBufPool;
import io.activej.csp.ChannelSupplier;
import io.activej.csp.binary.BinaryChannelSupplier;
import io.activej.eventloop.Eventloop;
import io.activej.net.socket.tcp.AsyncTcpSocket;
import io.activej.net.socket.tcp.AsyncTcpSocketNio;
import io.activej.promise.Promise;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static io.activej.eventloop.Eventloop.getCurrentEventloop;
import static io.activej.promise.Promises.loop;
import static java.nio.charset.StandardCharsets.UTF_8;

public class TransactionStrategy implements IStrategy {
    private static Logger LOG = LoggerFactory.getLogger(TransactionStrategy.class);

    private static final int CONNECT_TIMER_DELAY_TIMEOUT = 4000;
    private static final int EXECUTION_TIMER_DELAY_TIMEOUT = 15000;
    private List<String> list_ip;
    private final ExecutorService executorService;
    private final Eventloop eventloop;
    private final SerializationUtil<Transaction> transaction_encode;
    private final Transaction transaction;
    private List<Transaction> transaction_list;
    private static CountDownLatch[] local_termination;
    private static Semaphore[] available;

    private final MessageListener messageListener;

    public TransactionStrategy(Transaction transaction, MessageListener messageListener) {
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        this.messageListener = messageListener;
        this.transaction_list = new ArrayList<>();
        this.transaction = transaction;
        this.executorService = Executors.newFixedThreadPool(AdrestusConfiguration.CORES);
        this.list_ip = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).values().stream().collect(Collectors.toList());
        this.eventloop = Eventloop.create().withCurrentThread();
        this.transaction_encode = new SerializationUtil<Transaction>(Transaction.class, list);
    }

    public TransactionStrategy(List<Transaction> transaction_list) {
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        this.messageListener = new MessageListener();
        this.transaction_list = transaction_list;
        this.executorService = Executors.newFixedThreadPool(AdrestusConfiguration.CORES);
        this.list_ip = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).values().stream().collect(Collectors.toList());
        this.local_termination = new CountDownLatch[list_ip.size()];
        this.available = new Semaphore[list_ip.size()];
        this.eventloop = Eventloop.create().withCurrentThread();
        this.transaction_encode = new SerializationUtil<Transaction>(Transaction.class, list);
        this.transaction = null;
    }


    private void Setup(List<Transaction> transaction_list) {
        for (int i = 0; i < local_termination.length; i++) {
            local_termination[i] = new CountDownLatch(transaction_list.size());
            available[i] = new Semaphore(1, true);
        }
    }

    @Override
    public void execute() {
        if (!transaction_list.isEmpty()) {
            Map<Integer, List<Transaction>> transcationGrouped = transaction_list.stream().collect(Collectors.groupingBy(w -> w.getZoneFrom()));
            for (Map.Entry<Integer, List<Transaction>> entry : transcationGrouped.entrySet()) {
                list_ip = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(entry.getKey()).values().stream().collect(Collectors.toList());
                this.Setup(entry.getValue());
                this.messageListener.setSize(list_ip.size());
                this.messageListener.onStart();
                for (int i = 0; i < list_ip.size(); i++) {
                    int finalI = i;
                    executorService.submit(() -> {
                        Eventloop eventloop = Eventloop.create().withCurrentThread();
                        MultipleAsync(list_ip.get(finalI), eventloop, finalI, entry.getValue());
                        eventloop.run();

                    });
                }
                this.awaitTerminationAfterShutdown();
                this.terminate();
            }
        } else {
            list_ip = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(transaction.getZoneFrom()).values().stream().collect(Collectors.toList());
            this.messageListener.setSize(list_ip.size());
            this.messageListener.onStart();
            for (int i = 0; i < list_ip.size(); i++) {
                int finalI = i;
                executorService.submit(() -> {
                    Eventloop eventloop = Eventloop.create().withCurrentThread();
                    SingleAsync(list_ip.get(finalI), eventloop);
                    eventloop.run();

                });
            }
            this.awaitTerminationAfterShutdown();
            this.terminate();
        }

        if (this.transaction_list != null) {
            this.transaction_list.clear();
            this.transaction_list = null;
        }
    }

    private void SingleAsync(String ip, Eventloop eventloop) {
        eventloop.connect(new InetSocketAddress(ip, SocketConfigOptions.TRANSACTION_PORT), CONNECT_TIMER_DELAY_TIMEOUT, (socketChannel, e) -> {
            if (e == null) {
                try {
                    AsyncTcpSocket socket = AsyncTcpSocketNio.wrapChannel(getCurrentEventloop(), socketChannel, null);

                    byte[] data = transaction_encode.encode(transaction, 1024);
                    ByteBuf sizeBuf = ByteBufPool.allocate(2); // enough to serialize size 1024
                    sizeBuf.writeVarInt(data.length);
                    ByteBuf appendedBuf = ByteBufPool.append(sizeBuf, ByteBuf.wrapForReading(data));
                    socket.write(appendedBuf);
                    socket.read().whenResult(buf -> this.messageListener.onNext(buf.getString(UTF_8))).whenComplete(socket::close);
                    socket = null;
                } catch (IOException ioException) {
                    throw new RuntimeException(ioException);
                }
            }
        });
    }

    private void MultipleAsync(String ip, Eventloop eventloop, int pos, List<Transaction> transaction_list) {
        eventloop.connect(new InetSocketAddress(ip, SocketConfigOptions.TRANSACTION_PORT), CONNECT_TIMER_DELAY_TIMEOUT, (socketChannel, e) -> {
            if (e == null) {
                try {
                    available[pos].acquire();
                    AsyncTcpSocket socket = AsyncTcpSocketNio.wrapChannel(getCurrentEventloop(), socketChannel, null);
                    BinaryChannelSupplier bufsSupplier = BinaryChannelSupplier.of(ChannelSupplier.ofSocket(socket));
                    loop(0,
                            i -> i < transaction_list.size(),
                            i -> loadData(transaction_list.get(i))
                                    .then(socket::write)
                                    .then(bufsSupplier::needMoreData)
                                    .then(() -> decrease(pos))
                                    .map($2 -> i + 1))
                            .whenComplete(socket::close)
                            .whenComplete(() -> release(pos))
                            .whenException(ex -> {
                                //enable this when problem occurred
                                //throw new RuntimeException(ex);
                            });
                } catch (IOException ex) {
                    ex.printStackTrace();
                    LOG.info("Exception caught" + ex.toString());
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    LOG.info("Exception caught" + ex.toString());
                }
            } else {
            }
        });
    }

    private static @NotNull Promise<ByteBuf> loadData(Transaction transaction) {
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        SerializationUtil<Transaction> transaction_encode = new SerializationUtil<Transaction>(Transaction.class, list);
        byte transaction_hash[] = transaction_encode.encode(transaction, 1024);
        ByteBuf sizeBuf = ByteBufPool.allocate(2); // enough to serialize size 1024
        sizeBuf.writeVarInt(transaction_hash.length);
        ByteBuf appendedBuf = ByteBufPool.append(sizeBuf, ByteBuf.wrapForReading(transaction_hash));
        return Promise.of(appendedBuf);
    }

    private static @NotNull Promise<Void> decrease(int pos) {
        local_termination[pos].countDown();
        return Promise.complete();
    }

    private static @NotNull Promise<Void> release(int pos) {
        available[pos].release();
        return Promise.complete();
    }


    private void terminate() {
        this.list_ip.clear();

        if (eventloop != null) {
            eventloop.breakEventloop();
        }
        if (available != null) {
            Arrays.fill(available, null);
            available = null;
        }
        if (local_termination != null) {
            Arrays.fill(local_termination, null);
            local_termination = null;
        }
    }

    @SneakyThrows
    public void awaitTerminationAfterShutdown() {
        if (local_termination != null) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @SneakyThrows
                @Override
                public void run() {
                    for (int i = 0; i < local_termination.length; i++) {
                        available[i].acquire();
                        while (local_termination[i].getCount() > 0) {
                            local_termination[i].countDown();
                        }
                    }
                }
            }, EXECUTION_TIMER_DELAY_TIMEOUT);
            for (int i = 0; i < local_termination.length; i++) {
                local_termination[i].await();
            }
            timer.cancel();
            timer.purge();
        }
        // termination.await();
        try {
            if (!executorService.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException ex) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}
