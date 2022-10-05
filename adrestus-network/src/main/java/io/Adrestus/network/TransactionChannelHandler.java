package io.Adrestus.network;

import io.Adrestus.config.TransactionConfigOptions;
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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;

import static io.activej.bytebuf.ByteBufStrings.CR;
import static io.activej.bytebuf.ByteBufStrings.LF;
import static io.activej.promise.Promises.repeat;

public class TransactionChannelHandler<T> {
    private static final Eventloop eventloop = Eventloop.create().withCurrentThread();
    private static final ByteBufsDecoder<ByteBuf> DECODER = ByteBufsDecoder.ofCrlfTerminatedBytes(2048);
    private String IP;
    private final InetSocketAddress ADDRESS;
    private static final String RESPONSE_MSG = "PONG";
    private static final byte[] CRLF = {CR, LF};
    private final ByteBufs bufs = new ByteBufs();
    private final ByteBufs tempBufs = new ByteBufs();
    private final SocketSettings settings;

    public TransactionChannelHandler(String IP) {
        this.IP = IP;
        this.ADDRESS = new InetSocketAddress(IP, TransactionConfigOptions.TRANSACTION_PORT);
        this.settings = SocketSettings.create().withImplReadTimeout(Duration.ofSeconds(3)).withImplWriteTimeout(Duration.ofSeconds(3));
    }

    public TransactionChannelHandler() throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("google.com", 80));
        this.IP = socket.getLocalAddress().getHostAddress();
        this.ADDRESS = new InetSocketAddress(IP, TransactionConfigOptions.TRANSACTION_PORT);
        this.settings = SocketSettings.create().withImplReadTimeout(Duration.ofSeconds(3)).withImplWriteTimeout(Duration.ofSeconds(3));
    }

    public void BindServerAndReceive(TCPTransactionConsumer<T> callback) throws IOException {
        SimpleServer server = SimpleServer.create(socket ->
                {
                    BinaryChannelSupplier bufsSupplier = BinaryChannelSupplier.of(ChannelSupplier.ofSocket(socket));
                    repeat(() ->
                            bufsSupplier
                                    .decodeStream(DECODER)
                                    .peek(buf -> callback.accept((T) buf.asArray()))
                                    .streamTo(ChannelConsumer.ofSocket(socket))
                                    .map($ -> true)
                                    .whenComplete(socket::close));
                })
                .withSocketSettings(settings)
                .withListenPort(TransactionConfigOptions.TRANSACTION_PORT);

        server.listen();
        eventloop.run();
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
}
