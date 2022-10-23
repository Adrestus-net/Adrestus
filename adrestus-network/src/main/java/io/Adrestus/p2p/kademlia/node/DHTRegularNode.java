package io.Adrestus.p2p.kademlia.node;

import io.Adrestus.config.KademliaConfiguration;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.p2p.kademlia.NettyKademliaDHTNode;
import io.Adrestus.config.NodeSettings;
import io.Adrestus.p2p.kademlia.builder.NettyKademliaDHTNodeBuilder;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.protocol.handler.MessageHandler;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.p2p.kademlia.repository.KademliaRepository;
import io.Adrestus.p2p.kademlia.repository.KademliaRepositoryImp;
import io.Adrestus.p2p.kademlia.util.BoundedHashUtil;
import io.Adrestus.p2p.kademlia.util.LoggerKademlia;
import lombok.SneakyThrows;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DHTRegularNode {

    private final NettyConnectionInfo nettyConnectionInfo;
    private final KeyHashGenerator<BigInteger, String> keyHashGenerator;
    private final KademliaRepository repository;

    private BigInteger ID;
    private MessageHandler<BigInteger, NettyConnectionInfo> handler;
    private NettyKademliaDHTNode<String, KademliaData> regular_node;

    public DHTRegularNode(NettyConnectionInfo nettyConnectionInfo) {
        LoggerKademlia.setLevelOFF();
        this.nettyConnectionInfo = nettyConnectionInfo;
        this.keyHashGenerator = key -> new BoundedHashUtil(NodeSettings.getInstance().getIdentifierSize()).hash(new BigInteger(HashUtil.convertIPtoHex(key,2)), BigInteger.class);
        this.repository = new KademliaRepositoryImp();
    }

    public DHTRegularNode(NettyConnectionInfo nettyConnectionInfo,BigInteger ID) {
        LoggerKademlia.setLevelOFF();
        this.ID=ID;
        this.nettyConnectionInfo = nettyConnectionInfo;
        this.keyHashGenerator = key -> new BoundedHashUtil(NodeSettings.getInstance().getIdentifierSize()).hash(new BigInteger(HashUtil.convertIPtoHex(key,2)), BigInteger.class);
        this.repository = new KademliaRepositoryImp();
    }

    public DHTRegularNode(NettyConnectionInfo nettyConnectionInfo,BigInteger ID,KeyHashGenerator<BigInteger, String> keyHashGenerator) {
        LoggerKademlia.setLevelOFF();
        this.ID=ID;
        this.nettyConnectionInfo = nettyConnectionInfo;
        this.keyHashGenerator =keyHashGenerator;
        this.repository = new KademliaRepositoryImp();
    }


    public BigInteger nextRandomBigInteger() {
        BigInteger n = BigInteger.valueOf(1000);
        Random rand = new Random();
        BigInteger result = new BigInteger(n.bitLength(), rand);
        while (result.compareTo(n) >= 0) {
            result = new BigInteger(n.bitLength(), rand);
        }
        return result;
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

    @SneakyThrows
    public void start() {
        regular_node = new NettyKademliaDHTNodeBuilder<>(
                this.nextRandomBigInteger(),
                this.nettyConnectionInfo,
                this.repository,
                keyHashGenerator
        ).withNodeSettings(NodeSettings.getInstance()).build();
        regular_node.start();
    }


    public NettyConnectionInfo getNettyConnectionInfo() {
        return nettyConnectionInfo;
    }

    public KeyHashGenerator<BigInteger, String> getKeyHashGenerator() {
        return keyHashGenerator;
    }

    public KademliaRepository getRepository() {
        return repository;
    }

    public MessageHandler<BigInteger, NettyConnectionInfo> getHandler() {
        return handler;
    }

    public void setHandler(MessageHandler<BigInteger, NettyConnectionInfo> handler) {
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
