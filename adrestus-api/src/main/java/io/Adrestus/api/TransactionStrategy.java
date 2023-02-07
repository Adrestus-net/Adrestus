package io.Adrestus.api;

import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.config.TransactionConfigOptions;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.Transaction;
import io.Adrestus.util.SerializationUtil;
import io.activej.bytebuf.ByteBuf;
import io.activej.csp.ChannelSupplier;
import io.activej.csp.binary.BinaryChannelSupplier;
import io.activej.eventloop.Eventloop;
import io.activej.net.socket.tcp.AsyncTcpSocket;
import io.activej.net.socket.tcp.AsyncTcpSocketNio;
import io.activej.promise.Promise;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.activej.eventloop.Eventloop.getCurrentEventloop;
import static io.activej.promise.Promises.loop;
import static java.nio.charset.StandardCharsets.UTF_8;

public class TransactionStrategy implements IStrategy {

    private final List<String> list_ip;
    private final ExecutorService executorService;
    private Transaction transaction;
    private List<Transaction> transaction_list;

    public TransactionStrategy(Transaction transaction) {
        this.transaction = transaction;
        this.executorService = Executors.newFixedThreadPool(AdrestusConfiguration.CORES);
        this.list_ip = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).values().stream().collect(Collectors.toList());
    }

    public TransactionStrategy(List<Transaction> transaction_list) {
        this.transaction_list = transaction_list;
        this.executorService = Executors.newFixedThreadPool(AdrestusConfiguration.CORES);
        this.list_ip = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).values().stream().collect(Collectors.toList());
    }

    @Override
    public void execute() {
        for (int i = 0; i < list_ip.size(); i++) {
            if (transaction_list == null)
                this.executorService.execute(new TransactionWorker(list_ip.get(i), transaction));
            else
                this.executorService.execute(new TransactionWorker(list_ip.get(i), transaction_list));
        }
    }

    @Override
    public void block_until_send() {
        while (!transaction_list.isEmpty() || transaction != null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void terminate() {
        this.awaitTerminationAfterShutdown();
        if (this.transaction_list != null) {
            this.transaction_list.clear();
            this.transaction_list = null;
        }
        this.transaction = null;
        this.list_ip.clear();
    }

    public void awaitTerminationAfterShutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException ex) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
        }
    }

    private static class TransactionWorker implements Runnable {

        private final String ip;

        private Eventloop eventloop;
        private Transaction transaction;
        private List<Transaction> transaction_list;
        private static SerializationUtil<Transaction> transaction_encode;
        private AsyncTcpSocket socket;

        public TransactionWorker(String ip, Transaction transaction) {
            this.transaction_encode = new SerializationUtil<Transaction>(Transaction.class);
            this.transaction = transaction;
            this.ip = ip;
            this.eventloop = Eventloop.create().withCurrentThread();
        }

        public TransactionWorker(String ip, List<Transaction> transaction_list) {
            this.transaction_encode = new SerializationUtil<Transaction>(Transaction.class);
            this.transaction_list = transaction_list;
            this.ip = ip;
            this.eventloop = Eventloop.create().withCurrentThread();
        }

        @Override
        public void run() {
            if (transaction_list != null)
                this.MultipleAsync();
            else
                this.SingleAsync();
            eventloop.run();
            this.clean();
        }

        private void SingleAsync() {
            eventloop.connect(new InetSocketAddress(ip, TransactionConfigOptions.TRANSACTION_PORT), (socketChannel, e) -> {
                if (e == null) {
                    System.out.println("Connected to server, enter some text and send it by pressing 'Enter'.");
                    try {
                        socket = AsyncTcpSocketNio.wrapChannel(getCurrentEventloop(), socketChannel, null);
                    } catch (IOException ioException) {
                        throw new RuntimeException(ioException);
                    }

                    byte[] data = transaction_encode.encode(transaction, 1024);
                    socket.write(ByteBuf.wrapForReading(ArrayUtils.addAll(data, "\r\n".getBytes(UTF_8))));

                } else {
                    System.out.printf("Could not connect to server, make sure it is started: %s%n", e);
                }
            });
        }

        private void MultipleAsync() {
            eventloop.connect(new InetSocketAddress(ip, TransactionConfigOptions.TRANSACTION_PORT), (socketChannel, e) -> {
                if (e == null) {
                    System.out.println("Connected to server, enter some text and send it by pressing 'Enter'.");
                    try {
                        socket = AsyncTcpSocketNio.wrapChannel(getCurrentEventloop(), socketChannel, null);
                    } catch (IOException ioException) {
                        throw new RuntimeException(ioException);
                    }
                    BinaryChannelSupplier bufsSupplier = BinaryChannelSupplier.of(ChannelSupplier.ofSocket(socket));
                    loop(0,
                            i -> i < this.transaction_list.size(),
                            i -> loadData(this.transaction_list.get(i)).then(bytes -> socket.write(ByteBuf.wrapForReading((bytes)))).then(() -> bufsSupplier.needMoreData())
                                    .map($2 -> i + 1))
                            .whenComplete(socket::close);
                } else {
                    System.out.printf("Could not connect to server, make sure it is started: %s%n", e);
                }
            });
        }

        private static @NotNull Promise<byte[]> loadData(Transaction transaction) {
            byte transaction_hash[] = transaction_encode.encode(transaction, 1024);
            byte[] concatBytes = ArrayUtils.addAll(transaction_hash, "\r\n".getBytes());
            return Promise.of(concatBytes);
        }

        private void clean() {
            if (this.transaction_list != null) {
                this.transaction_list.clear();
                this.transaction_list = null;
            }
            if (socket != null) {
                socket.close();
                socket = null;
            }
            if (eventloop != null) {
                eventloop.breakEventloop();
                eventloop = null;
            }
            transaction = null;
            transaction_encode = null;
        }
    }

}
