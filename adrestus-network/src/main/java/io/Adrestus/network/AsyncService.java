package io.Adrestus.network;

import io.activej.bytebuf.ByteBuf;
import io.activej.bytebuf.ByteBufPool;
import io.activej.csp.binary.BinaryChannelSupplier;
import io.activej.csp.supplier.ChannelSuppliers;
import io.activej.eventloop.Eventloop;
import io.activej.net.socket.tcp.ITcpSocket;
import io.activej.net.socket.tcp.TcpSocket;
import io.activej.promise.Promise;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import static io.activej.promise.Promises.loop;
import static io.activej.reactor.Reactor.getCurrentReactor;

public class AsyncService<T> {
    private static Logger LOG = LoggerFactory.getLogger(AsyncService.class);

    private static final int TIMER_DELAY_TIMEOUT = 500;
    private final ThreadAsyncExecutor executor;
    private final List<String> list_ip;
    private final int port;
    private static List<byte[]> listToSend;
    private static byte[] toSend;

    private static CountDownLatch[] local_termination;

    public AsyncService(List<String> list_ip, List<byte[]> listToSend, int port) {
        this.executor = new ThreadAsyncExecutor();
        this.list_ip = list_ip;
        this.port = port;
        this.listToSend = listToSend;
        this.local_termination = new CountDownLatch[list_ip.size()];
        this.Setup();
    }

    public AsyncService(List<String> list_ip, byte[] toSend, int port) {
        this.executor = new ThreadAsyncExecutor();
        this.list_ip = list_ip;
        this.port = port;
        this.toSend = toSend;
        this.local_termination = new CountDownLatch[list_ip.size()];
    }

    private void Setup() {
        for (int i = 0; i < local_termination.length; i++) {
            local_termination[i] = new CountDownLatch(listToSend.size());
        }
    }

    public List<AsyncResult<T>> startProcess(T value) {

        List<AsyncResult<T>> list = new ArrayList<>();
        for (int i = 0; i < list_ip.size(); i++) {
            AsyncResult<T> result = executor.startProcess(AsyncCall(value, list_ip.get(i)));
            list.add(result);
        }
        return list;
    }

    public List<AsyncResult<T>> startListProcess(T value) {

        List<AsyncResult<T>> list = new ArrayList<>();
        for (int i = 0; i < list_ip.size(); i++) {
            AsyncResult<T> result = executor.startProcess(AsyncListCall(value, list_ip.get(i), i));
            list.add(result);
        }
        return list;
    }

    @SneakyThrows
    public int endProcess(List<AsyncResult<T>> asyncResults) {
        asyncResults.forEach(result -> {
            try {
                executor.endProcess(result);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        return 1;
    }


    private <T> Callable<T> AsyncCall(T value, String ip) {
        return () -> {
            Eventloop eventloop = Eventloop.builder().withCurrentThread().build();
            eventloop.connect(new InetSocketAddress(ip, port), TIMER_DELAY_TIMEOUT, (socketChannel, e) -> {
                if (e == null) {
                    try {
                        ITcpSocket socket = TcpSocket.wrapChannel(getCurrentReactor(), socketChannel, null);
                        ByteBuf sizeBuf = ByteBufPool.allocate(2); // enough to serialize size 1024
                        sizeBuf.writeVarInt(toSend.length);
                        ByteBuf appendedBuf = ByteBufPool.append(sizeBuf, ByteBuf.wrapForReading(toSend));
                        socket.write(appendedBuf);
                        socket.close();
                        socket = null;
                    } catch (IOException ioException) {
                        throw new RuntimeException(ioException);
                    }
                }
            });
            eventloop.run();
            this.terminate();
            if (eventloop != null) {
                eventloop.breakEventloop();
                eventloop = null;
                ;
            }
            return value;
        };
    }

    private <T> Callable<T> AsyncListCall(T value, String ip, int pos) {
        return () -> {
            Eventloop eventloop = Eventloop.builder().withCurrentThread().build();
            eventloop.connect(new InetSocketAddress(ip, this.port), TIMER_DELAY_TIMEOUT, (socketChannel, e) -> {
                if (e == null) {
                    try {
                        ITcpSocket socket = TcpSocket.wrapChannel(getCurrentReactor(), socketChannel, null);
                        BinaryChannelSupplier bufsSupplier = BinaryChannelSupplier.of(ChannelSuppliers.ofSocket(socket));
                        loop(0,
                                i -> i < listToSend.size(),
                                i -> loadData(listToSend.get(i))
                                        .then(socket::write)
                                        .then(bufsSupplier::needMoreData)
                                        .then(() -> decrease(pos))
                                        .map($2 -> i + 1))
                                .whenComplete(socket::close)
                                .whenException(ex -> {
                                    throw new RuntimeException(ex);
                                });
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        LOG.info("Exception caught" + ex.toString());
                    }

                } else {
                    while (local_termination[pos].getCount() > 0) {
                        local_termination[pos].countDown();
                    }
                }
            });
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @SneakyThrows
                @Override
                public void run() {
                    while (local_termination[pos].getCount() > 0) {
                        local_termination[pos].countDown();
                    }
                }
            }, TIMER_DELAY_TIMEOUT);
            eventloop.run();
            for (int i = 0; i < local_termination.length; i++) {
                local_termination[i].await();
            }
            local_termination[pos].await();
            timer.cancel();
            timer.purge();
            this.terminate();
            if (eventloop != null) {
                eventloop.breakEventloop();
                eventloop = null;
            }
            return value;
        };
    }

    private static @NotNull Promise<ByteBuf> loadData(byte transaction_hash[]) {
        ByteBuf sizeBuf = ByteBufPool.allocate(2); // enough to serialize size 1024
        sizeBuf.writeVarInt(transaction_hash.length);
        ByteBuf appendedBuf = ByteBufPool.append(sizeBuf, ByteBuf.wrapForReading(transaction_hash));
        return Promise.of(appendedBuf);
    }

    private static @NotNull Promise<Void> decrease(int pos) {
        local_termination[pos].countDown();
        return Promise.complete();
    }

    private void terminate() {
        if (listToSend != null)
            this.listToSend.clear();
        if (this.list_ip != null)
            this.list_ip.clear();
    }
}
