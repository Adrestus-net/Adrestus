package io.Adrestus.protocol;

import io.Adrestus.config.SocketConfigOptions;
import io.Adrestus.consensus.CachedConsensusState;
import io.Adrestus.core.Resourses.*;
import io.Adrestus.network.IPFinder;
import io.Adrestus.util.SerializationFuryUtil;
import io.activej.bytebuf.ByteBuf;
import io.activej.bytebuf.ByteBufPool;
import io.activej.csp.binary.BinaryChannelSupplier;
import io.activej.csp.binary.decoder.ByteBufsDecoder;
import io.activej.csp.binary.decoder.ByteBufsDecoders;
import io.activej.csp.supplier.ChannelSuppliers;
import io.activej.eventloop.Eventloop;
import io.activej.net.SimpleServer;
import io.activej.promise.Promise;
import io.activej.reactor.net.SocketSettings;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static io.activej.promise.Promises.repeat;

public class BindServerCachedTask extends AdrestusTask {
    private static Logger LOG = LoggerFactory.getLogger(BindServerCachedTask.class);
    private static final ByteBufsDecoder<ByteBuf> DECODER = ByteBufsDecoders.ofVarIntSizePrefixedBytes();
    private final InetSocketAddress ADDRESS;

    private final SocketSettings settings;
    private SimpleServer server;
    private Eventloop eventloop;

    public BindServerCachedTask() {
        this.ADDRESS = new InetSocketAddress(IPFinder.getLocal_address(), SocketConfigOptions.CACHED_DATA_PORT);
        this.settings = SocketSettings.builder().withImplReadTimeout(Duration.ofSeconds(3)).withImplWriteTimeout(Duration.ofSeconds(3)).build();
    }

    @SneakyThrows
    @Override
    public void execute() {
        eventloop = Eventloop.builder().withCurrentThread().build();
        this.server = SimpleServer.builder(
                        eventloop,
                        socket -> {
                            BinaryChannelSupplier bufsSupplier = BinaryChannelSupplier.of(ChannelSuppliers.ofSocket(socket));
                            repeat(() ->
                                    bufsSupplier.decode(DECODER)
                                            .whenResult(x -> System.out.println(x))
                                            .then(() -> loadData())
                                            .then(socket::write)
                                            .map($ -> true))
                                    .whenComplete(socket::close);
                        })
                .withListenAddress(ADDRESS)
                .withSocketSettings(settings)
                .build();
        server.listen();
        (new Thread() {
            public void run() {
                eventloop.run();
            }
        }).start();
    }

    private static @NotNull Promise<ByteBuf> loadData() {
        final CachedNetworkData cachedNetworkData = new CachedNetworkData(
                CachedConsensusState.getInstance().isValid(),
                CachedEpochGeneration.getInstance().getEpoch_counter(),
                CachedLatestBlocks.getInstance().getCommitteeBlock(),
                CachedLatestBlocks.getInstance().getTransactionBlock(),
                CachedLeaderIndex.getInstance().getCommitteePositionLeader(),
                CachedLeaderIndex.getInstance().getTransactionPositionLeader(),
                CachedSecurityHeaders.getInstance().getSecurityHeader(),
                CachedZoneIndex.getInstance().getZoneIndex());
        byte data_bytes[] = SerializationFuryUtil.getInstance().getFury().serialize(cachedNetworkData);
        ByteBuf sizeBuf = ByteBufPool.allocate(data_bytes.length); // enough to serialize size 1024
        sizeBuf.writeVarInt(data_bytes.length);
        ByteBuf appendedBuf = ByteBufPool.append(sizeBuf, ByteBuf.wrapForReading(data_bytes));
        sizeBuf.recycle();
        return Promise.of(appendedBuf);
    }

    @SneakyThrows
    public void close() {
        this.eventloop.breakEventloop();
        this.server.closeFuture().cancel(true);
        this.server.closeFuture().get(5, TimeUnit.SECONDS);
        this.server = null;
    }
}
