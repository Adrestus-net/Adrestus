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
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

import static io.activej.eventloop.Eventloop.getCurrentEventloop;
import static io.activej.promise.Promises.loop;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ReceiptChannelTest {
    Eventloop eventloop = Eventloop.create().withCurrentThread();
    private static final ByteBufsDecoder<byte[]> DECODER = ByteBufsDecoder.ofNullTerminatedBytes().andThen(buf -> buf.asArray());
    private static final String REQUEST_MSG = "03e4c11dd892a055a201a22e915aa2e762676b8d2c9524289b2ee3b9d6a592b1";
    private static final InetSocketAddress ADDRESS = new InetSocketAddress("localhost", SocketConfigOptions.TRANSACTION_PORT);
    private static final int ITERATIONS = 10;
    static CountDownLatch latch;
    static AsyncTcpSocket socket;

    @Test
    public void simple_test() throws Exception {

        TCPTransactionConsumer<byte[]> print = x -> {
            System.out.println("Callback" + new String(x));
        };

        ReceiptChannelHandler transactionChannelHandler = new ReceiptChannelHandler<byte[]>("localhost");
        transactionChannelHandler.BindServerAndReceive(print);

        Thread.sleep(2000);
        System.out.println("Connecting to server at localhost (port 9922)...");
        eventloop.connect(new InetSocketAddress("localhost", SocketConfigOptions.TRANSACTION_PORT), (socketChannel, e) -> {
            if (e == null) {
                System.out.println("Connected to server, enter some text and send it by pressing 'Enter'.");
                try {
                    socket = AsyncTcpSocketNio.wrapChannel(getCurrentEventloop(), socketChannel, null);
                } catch (IOException ioException) {
                    throw new RuntimeException(ioException);
                }


                latch = new CountDownLatch(ITERATIONS);
                BinaryChannelSupplier bufsSupplier = BinaryChannelSupplier.of(ChannelSupplier.ofSocket(socket));
                loop(0,
                        i -> i <= ITERATIONS,
                        i -> loadData(i).then(bytes -> socket.write(bytes)).then(() -> bufsSupplier.needMoreData())
                                .map($2 -> i + 1))
                        .whenComplete(socket::close);
                // eventloop.execute(() -> socket.close());
                //socket.close();

            } else {
                System.out.printf("Could not connect to server, make sure it is started: %s%n", e);
            }
        });
        System.out.println("send");
        eventloop.run();
        transactionChannelHandler.close();
        transactionChannelHandler = null;

    }

    @Test
    public void simple_test2() throws Exception {

        TCPTransactionConsumer<byte[]> print = x -> {
            System.out.println("Callback 2: " + new String(x));
        };

        ReceiptChannelHandler transactionChannelHandler = new ReceiptChannelHandler<byte[]>("localhost", SocketConfigOptions.TRANSACTION_PORT + 1);
        transactionChannelHandler.BindServerAndReceive(print);

        Thread.sleep(2000);
        System.out.println("Connecting to server at localhost (port 9922)...");
        eventloop.connect(new InetSocketAddress("localhost", SocketConfigOptions.TRANSACTION_PORT + 1), (socketChannel, e) -> {
            if (e == null) {
                try {
                    socket = AsyncTcpSocketNio.wrapChannel(getCurrentEventloop(), socketChannel, null);
                } catch (IOException ioException) {
                    throw new RuntimeException(ioException);
                }
                socket.write(ByteBuf.wrapForReading(ArrayUtils.addAll(REQUEST_MSG.getBytes(UTF_8), "\r\n".getBytes(UTF_8))));
                socket.close();

            } else {
                System.out.printf("Could not connect to server, make sure it is started: %s%n", e);
            }
        });
        System.out.println("send");
        eventloop.run();
        transactionChannelHandler.close();
        transactionChannelHandler = null;

    }

    private static @NotNull Promise<ByteBuf> loadData(int i) {
        byte transaction_hash[] = ArrayUtils.addAll((String.valueOf(i) + REQUEST_MSG).getBytes(UTF_8));
        ByteBuf sizeBuf = ByteBufPool.allocate(2); // enough to serialize size 1024
        sizeBuf.writeVarInt(transaction_hash.length);
        ByteBuf appendedBuf = ByteBufPool.append(sizeBuf, ByteBuf.wrapForReading(transaction_hash));
        return Promise.of(appendedBuf);
    }

    private static @NotNull Promise<String> count(byte[] bytes) {
        Promise<Void> first = socket.write(ByteBuf.wrapForReading(bytes));
        //Promise<Integer> secondNumber = Promises.delay(100, 10);
        //Promise<String> strPromise = first.combine(secondNumber, Integer::sum);
        return Promise.of("");
    }
}
