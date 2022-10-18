package io.Adrestus.kademlia.p2p;

import io.Adrestus.kademlia.p2p.common.NettyConnectionInfo;
import io.Adrestus.kademlia.p2p.server.KademliaNodeServer;
import io.ep2p.kademlia.node.DHTKademliaNodeAPI;
import io.ep2p.kademlia.node.DHTKademliaNodeAPIDecorator;
import io.ep2p.kademlia.node.Node;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.concurrent.Future;

public class NettyKademliaDHTNode<K extends Serializable, V extends Serializable>
        extends DHTKademliaNodeAPIDecorator<BigInteger, NettyConnectionInfo, K, V> {

    @Getter
    private final transient KademliaNodeServer<K, V> kademliaNodeServer;

    public NettyKademliaDHTNode(DHTKademliaNodeAPI<BigInteger, NettyConnectionInfo, K, V> kademliaNode, KademliaNodeServer<K, V> kademliaNodeServer) {
        super(kademliaNode);
        this.kademliaNodeServer = kademliaNodeServer;
    }

    @Override
    @SneakyThrows
    public void start() {
        super.start();
        kademliaNodeServer.run(this);
    }

    @Override
    @SneakyThrows
    public Future<Boolean> start(Node<BigInteger, NettyConnectionInfo> bootstrapNode) {
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

    @Override
    public void setLastSeen(Date date) {
        // implementation is ignored
    }

    @Override
    public Date getLastSeen() {
        return new Date();
    }
}
