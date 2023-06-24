package io.Adrestus.network;

import io.Adrestus.config.SocketConfigOptions;
import io.activej.bytebuf.ByteBuf;
import io.activej.bytebuf.ByteBufs;
import io.activej.common.exception.MalformedDataException;
import io.activej.csp.ChannelConsumer;
import io.activej.csp.ChannelSupplier;
import io.activej.csp.binary.BinaryChannelSupplier;
import io.activej.csp.binary.ByteBufsDecoder;
import io.activej.eventloop.Eventloop;
import io.activej.eventloop.net.SocketSettings;
import io.activej.net.SimpleServer;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static io.activej.promise.Promises.repeat;

public class ReceiptChannelHandler<T> {

    private static Logger LOG = LoggerFactory.getLogger(TransactionChannelHandler.class);
    private static final ByteBufsDecoder<ByteBuf> DECODER = ByteBufsDecoder.ofVarIntSizePrefixedBytes();
    private String IP;
    private final InetSocketAddress ADDRESS;
    private final Eventloop eventloop;
    private final ByteBufs bufs = new ByteBufs();
    private final ByteBufs tempBufs = new ByteBufs();
    private final SocketSettings settings;
    private io.activej.net.SimpleServer server;

    public ReceiptChannelHandler(String IP) {
        this.IP = IP;
        this.eventloop = Eventloop.create().withCurrentThread();
        this.ADDRESS = new InetSocketAddress(IP, SocketConfigOptions.TRANSACTION_PORT);
        this.settings = SocketSettings.create().withImplReadTimeout(Duration.ofSeconds(3)).withImplWriteTimeout(Duration.ofSeconds(3));
    }

    public ReceiptChannelHandler(String IP, int port) {
        this.IP = IP;
        this.eventloop = Eventloop.create().withCurrentThread();
        this.ADDRESS = new InetSocketAddress(IP, port);
        this.settings = SocketSettings.create().withImplReadTimeout(Duration.ofSeconds(3)).withImplWriteTimeout(Duration.ofSeconds(3));
    }


    public void BindServerAndReceive(TCPTransactionConsumer<T> callback) throws Exception {
        server = SimpleServer.create(socket ->
                {
                    BinaryChannelSupplier bufsSupplier = BinaryChannelSupplier.of(ChannelSupplier.ofSocket(socket));
                    repeat(() ->
                            bufsSupplier
                                    .decodeStream(DECODER)
                                    .peek(buf -> {
                                        try {
                                            callback.accept((T) buf.getArray());
                                        } catch (NullPointerException e) {
                                            LOG.info("Null Transaction: " + e.toString());
                                            // e.printStackTrace();
                                        } catch (Exception e) {
                                            //LOG.info("General Exception: " + e.toString());
                                            e.printStackTrace();
                                        }
                                    })
                                    .streamTo(ChannelConsumer.ofSocket(socket))
                                    .map($ -> true)
                                    .whenComplete(socket::close));
                })
                .withSocketSettings(settings)
                .withListenAddress(this.ADDRESS);
        //.withListenPort(TransactionConfigOptions.TRANSACTION_PORT);

        server.listen();
        (new Thread() {
            public void run() {
                eventloop.run();
            }
        }).start();
    }

    private <T> T doDecode(ByteBufsDecoder<T> decoder) throws MalformedDataException {
        while (true) {
            T result = decoder.tryDecode(tempBufs);
            if (result != null) {
                return result;
            }
            tempBufs.add(bufs.takeExactSize(1));
        }
    }

    @SneakyThrows
    public void close() {
        this.server.closeFuture().cancel(true);
        this.server.closeFuture().get(5, TimeUnit.SECONDS);
        this.server = null;
        this.eventloop.breakEventloop();
    }
}
