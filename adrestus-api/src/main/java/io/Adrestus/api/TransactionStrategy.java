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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
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
    private volatile Boolean[] termination;

    public TransactionStrategy(Transaction transaction) {
        this.transaction_list = new ArrayList<>();
        this.transaction = transaction;
        this.executorService = Executors.newFixedThreadPool(AdrestusConfiguration.CORES);
        this.list_ip = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).values().stream().collect(Collectors.toList());
        this.termination = new Boolean[this.list_ip.size()];
        Arrays.fill(this.termination, Boolean.FALSE);
    }

    public TransactionStrategy(List<Transaction> transaction_list) {
        this.transaction_list = transaction_list;
        this.executorService = Executors.newFixedThreadPool(AdrestusConfiguration.CORES);
        this.list_ip = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).values().stream().collect(Collectors.toList());
        this.termination = new Boolean[this.list_ip.size()];
        Arrays.fill(this.termination, Boolean.FALSE);
    }

    @Override
    public void execute() {
        list_ip.stream().forEach(val -> executorService.execute(new TransactionWorker(this, val, new CountDownLatch(transaction_list.size()))));
    }

    @Override
    public void block_until_send() {
        while (Arrays.stream(this.termination).filter(val -> val.equals(Boolean.FALSE)).findFirst().isPresent()) {
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


        private TransactionStrategy instance;
        private Eventloop eventloop;
        private Transaction transaction;
        private List<Transaction> transaction_list;
        private static SerializationUtil<Transaction> transaction_encode;
        private static CountDownLatch termination;
        private AsyncTcpSocket socket;


        public TransactionWorker(TransactionStrategy instance, String ip, CountDownLatch terminate) {
            this.instance = instance;
            this.transaction_encode = new SerializationUtil<Transaction>(Transaction.class);
            if (this.instance.transaction_list == null) {
                this.transaction = this.instance.transaction;
                this.transaction_list = new ArrayList<>();
            } else
                this.transaction_list = this.instance.transaction_list;
            this.ip = ip;
            termination = terminate;
            this.eventloop = Eventloop.create().withCurrentThread();
        }


        @Override
        public void run() {
            if (transaction_list != null)
                this.MultipleAsync();
            else
                this.SingleAsync();
            eventloop.run();
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
                    try {
                        Thread.sleep(100);
                        termination.await();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    transaction = null;
                    this.clean();

                } else {
                    System.out.printf("Could not connect to server, make sure it is started: %s%n", e);
                    while (termination.getCount() > 0) {
                        termination.countDown();
                    }
                    this.clean();
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
                            i -> loadData(this.transaction_list.get(i))
                                    .then(bytes -> socket.write(ByteBuf.wrapForReading((bytes))))
                                    .then(() -> bufsSupplier.needMoreData())
                                    .then(() -> decrease())
                                    .map($2 -> i + 1))
                            .whenComplete(socket::close)
                            .whenException(ex -> {
                                throw new RuntimeException(ex);
                            });
                    try {
                        termination.await();
                        this.clean();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    this.clean();
                } else {
                    System.out.printf("Could not connect to server, make sure it is started: %s%n", e);
                    while (termination.getCount() > 0) {
                        termination.countDown();
                    }
                    this.clean();
                }
            });
        }

        private static @NotNull Promise<byte[]> loadData(Transaction transaction) {
            byte transaction_hash[] = transaction_encode.encode(transaction, 1024);
            byte[] concatBytes = ArrayUtils.addAll(transaction_hash, "\r\n".getBytes());
            return Promise.of(concatBytes);
        }

        private static @NotNull Promise<Void> decrease() {
            termination.countDown();
            return Promise.complete();
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
            int index = this.instance.list_ip.indexOf(ip);
            this.instance.termination[index] = Boolean.TRUE;
            transaction = null;
            transaction_encode = null;
            instance = null;
        }
    }

}
