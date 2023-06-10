package io.Adrestus.network;

import io.Adrestus.config.SocketConfigOptions;
import io.activej.bytebuf.ByteBuf;
import io.activej.bytebuf.ByteBufPool;
import io.activej.csp.ChannelSupplier;
import io.activej.csp.binary.BinaryChannelSupplier;
import io.activej.csp.binary.ByteBufsDecoder;
import io.activej.eventloop.Eventloop;
import io.activej.net.socket.tcp.AsyncTcpSocket;
import io.activej.net.socket.tcp.AsyncTcpSocketNio;
import io.activej.promise.Promise;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.activej.eventloop.Eventloop.getCurrentEventloop;
import static io.activej.promise.Promises.loop;

public class AsyncServiceNetworkData<T> {
    private static Logger LOG = LoggerFactory.getLogger(AsyncServiceNetworkData.class);

    private static final int TIMER_DELAY_TIMEOUT = 3000;
    private static final int EVENTLOOP_TIMER_DELAY_TIMEOUT = 1000;
    private static final ByteBufsDecoder<ByteBuf> DECODER = ByteBufsDecoder.ofVarIntSizePrefixedBytes();
    private List<String> list_ip;

    private List<byte[]> data_bytes;
    private final ThreadAsyncExecutor executor;

    private static CountDownLatch[] local_termination;

    public AsyncServiceNetworkData(List<String> list_ip) {
        this.executor = new ThreadAsyncExecutor();
        this.list_ip = list_ip;
        this.local_termination = new CountDownLatch[list_ip.size()];
        this.data_bytes = Collections.synchronizedList(new ArrayList<byte[]>());
        this.Setup();
    }

    private void Setup() {
        for (int i = 0; i < local_termination.length; i++) {
            local_termination[i] = new CountDownLatch(1);
        }
    }

    @SneakyThrows
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
            Timer receivetimer = new Timer();
            Eventloop eventloop = Eventloop.create().withCurrentThread();
            Eventloop finalEventloop=eventloop;
            eventloop.connect(new InetSocketAddress(ip,SocketConfigOptions.CACHED_DATA_PORT),EVENTLOOP_TIMER_DELAY_TIMEOUT, (socketChannel, e) -> {
                if (e == null) {
                    try {
                        AsyncTcpSocket socket = AsyncTcpSocketNio.wrapChannel(getCurrentEventloop(), socketChannel, null);
                        BinaryChannelSupplier bufsSupplier = BinaryChannelSupplier.of(ChannelSupplier.ofSocket(socket));
                        loop(0,
                                i -> i < 1,
                                i -> loadData()
                                        .then(socket::write)
                                        .then(() -> bufsSupplier.decode(DECODER))
                                        .whenResult(val -> data_bytes.add(val.getArray()))
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

                    receivetimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            finalEventloop.breakEventloop();
                            while (local_termination[pos].getCount() > 0) {
                                local_termination[pos].countDown();
                            }
                        }
                    }, TIMER_DELAY_TIMEOUT);

                } else {
                    while (local_termination[pos].getCount() > 0) {
                        local_termination[pos].countDown();
                    }
                }
            });
            eventloop.run();
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
            for (int i = 0; i < local_termination.length; i++) {
                local_termination[i].await();
            }
            local_termination[pos].await();
            receivetimer.cancel();
            receivetimer.purge();
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

    private static @NotNull Promise<Void> decrease(int pos) {
        local_termination[pos].countDown();
        return Promise.complete();
    }

    private static @NotNull Promise<ByteBuf> loadData() {
        byte transaction_hash[] = new String("").getBytes(StandardCharsets.UTF_8);
        ByteBuf sizeBuf = ByteBufPool.allocate(2); // enough to serialize size 1024
        sizeBuf.writeVarInt(transaction_hash.length);
        ByteBuf appendedBuf = ByteBufPool.append(sizeBuf, ByteBuf.wrapForReading(transaction_hash));
        sizeBuf.recycle();
        return Promise.of(appendedBuf);
    }

    public byte[] getResult() {
        ArrayList<String> toCompare = new ArrayList<String>();
        data_bytes.stream().forEach(val -> toCompare.add(Hex.toHexString(val)));
        Map<String, Long> collect = toCompare.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        byte[] result = Hex.decode(collect.keySet().stream().findFirst().get());
        return result;
    }

    private void terminate() {
        if (this.list_ip != null)
            this.list_ip.clear();
    }

}
