package io.Adrestus.kademlia.p2p.builder;

import io.ep2p.kademlia.NodeSettings;
import io.Adrestus.kademlia.p2p.client.NettyMessageSender;
import io.Adrestus.kademlia.p2p.common.NettyConnectionInfo;
import io.Adrestus.kademlia.p2p.factory.GsonFactory;
import io.Adrestus.kademlia.p2p.factory.KademliaMessageHandlerFactory;
import io.Adrestus.kademlia.p2p.factory.NettyServerInitializerFactory;
import io.Adrestus.kademlia.p2p.server.KademliaNodeServer;
import io.Adrestus.kademlia.p2p.server.filter.KademliaMainHandlerFilter;
import io.Adrestus.kademlia.p2p.server.filter.NettyKademliaServerFilterChain;
import io.ep2p.kademlia.table.Bucket;
import io.ep2p.kademlia.table.DefaultRoutingTableFactory;
import io.ep2p.kademlia.table.RoutingTableFactory;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


/* This class can later be improved to make default values of NettyKademliaDHTNodeBuilder more dynamically */
public class NettyKademliaDHTNodeDefaults {

    private interface DefaultFillerPipeline<K extends Serializable, V extends Serializable> {
        void process(NettyKademliaDHTNodeBuilder<K, V> builder);
    }

    public static <K extends Serializable, V extends Serializable> void run(NettyKademliaDHTNodeBuilder<K, V> builder){
        List<DefaultFillerPipeline<K, V>> pipeline = getPipeline();
        pipeline.forEach(pipe -> pipe.process(builder));
    }

    private static <K extends Serializable, V extends Serializable> List<DefaultFillerPipeline<K, V>> getPipeline(){
        List<DefaultFillerPipeline<K, V>> pipelines = new ArrayList<>();

        pipelines.add(builder -> {
            if (builder.getNodeSettings() == null) {
                builder.nodeSettings(NodeSettings.Default.build());
            }
        });

        pipelines.add(builder -> {
            if (builder.getRoutingTable() == null) {
                RoutingTableFactory<BigInteger, NettyConnectionInfo, Bucket<BigInteger, NettyConnectionInfo>> routingTableFactory = new DefaultRoutingTableFactory<>(builder.getNodeSettings());
                builder.routingTable(routingTableFactory.getRoutingTable(builder.getId()));
            }
        });

        pipelines.add(builder -> {
            if (builder.getGsonFactory() == null) {
                builder.gsonFactory(new GsonFactory.DefaultGsonFactory<>());
            }
        });

        pipelines.add(builder -> {
            if (builder.getMessageSender() == null) {
                builder.messageSender( new NettyMessageSender<>(builder.getGsonFactory().gson()));
            }
        });

        pipelines.add(builder -> {
            if (builder.getKademliaMessageHandlerFactory() == null) {
                NettyKademliaServerFilterChain<K, V> filterChain = new NettyKademliaServerFilterChain<>();
                filterChain.addFilter(new KademliaMainHandlerFilter<>(builder.getGsonFactory().gson()));
                builder.kademliaMessageHandlerFactory(new KademliaMessageHandlerFactory.DefaultKademliaMessageHandlerFactory<>(filterChain));
            }
        });

        pipelines.add(builder -> {
            if (builder.getNettyServerInitializerFactory() == null) {
                builder.nettyServerInitializerFactory(
                        new NettyServerInitializerFactory.DefaultNettyServerInitializerFactory<>(
                                builder.getKademliaMessageHandlerFactory()
                        )
                );
            }
        });

        pipelines.add(builder -> {
            if (builder.getKademliaNodeServer() == null){
                builder.kademliaNodeServer(
                        new KademliaNodeServer<>(
                                builder.getConnectionInfo().getHost(),
                                builder.getConnectionInfo().getPort(),
                                builder.getNettyServerInitializerFactory()
                        )
                );
            }
        });

        return pipelines;
    }

}
