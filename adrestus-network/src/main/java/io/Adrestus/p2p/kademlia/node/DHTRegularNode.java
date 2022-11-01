package io.Adrestus.p2p.kademlia.node;

import io.Adrestus.config.NodeSettings;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.p2p.kademlia.NettyKademliaDHTNode;
import io.Adrestus.p2p.kademlia.builder.NettyKademliaDHTNodeBuilder;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.protocol.handler.MessageHandler;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.p2p.kademlia.repository.KademliaRepository;
import io.Adrestus.p2p.kademlia.repository.KademliaRepositoryImp;
import io.Adrestus.p2p.kademlia.table.Bucket;
import io.Adrestus.p2p.kademlia.table.RoutingTable;
import io.Adrestus.p2p.kademlia.table.RoutingTableFactory;
import io.Adrestus.p2p.kademlia.util.BoundedHashUtil;
import io.Adrestus.p2p.kademlia.util.LoggerKademlia;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DHTRegularNode {

    private final NettyConnectionInfo nettyConnectionInfo;
    private final KeyHashGenerator<Long, String> keyHashGenerator;
    private final KademliaRepository repository;

    private Long ID;
    private MessageHandler<Long, NettyConnectionInfo> handler;
    private NettyKademliaDHTNode<String, KademliaData> regular_node;

    public DHTRegularNode(NettyConnectionInfo nettyConnectionInfo) {
        LoggerKademlia.setLevelOFF();
        this.nettyConnectionInfo = nettyConnectionInfo;
        this.keyHashGenerator = key -> new BoundedHashUtil(NodeSettings.getInstance().getIdentifierSize()).hash(new Long(HashUtil.convertIPtoHex(key, 2)), Long.class);
        this.repository = new KademliaRepositoryImp();
    }

    public DHTRegularNode(NettyConnectionInfo nettyConnectionInfo, Long ID) {
        LoggerKademlia.setLevelOFF();
        this.ID = ID;
        this.nettyConnectionInfo = nettyConnectionInfo;
        this.keyHashGenerator = key -> new BoundedHashUtil(NodeSettings.getInstance().getIdentifierSize()).hash(new Long(HashUtil.convertIPtoHex(key, 2)), Long.class);
        this.repository = new KademliaRepositoryImp();
    }

    public DHTRegularNode(NettyConnectionInfo nettyConnectionInfo, Long ID, KeyHashGenerator<Long, String> keyHashGenerator) {
        LoggerKademlia.setLevelOFF();
        this.ID = ID;
        this.nettyConnectionInfo = nettyConnectionInfo;
        this.keyHashGenerator = keyHashGenerator;
        this.repository = new KademliaRepositoryImp();
    }


    public void start(DHTBootstrapNode bootstrap) {
        regular_node = new NettyKademliaDHTNodeBuilder<>(
                this.ID,
                this.nettyConnectionInfo,
                this.repository,
                keyHashGenerator
        ).withNodeSettings(NodeSettings.getInstance()).build();
        try {
            System.out.println("Bootstrapped? " + regular_node.start(bootstrap.getBootStrapNode()).get(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    public void start(DHTBootstrapNode bootstrap, RoutingTable<Long, NettyConnectionInfo, Bucket<Long, NettyConnectionInfo>> routingTable) {
        regular_node = new NettyKademliaDHTNodeBuilder<>(
                this.ID,
                this.nettyConnectionInfo,
                this.repository,
                keyHashGenerator
        ).withNodeSettings(NodeSettings.getInstance()).routingTable(routingTable).build();
        try {
            System.out.println("Bootstrapped? " + regular_node.start(bootstrap.getBootStrapNode()).get(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }


    public NettyConnectionInfo getNettyConnectionInfo() {
        return nettyConnectionInfo;
    }

    public KeyHashGenerator<Long, String> getKeyHashGenerator() {
        return keyHashGenerator;
    }

    public KademliaRepository getRepository() {
        return repository;
    }

    public MessageHandler<Long, NettyConnectionInfo> getHandler() {
        return handler;
    }

    public void setHandler(MessageHandler<Long, NettyConnectionInfo> handler) {
        this.handler = handler;
    }

    public NettyKademliaDHTNode<String, KademliaData> getRegular_node() {
        return regular_node;
    }

    public void setRegular_node(NettyKademliaDHTNode<String, KademliaData> regular_node) {
        this.regular_node = regular_node;
    }


    public void close() {
        regular_node.stopNow();
    }
}
