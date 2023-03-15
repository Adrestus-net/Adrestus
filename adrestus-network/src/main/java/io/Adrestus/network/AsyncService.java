package io.Adrestus.network;

import io.Adrestus.config.TransactionConfigOptions;
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
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import static io.activej.eventloop.Eventloop.getCurrentEventloop;
import static io.activej.promise.Promises.loop;

public class AsyncService<T> {
    private static Logger LOG = LoggerFactory.getLogger(AsyncService.class);
    private final ThreadAsyncExecutor executor;
    private final List<String> list_ip;
    private final int port;
    private static List<byte[]> transaction_list;
    private static Eventloop eventloop;

    private static CountDownLatch[] local_termination;

    public AsyncService(List<String> list_ip, List<byte[]> transaction_list,int port) {
        this.executor = new ThreadAsyncExecutor();
        this.list_ip = list_ip;
        this.port=port;
        this.transaction_list = transaction_list;
        this.eventloop = Eventloop.create().withCurrentThread();
        this.local_termination = new CountDownLatch[list_ip.size()];
        this.Setup();
    }

    private void Setup() {
        for (int i = 0; i < local_termination.length; i++) {
            local_termination[i] = new CountDownLatch(transaction_list.size());
        }
    }

    public List<AsyncResult<T>> startProcess(T value) {

        List<AsyncResult<T>> list = new ArrayList<>();
        for (int i = 0; i < list_ip.size(); i++) {
            AsyncResult<T> result = executor.startProcess(AsyncCall(value, list_ip.get(i), i));
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


    private <T> Callable<T> AsyncCall(T value, String ip, int pos) {
        return () -> {
            eventloop.connect(new InetSocketAddress(ip, this.port), (socketChannel, e) -> {
                if (e == null) {
                    try {
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
                                .whenException(ex -> {
                                    throw new RuntimeException(ex);
                                });
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        LOG.info("Exception caught" + ex.toString());
                    }
                } else {
                }
            });
            local_termination[pos].await();
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
}
