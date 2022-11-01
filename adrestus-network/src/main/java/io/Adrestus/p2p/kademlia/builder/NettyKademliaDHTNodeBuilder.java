package io.Adrestus.p2p.kademlia.builder;

import io.Adrestus.p2p.kademlia.NettyKademliaDHTNode;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.factory.GsonFactory;
import io.Adrestus.p2p.kademlia.factory.KademliaMessageHandlerFactory;
import io.Adrestus.p2p.kademlia.factory.NettyServerInitializerFactory;
import io.Adrestus.p2p.kademlia.server.KademliaNodeServer;
import io.Adrestus.config.NodeSettings;
import io.Adrestus.p2p.kademlia.connection.MessageSender;
import io.Adrestus.p2p.kademlia.node.DHTKademliaNode;
import io.Adrestus.p2p.kademlia.node.DHTKademliaNodeAPI;
import io.Adrestus.p2p.kademlia.node.KeyHashGenerator;
import io.Adrestus.p2p.kademlia.repository.KademliaRepository;
import io.Adrestus.p2p.kademlia.table.Bucket;
import io.Adrestus.p2p.kademlia.table.RoutingTable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class NettyKademliaDHTNodeBuilder<K extends Serializable, V extends Serializable> {
    private final Long id;
    private final NettyConnectionInfo connectionInfo;
    private RoutingTable<Long, NettyConnectionInfo, Bucket<Long, NettyConnectionInfo>> routingTable;
    private MessageSender<Long, NettyConnectionInfo> messageSender;
    private NodeSettings nodeSettings;
    private GsonFactory gsonFactory;
    private final KademliaRepository<K, V> repository;
    private final KeyHashGenerator<Long, K> keyHashGenerator;
    private KademliaNodeServer<K, V> kademliaNodeServer;
    private KademliaMessageHandlerFactory<K, V> kademliaMessageHandlerFactory;
    private NettyServerInitializerFactory<K, V> nettyServerInitializerFactory;

    protected List<String> required = new ArrayList<>();

    public NettyKademliaDHTNodeBuilder(Long id, NettyConnectionInfo connectionInfo, KademliaRepository<K, V> repository, KeyHashGenerator<Long, K> keyHashGenerator) {
        this.id = id;
        this.connectionInfo = connectionInfo;
        this.repository = repository;
        this.keyHashGenerator = keyHashGenerator;
    }

    public NettyKademliaDHTNodeBuilder<K, V> routingTable(RoutingTable<Long, NettyConnectionInfo, Bucket<Long, NettyConnectionInfo>> routingTable) {
        this.routingTable = routingTable;
        return this;
    }

    public NettyKademliaDHTNodeBuilder<K, V> messageSender(MessageSender<Long, NettyConnectionInfo> messageSender) {
        this.messageSender = messageSender;
        return this;
    }

    public NettyKademliaDHTNodeBuilder<K, V> nodeSettings(NodeSettings nodeSettings) {
        this.nodeSettings = nodeSettings;
        return this;
    }

    public NettyKademliaDHTNodeBuilder<K, V> kademliaNodeServer(KademliaNodeServer<K, V> kademliaNodeServer) {
        this.kademliaNodeServer = kademliaNodeServer;
        return this;
    }

    public NettyKademliaDHTNodeBuilder<K, V> kademliaMessageHandlerFactory(KademliaMessageHandlerFactory<K, V> kademliaMessageHandlerFactory) {
        this.kademliaMessageHandlerFactory = kademliaMessageHandlerFactory;
        return this;
    }

    public NettyKademliaDHTNodeBuilder<K, V> nettyServerInitializerFactory(NettyServerInitializerFactory<K, V> nettyServerInitializerFactory) {
        this.nettyServerInitializerFactory = nettyServerInitializerFactory;
        return this;
    }

    public NettyKademliaDHTNodeBuilder<K, V> gsonFactory(GsonFactory gsonFactory) {
        this.gsonFactory = gsonFactory;
        return this;
    }

    public NettyKademliaDHTNodeBuilder<K,V> withNodeSettings(NodeSettings nodeSettings){
        this.nodeSettings=nodeSettings;
        return this;
    }

    public NettyKademliaDHTNode<K, V> build() {
        fillDefaults();

        DHTKademliaNodeAPI<Long, NettyConnectionInfo, K, V> kademliaNode = new DHTKademliaNode<>(
                this.id,
                this.connectionInfo,
                this.routingTable,
                this.messageSender,
                this.nodeSettings, this.repository, this.keyHashGenerator
        );

        return new NettyKademliaDHTNode<>(kademliaNode, this.kademliaNodeServer);
    }

    protected void fillDefaults() {
        NettyKademliaDHTNodeDefaults.run(this);
    }


}
