package io.Adrestus.p2p.kademlia.node;


import io.Adrestus.config.KademliaConfiguration;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.p2p.kademlia.NettyKademliaDHTNode;
import io.Adrestus.config.NodeSettings;
import io.Adrestus.p2p.kademlia.builder.NettyKademliaDHTNodeBuilder;
import io.Adrestus.p2p.kademlia.client.NettyMessageSender;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.protocol.MessageType;
import io.Adrestus.p2p.kademlia.protocol.handler.MessageHandler;
import io.Adrestus.p2p.kademlia.protocol.handler.PongMessageHandler;
import io.Adrestus.p2p.kademlia.protocol.message.KademliaMessage;
import io.Adrestus.p2p.kademlia.protocol.message.PongKademliaMessage;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.p2p.kademlia.repository.KademliaRepository;
import io.Adrestus.p2p.kademlia.repository.KademliaRepositoryImp;
import io.Adrestus.p2p.kademlia.util.BoundedHashUtil;
import io.Adrestus.p2p.kademlia.util.LoggerKademlia;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class DHTBootstrapNode {

    private final NettyConnectionInfo nettyConnectionInfo;
    private final KeyHashGenerator<BigInteger, String> keyHashGenerator;
    private final KademliaRepository repository;
    private BigInteger ID;
    private MessageHandler<BigInteger, NettyConnectionInfo> handler;
    private NettyKademliaDHTNode<String, KademliaData> bootStrapNode;

    public DHTBootstrapNode(NettyConnectionInfo nettyConnectionInfo) {
        LoggerKademlia.setLevelOFF();
        this.nettyConnectionInfo = nettyConnectionInfo;
        this.keyHashGenerator = key -> new BoundedHashUtil(NodeSettings.getInstance().getIdentifierSize()).hash(new BigInteger(HashUtil.convertIPtoHex(key,2)), BigInteger.class);
        this.repository = new KademliaRepositoryImp();
        //this.InitHandler();
    }

    public DHTBootstrapNode(NettyConnectionInfo nettyConnectionInfo,BigInteger ID) {
        LoggerKademlia.setLevelOFF();
        this.ID=ID;
        this.nettyConnectionInfo = nettyConnectionInfo;
        this.keyHashGenerator = key -> new BoundedHashUtil(NodeSettings.getInstance().getIdentifierSize()).hash(new BigInteger(HashUtil.convertIPtoHex(key,2)), BigInteger.class);
        this.repository = new KademliaRepositoryImp();
        this.InitHandler();
    }

    public DHTBootstrapNode(NettyConnectionInfo nettyConnectionInfo,BigInteger ID,KeyHashGenerator<BigInteger, String> keyHashGenerator) {
        LoggerKademlia.setLevelOFF();
        this.ID=ID;
        this.nettyConnectionInfo = nettyConnectionInfo;
        this.keyHashGenerator = keyHashGenerator;
        this.repository = new KademliaRepositoryImp();
        this.InitHandler();
    }


    private void InitHandler(){
        handler = new PongMessageHandler<BigInteger, NettyConnectionInfo>() {
            @Override
            public <I extends KademliaMessage<BigInteger, NettyConnectionInfo, ?>, O extends KademliaMessage<BigInteger, NettyConnectionInfo, ?>> O doHandle(KademliaNodeAPI<BigInteger, NettyConnectionInfo> kademliaNode, I message) {
                kademliaNode.getRoutingTable().getBuckets().stream().filter(val->val!=null).forEach(x -> {
                   x.getNodeIds().stream().forEach(y-> {
                       if (y != null && !y.equals(BigInteger.ZERO)) {
                           System.out.println("esd"+y.toString());
                       }
                   });
                });
                return (O) doHandle(kademliaNode, (PongKademliaMessage<BigInteger, NettyConnectionInfo>) message);
            }
        };
    }


    public BigInteger nextRandomBigInteger() {
        BigInteger n = BigInteger.valueOf(NodeSettings.getInstance().getIdentifierSize());
        Random rand = new Random();
        BigInteger result = new BigInteger(n.bitLength(), rand);
        while (result.compareTo(n) >= 0) {
            result = new BigInteger(n.bitLength(), rand);
        }
        return result;
    }

    public void start() {
        bootStrapNode = new NettyKademliaDHTNodeBuilder<>(
                this.ID,
                this.nettyConnectionInfo,
                this.repository,
                keyHashGenerator
        ).withNodeSettings(NodeSettings.getInstance()).build();
        bootStrapNode.registerMessageHandler(MessageType.PONG, handler);
        bootStrapNode.start();
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

    public NettyKademliaDHTNode<String, KademliaData> getBootStrapNode() {
        return bootStrapNode;
    }

    public void setBootStrapNode(NettyKademliaDHTNode<String, KademliaData> bootStrapNode) {
        this.bootStrapNode = bootStrapNode;
    }

    public void close() {
        this.bootStrapNode.stopNow();
    }
}
