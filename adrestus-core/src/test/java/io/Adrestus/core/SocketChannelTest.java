package io.Adrestus.core;

import io.Adrestus.config.TransactionConfigOptions;
import io.Adrestus.network.TCPTransactionConsumer;
import io.Adrestus.network.TransactionChannelHandler;
import io.Adrestus.util.SerializationUtil;
import io.activej.bytebuf.ByteBuf;
import io.activej.eventloop.Eventloop;
import io.activej.net.socket.tcp.AsyncTcpSocket;
import io.activej.net.socket.tcp.AsyncTcpSocketNio;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;

import static io.activej.eventloop.Eventloop.getCurrentEventloop;
import static java.nio.charset.StandardCharsets.UTF_8;

public class SocketChannelTest {
    Eventloop eventloop = Eventloop.create().withCurrentThread();
    static AsyncTcpSocket socket;
    SerializationUtil<Receipt> recep = new SerializationUtil<Receipt>(Receipt.class);
    SerializationUtil<Transaction> trans = new SerializationUtil<Transaction>(Transaction.class);

    @Test
    public void receipt_test() throws Exception {
        TCPTransactionConsumer<byte[]> print = x -> {
            System.out.println("Callback" + recep.decode(x).toString());
        };

        TransactionChannelHandler transactionChannelHandler = new TransactionChannelHandler<byte[]>("localhost");
        transactionChannelHandler.BindServerAndReceive(print);

        System.out.println("Connecting to server at localhost (port 9922)...");
        eventloop.connect(new InetSocketAddress("localhost", TransactionConfigOptions.TRANSACTION_PORT), (socketChannel, e) -> {
            if (e == null) {
                System.out.println("Connected to server, enter some text and send it by pressing 'Enter'.");
                try {
                    socket = AsyncTcpSocketNio.wrapChannel(getCurrentEventloop(), socketChannel, null);
                } catch (IOException ioException) {
                    throw new RuntimeException(ioException);
                }

                Receipt receipt = new Receipt(1, 1, new RegularTransaction("hash1"));
                byte[] data = recep.encode(receipt);
                socket.write(ByteBuf.wrapForReading(ArrayUtils.addAll(data, "\r\n".getBytes(UTF_8))));
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

    @Test
    public void receipt_test2() throws Exception {
        TCPTransactionConsumer<byte[]> print = x -> {
            System.out.println("Callback" + recep.decode(x).toString());
        };

        TransactionChannelHandler transactionChannelHandler = new TransactionChannelHandler<byte[]>("localhost");
        transactionChannelHandler.BindServerAndReceive(print);

        System.out.println("Connecting to server at localhost (port 9922)...");
        (new Thread() {
            public void run() {
                Eventloop eventloop = Eventloop.create().withCurrentThread();
                eventloop.connect(new InetSocketAddress("localhost", TransactionConfigOptions.TRANSACTION_PORT), (socketChannel, e) -> {
                    if (e == null) {
                        System.out.println("Connected to server, enter some text and send it by pressing 'Enter'.");
                        try {
                            socket = AsyncTcpSocketNio.wrapChannel(getCurrentEventloop(), socketChannel, null);
                        } catch (IOException ioException) {
                            throw new RuntimeException(ioException);
                        }

                        Receipt receipt = new Receipt(1, 1, new RegularTransaction("hash1"));
                        byte[] data = recep.encode(receipt);
                        socket.write(ByteBuf.wrapForReading(ArrayUtils.addAll(data, "\r\n".getBytes(UTF_8))));
                        socket.close();

                    } else {
                        System.out.printf("Could not connect to server, make sure it is started: %s%n", e);
                    }
                });
                eventloop.run();
            }
        }).start();

        System.out.println("send");

        Thread.sleep(3000);
        transactionChannelHandler.close();
        transactionChannelHandler = null;
    }

    @Test
    public void Transaction_test() throws Exception {
        TCPTransactionConsumer<byte[]> print = x -> {
            System.out.println("Callback" + trans.decode(x).toString());
        };

        TransactionChannelHandler transactionChannelHandler = new TransactionChannelHandler<byte[]>("localhost");
        transactionChannelHandler.BindServerAndReceive(print);

        System.out.println("Connecting to server at localhost (port 9922)...");
        eventloop.connect(new InetSocketAddress("localhost", TransactionConfigOptions.TRANSACTION_PORT), (socketChannel, e) -> {
            if (e == null) {
                System.out.println("Connected to server, enter some text and send it by pressing 'Enter'.");
                try {
                    socket = AsyncTcpSocketNio.wrapChannel(getCurrentEventloop(), socketChannel, null);
                } catch (IOException ioException) {
                    throw new RuntimeException(ioException);
                }

                Transaction transaction = new RegularTransaction("hash2");
                byte[] data = trans.encode(transaction);
                socket.write(ByteBuf.wrapForReading(ArrayUtils.addAll(data, "\r\n".getBytes(UTF_8))));
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
}
