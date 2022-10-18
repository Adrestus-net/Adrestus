package io.Adrestus.kademlia.p2p.server;

import io.Adrestus.kademlia.p2p.common.NettyConnectionInfo;
import io.Adrestus.kademlia.p2p.factory.NettyServerInitializerFactory;
import io.ep2p.kademlia.node.DHTKademliaNodeAPI;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigInteger;


@Getter
@Slf4j
public class KademliaNodeServer<K extends Serializable, V extends Serializable> {
    private static Logger log = LoggerFactory.getLogger(KademliaNodeServer.class);

    private final int port;
    private final String host;
    private final NettyServerInitializerFactory<K, V> nettyServerInitializerFactory;
    private boolean running = false;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture bindFuture;

    public KademliaNodeServer(String host, int port, NettyServerInitializerFactory<K, V> factory) {
        this.port = port;
        this.host = host;
        this.nettyServerInitializerFactory = factory;
    }

    public KademliaNodeServer(int port, NettyServerInitializerFactory<K, V> factory) {
        this(null, port, factory);
    }

    public synchronized void run(DHTKademliaNodeAPI<BigInteger, NettyConnectionInfo, K, V> dhtKademliaNodeAPI) throws InterruptedException {
        assert !running;

        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();

        NettyServerInitializer<K, V> nettyServerInitializer = nettyServerInitializerFactory.getKademliaNodeServerInitializerAPI();
        nettyServerInitializer.registerKademliaNode(dhtKademliaNodeAPI);

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(nettyServerInitializer)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .option(ChannelOption.SO_KEEPALIVE, false);

            ChannelFuture bind = host != null ? bootstrap.bind(host, port) : bootstrap.bind(port);
            running = true;
            this.bindFuture = bind.sync();

        } catch (InterruptedException e) {
            log.error("Kademlia Node Server interrupted", e);
            stop();
            throw e;
        }

    }

    public synchronized void stop() throws InterruptedException {
        this.running = false;
        if (bossGroup != null && workerGroup != null){
            bossGroup.shutdownGracefully().sync();
            workerGroup.shutdownGracefully().sync();
        }
        if (bindFuture != null)
            this.bindFuture.channel().closeFuture().sync();
    }

}
