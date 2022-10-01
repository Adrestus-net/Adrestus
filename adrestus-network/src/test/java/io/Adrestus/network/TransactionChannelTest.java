package io.Adrestus.network;

import io.Adrestus.config.TransactionConfigOptions;
import io.activej.bytebuf.ByteBuf;
import io.activej.csp.ChannelConsumer;
import io.activej.csp.ChannelSupplier;
import io.activej.csp.binary.BinaryChannelSupplier;
import io.activej.csp.binary.ByteBufsDecoder;
import io.activej.eventloop.Eventloop;
import io.activej.net.socket.tcp.AsyncTcpSocket;
import io.activej.net.socket.tcp.AsyncTcpSocketNio;
import io.activej.promise.Promise;
import io.activej.promise.Promises;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;

import static io.activej.bytebuf.ByteBufStrings.encodeAscii;
import static io.activej.eventloop.Eventloop.getCurrentEventloop;
import static io.activej.promise.Promises.loop;
import static io.activej.promise.Promises.mapTuple;
import static java.nio.charset.StandardCharsets.UTF_8;

public class TransactionChannelTest {
    Eventloop eventloop = Eventloop.create().withCurrentThread();
    private static final ByteBufsDecoder<byte[]> DECODER = ByteBufsDecoder.ofNullTerminatedBytes()
            .andThen(buf -> buf.asArray());
    private static final String REQUEST_MSG = "03e4c11dd892a055a201a22e915aa2e762676b8d2c9524289b2ee3b9d6a592b1";
    private static final InetSocketAddress ADDRESS = new InetSocketAddress("localhost", TransactionConfigOptions.TRANSACTION_PORT);
    private static final int ITERATIONS = 5;
    static CountDownLatch latch;
    static AsyncTcpSocket socket;
    @Test
    public void simple_test() throws InterruptedException, IOException {

        TCPTransactionConsumer<byte[]> print = x -> {
            System.out.println("Server Message:" + new String(x));
        };

        (new Thread() {
            public void run() {
                try {
                    new TransactionChannelHandler<byte[]>("localhost").BindServerAndReceive(print);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        Thread.sleep(100);
        System.out.println("Connecting to server at localhost (port 9922)...");
        eventloop.connect(new InetSocketAddress("localhost", TransactionConfigOptions.TRANSACTION_PORT), (socketChannel, e) -> {
            if (e == null) {
                System.out.println("Connected to server, enter some text and send it by pressing 'Enter'.");
                try {
                    socket = AsyncTcpSocketNio.wrapChannel(getCurrentEventloop(), socketChannel, null);
                } catch (IOException ioException) {
                    throw new RuntimeException(ioException);
                }


                BinaryChannelSupplier bufsSupplier = BinaryChannelSupplier.of(ChannelSupplier.ofSocket(socket));
                loop(0,
                        i -> i <= ITERATIONS,
                        i -> loadData(i).then(bytes -> socket.write(ByteBuf.wrapForReading(bytes))).then(()->bufsSupplier.needMoreData())
                                .map($2 -> i + 1))
                        .whenComplete(socket::close);

            } else {
                System.out.printf("Could not connect to server, make sure it is started: %s%n", e);
            }
        });
        System.out.println("send");
        eventloop.run();

    }

    private static @NotNull Promise<byte[]> loadData(int i) {
        byte[] concatBytes = ArrayUtils.addAll((String.valueOf(i)+REQUEST_MSG).getBytes(UTF_8),"\r\n".getBytes(UTF_8));
        return Promise.of(concatBytes);
    }
}
