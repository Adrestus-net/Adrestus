package io.Adrestus.p2p.kademlia;

import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.server.KademliaNodeServer;
import io.Adrestus.p2p.kademlia.node.DHTKademliaNodeAPI;
import io.Adrestus.p2p.kademlia.node.DHTKademliaNodeAPIDecorator;
import io.Adrestus.p2p.kademlia.node.Node;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.concurrent.Future;

public class NettyKademliaDHTNode<K extends Serializable, V extends Serializable>
        extends DHTKademliaNodeAPIDecorator<Long, NettyConnectionInfo, K, V> {

    @Getter
    private final transient KademliaNodeServer<K, V> kademliaNodeServer;

    public NettyKademliaDHTNode(DHTKademliaNodeAPI<Long, NettyConnectionInfo, K, V> kademliaNode, KademliaNodeServer<K, V> kademliaNodeServer) {
        super(kademliaNode);
        this.kademliaNodeServer = kademliaNodeServer;
    }

    @Override
    @SneakyThrows
    public void start() {
        kademliaNodeServer.run(this);
        super.start();
    }

    @Override
    @SneakyThrows
    public Future<Boolean> start(Node<Long, NettyConnectionInfo> bootstrapNode) {
        kademliaNodeServer.run(this);
        return super.start(bootstrapNode);
    }

    @Override
    @SneakyThrows
    public void stop(){
        super.stop();
        kademliaNodeServer.stop();
    }

    @Override
    @SneakyThrows
    public void stopNow(){
        super.stopNow();
        kademliaNodeServer.stop();
    }

}
