package io.Adrestus.protocol;

import io.Adrestus.config.TransactionConfigOptions;
import io.Adrestus.core.RegularTransaction;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.Transaction;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.network.TCPTransactionConsumer;
import io.Adrestus.network.TransactionChannelHandler;
import io.Adrestus.util.GetTime;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import static io.activej.eventloop.Eventloop.getCurrentEventloop;
import static io.activej.promise.Promises.loop;

public class TransactionChannelTest {
    private static final InetSocketAddress ADDRESS = new InetSocketAddress("localhost", TransactionConfigOptions.TRANSACTION_PORT);
    private static Eventloop eventloop = Eventloop.create().withCurrentThread();
    private static SerializationUtil<Transaction> serenc;
    static AsyncTcpSocket socket;

    @BeforeAll
    public static void setup() throws InterruptedException {
        TCPTransactionConsumer<byte[]> print = x -> {
            System.out.println("Server Message:" + serenc.decode(x));
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
    }

    @Test
    public void NetworkChannelTest() {


        serenc = new SerializationUtil<Transaction>(Transaction.class);
        int size = 10;

        ArrayList<Transaction> list = new ArrayList<>();
        for (int j = 0; j < size; j++) {
            Transaction transaction = new RegularTransaction();
            transaction.setFrom("ADR-ADML-SVUG-O7QD-R5IA-HWBD-XUGY-TVJA-3KAG-HLBI-G5EC");
            transaction.setTo("ADR-ADWE-NMZY-K4DI-WZBZ-ARSA-BI3N-AI3C-S744-5L5E-F4BR");
            transaction.setStatus(StatusType.PENDING);
            transaction.setTimestamp(GetTime.GetTimeStampInString());
            transaction.setZoneFrom(0);
            transaction.setZoneTo(0);
            transaction.setAmount(j);
            transaction.setAmountWithTransactionFee(transaction.getAmount() * (10.0 / 100.0));
            transaction.setNonce(1);
            byte before_hash[] = serenc.encode(transaction);
            transaction.setHash(HashUtil.sha256_bytetoString(before_hash));

            list.add(transaction);
        }
        //byte transaction_hash[] = serenc.encodeWithTerminatedBytes(transaction);
        //Transaction copy=serenc.decode(transaction_hash);
        //int a=1;
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
                        i -> i < list.size(),
                        i -> loadData(list.get(i)).then(bytes -> socket.write(ByteBuf.wrapForReading((bytes)))).then(() -> bufsSupplier.needMoreData())
                                .map($2 -> i + 1))
                        .whenComplete(socket::close);
            } else {
                System.out.printf("Could not connect to server, make sure it is started: %s%n", e);
            }
        });
        System.out.println("send");
        eventloop.run();
    }

    private static @NotNull Promise<byte[]> loadData(Transaction transaction) {
        byte transaction_hash[] = serenc.encode(transaction);
        byte[] concatBytes = ArrayUtils.addAll(transaction_hash, "\r\n".getBytes());
        return Promise.of(concatBytes);
    }
}
