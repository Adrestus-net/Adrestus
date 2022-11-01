package io.Adrestus.p2p.kademlia.node;


import io.Adrestus.crypto.HashUtil;
import io.Adrestus.p2p.kademlia.NettyKademliaDHTNode;
import io.Adrestus.config.NodeSettings;
import io.Adrestus.p2p.kademlia.builder.NettyKademliaDHTNodeBuilder;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.protocol.handler.MessageHandler;
import io.Adrestus.p2p.kademlia.protocol.handler.PongMessageHandler;
import io.Adrestus.p2p.kademlia.protocol.message.KademliaMessage;
import io.Adrestus.p2p.kademlia.protocol.message.PongKademliaMessage;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.p2p.kademlia.repository.KademliaRepository;
import io.Adrestus.p2p.kademlia.repository.KademliaRepositoryImp;
import io.Adrestus.p2p.kademlia.util.BoundedHashUtil;
import io.Adrestus.p2p.kademlia.util.LoggerKademlia;

import java.util.Random;

public class DHTBootstrapNode {

    private final NettyConnectionInfo nettyConnectionInfo;
    private final KeyHashGenerator<Long, String> keyHashGenerator;
    private final KademliaRepository repository;
    private Long ID;
    private MessageHandler<Long, NettyConnectionInfo> handler;
    private NettyKademliaDHTNode<String, KademliaData> bootStrapNode;

    public DHTBootstrapNode(NettyConnectionInfo nettyConnectionInfo) {
        LoggerKademlia.setLevelOFF();
        this.nettyConnectionInfo = nettyConnectionInfo;
        this.keyHashGenerator = key -> new BoundedHashUtil(NodeSettings.getInstance().getIdentifierSize()).hash(new Long(HashUtil.convertIPtoHex(key,2)), Long.class);
        this.repository = new KademliaRepositoryImp();
        this.InitHandler();
    }

    public DHTBootstrapNode(NettyConnectionInfo nettyConnectionInfo,Long ID) {
        LoggerKademlia.setLevelOFF();
        this.ID=ID;
        this.nettyConnectionInfo = nettyConnectionInfo;
        this.keyHashGenerator = key -> new BoundedHashUtil(NodeSettings.getInstance().getIdentifierSize()).hash(new Long(HashUtil.convertIPtoHex(key,2)), Long.class);
        this.repository = new KademliaRepositoryImp();
        this.InitHandler();
    }

    public DHTBootstrapNode(NettyConnectionInfo nettyConnectionInfo,Long ID,KeyHashGenerator<Long, String> keyHashGenerator) {
        LoggerKademlia.setLevelOFF();
        this.ID=ID;
        this.nettyConnectionInfo = nettyConnectionInfo;
        this.keyHashGenerator = keyHashGenerator;
        this.repository = new KademliaRepositoryImp();
        this.InitHandler();
    }


    private void InitHandler(){
        handler = new PongMessageHandler<Long, NettyConnectionInfo>() {
            @Override
            public <I extends KademliaMessage<Long, NettyConnectionInfo, ?>, O extends KademliaMessage<Long, NettyConnectionInfo, ?>> O doHandle(KademliaNodeAPI<Long, NettyConnectionInfo> kademliaNode, I message) {
                kademliaNode.getRoutingTable().getBuckets().stream().filter(val->val!=null).forEach(x -> {
                   x.getNodeIds().stream().forEach(y-> {
                       if (y != null && !y.equals(0)) {
                           System.out.println("esd"+y.toString());
                       }
                   });
                });
                return (O) doHandle(kademliaNode, (PongKademliaMessage<Long, NettyConnectionInfo>) message);
            }
        };
    }


    public void start() {
        bootStrapNode = new NettyKademliaDHTNodeBuilder<>(
                this.ID,
                this.nettyConnectionInfo,
                this.repository,
                keyHashGenerator
        ).withNodeSettings(NodeSettings.getInstance()).build();
      //  bootStrapNode.registerMessageHandler(MessageType.PONG, handler);
        bootStrapNode.start();
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
