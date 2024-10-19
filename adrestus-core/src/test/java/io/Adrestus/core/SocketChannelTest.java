package io.Adrestus.core;

import io.Adrestus.config.SocketConfigOptions;
import io.Adrestus.core.mapper.CustomSerializerTreeMap;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.elliptic.mapper.BigDecimalSerializer;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.network.TCPTransactionConsumer;
import io.Adrestus.network.TransactionChannelHandler;
import io.Adrestus.util.SerializationUtil;
import io.activej.bytebuf.ByteBuf;
import io.activej.bytebuf.ByteBufPool;
import io.activej.eventloop.Eventloop;
import io.activej.net.socket.tcp.AsyncTcpSocket;
import io.activej.net.socket.tcp.AsyncTcpSocketNio;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import static io.activej.eventloop.Eventloop.getCurrentEventloop;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SocketChannelTest {
    private Eventloop eventloop = Eventloop.create().withCurrentThread();
    private static AsyncTcpSocket socket;
    private static SerializationUtil<Receipt> recep;
    private static SerializationUtil<Transaction> trans;
    private static Receipt receipt;
    private static ArrayList<Receipt> receiptArrayList;
    private static ArrayList<Transaction> transactionArrayList;
    @BeforeAll
    public static void setup() {
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        trans = new SerializationUtil<Transaction>(Transaction.class, list);
        recep = new SerializationUtil<Receipt>(Receipt.class, list);
        receipt = new Receipt(1, 1);
        receiptArrayList = new ArrayList<>();
        transactionArrayList=new ArrayList<>();
    }

    @Test
    public void receipt_test() throws Exception {
        TCPTransactionConsumer<byte[]> print = x -> {
            System.out.println("Callback" + recep.decode(x).toString());
            receiptArrayList.add(recep.decode(x));
            return "";
        };

        TransactionChannelHandler transactionChannelHandler = new TransactionChannelHandler<byte[]>("localhost", SocketConfigOptions.TRANSACTION_PORT);
        transactionChannelHandler.BindServerAndReceive(print);

        System.out.println("Connecting to server at localhost (port 9922)...");
        eventloop.connect(new InetSocketAddress("localhost", SocketConfigOptions.TRANSACTION_PORT), (socketChannel, e) -> {
            if (e == null) {
                System.out.println("Connected to server, enter some text and send it by pressing 'Enter'.");
                try {
                    socket = AsyncTcpSocketNio.wrapChannel(getCurrentEventloop(), socketChannel, null);
                } catch (IOException ioException) {
                    throw new RuntimeException(ioException);
                }

                byte data[] = recep.encode(receipt, 1024);
                ByteBuf sizeBuf = ByteBufPool.allocate(2); // enough to serialize size 1024
                sizeBuf.writeVarInt(data.length);
                ByteBuf appendedBuf = ByteBufPool.append(sizeBuf, ByteBuf.wrapForReading(data));
                socket.write(appendedBuf);
                socket.close();

            } else {
                System.out.printf("Could not connect to server, make sure it is started: %s%n", e);
            }
        });
        System.out.println("send");
        eventloop.run();
        assertEquals(receiptArrayList.get(0), receipt);
        transactionChannelHandler.close();
        transactionChannelHandler = null;
    }

    @Test
    public void receipt_test2() throws Exception {
        TCPTransactionConsumer<byte[]> print = x -> {
            System.out.println("Callback" + recep.decode(x).toString());
            receiptArrayList.add(recep.decode(x));
            return "";
        };

        TransactionChannelHandler transactionChannelHandler = new TransactionChannelHandler<byte[]>("localhost", SocketConfigOptions.TRANSACTION_PORT + 1);
        transactionChannelHandler.BindServerAndReceive(print);

        System.out.println("Connecting to server at localhost (port 9922)...");
        (new Thread() {
            public void run() {
                Eventloop eventloop = Eventloop.create().withCurrentThread();
                eventloop.connect(new InetSocketAddress("localhost", SocketConfigOptions.TRANSACTION_PORT + 1), (socketChannel, e) -> {
                    if (e == null) {
                        System.out.println("Connected to server, enter some text and send it by pressing 'Enter'.");
                        try {
                            socket = AsyncTcpSocketNio.wrapChannel(getCurrentEventloop(), socketChannel, null);
                        } catch (IOException ioException) {
                            throw new RuntimeException(ioException);
                        }

                        Receipt receipt = new Receipt(1, 1);
                        byte data[] = recep.encode(receipt, 1024);
                        ByteBuf sizeBuf = ByteBufPool.allocate(2); // enough to serialize size 1024
                        sizeBuf.writeVarInt(data.length);
                        ByteBuf appendedBuf = ByteBufPool.append(sizeBuf, ByteBuf.wrapForReading(data));
                        socket.write(appendedBuf);
                        socket.close();

                    } else {
                        System.out.printf("Could not connect to server, make sure it is started: %s%n", e);
                    }
                });
                eventloop.run();
            }
        }).start();

        System.out.println("send");
        assertEquals(receiptArrayList.get(0), receipt);
        Thread.sleep(3000);
        transactionChannelHandler.close();
        transactionChannelHandler = null;
    }

    @Test
    public void Transaction_test() throws Exception {
        TCPTransactionConsumer<byte[]> print = x -> {
            System.out.println("Callback" + trans.decode(x).toString());
            transactionArrayList.add(trans.decode(x));
            return "";
        };

        TransactionChannelHandler transactionChannelHandler = new TransactionChannelHandler<byte[]>("localhost", SocketConfigOptions.TRANSACTION_PORT + 2);
        transactionChannelHandler.BindServerAndReceive(print);

        System.out.println("Connecting to server at localhost (port 9922)...");
        eventloop.connect(new InetSocketAddress("localhost", SocketConfigOptions.TRANSACTION_PORT + 2), (socketChannel, e) -> {
            if (e == null) {
                System.out.println("Connected to server, enter some text and send it by pressing 'Enter'.");
                try {
                    socket = AsyncTcpSocketNio.wrapChannel(getCurrentEventloop(), socketChannel, null);
                } catch (IOException ioException) {
                    throw new RuntimeException(ioException);
                }

                Transaction transaction = new RegularTransaction("hash2");
                byte[] data = trans.encode(transaction, 1024);
                ByteBuf sizeBuf = ByteBufPool.allocate(2); // enough to serialize size 1024
                sizeBuf.writeVarInt(data.length);
                ByteBuf appendedBuf = ByteBufPool.append(sizeBuf, ByteBuf.wrapForReading(data));
                socket.write(appendedBuf);
                socket.close();

            } else {
                System.out.printf("Could not connect to server, make sure it is started: %s%n", e);
            }
        });
        System.out.println("send");
        eventloop.run();
        assertEquals(transactionArrayList.get(0).getHash(), "hash2");
        transactionChannelHandler.close();
        transactionChannelHandler = null;
    }
}
