package io.Adrestus.p2p.kademlia.node;

import io.Adrestus.TreeFactory;
import io.Adrestus.config.KademliaConfiguration;
import io.Adrestus.config.NodeSettings;
import io.Adrestus.config.StakingConfiguration;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.p2p.kademlia.NettyKademliaDHTNode;
import io.Adrestus.p2p.kademlia.builder.NettyKademliaDHTNodeBuilder;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.exception.DuplicateStoreRequest;
import io.Adrestus.p2p.kademlia.exception.UnsupportedBoundingException;
import io.Adrestus.p2p.kademlia.model.LookupAnswer;
import io.Adrestus.p2p.kademlia.protocol.handler.MessageHandler;
import io.Adrestus.p2p.kademlia.protocol.handler.PongMessageHandler;
import io.Adrestus.p2p.kademlia.protocol.message.KademliaMessage;
import io.Adrestus.p2p.kademlia.protocol.message.PongKademliaMessage;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.p2p.kademlia.repository.KademliaRepository;
import io.Adrestus.p2p.kademlia.repository.KademliaRepositoryImp;
import io.Adrestus.p2p.kademlia.util.BoundedHashUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DHTBootstrapNode {
    private static Logger LOG = LoggerFactory.getLogger(DHTBootstrapNode.class);


    private final NettyConnectionInfo nettyConnectionInfo;
    private final KeyHashGenerator<BigInteger, String> keyHashGenerator;
    private final KademliaRepository repository;
    private final Timer scheduledExecutorService;
    private final ECDSASign ecdsaSign;

    private BigInteger ID;
    private MessageHandler<BigInteger, NettyConnectionInfo> handler;
    private NettyKademliaDHTNode<String, KademliaData> bootStrapNode;
    private KademliaData kademliaData;
    private TimerTask task;


    public DHTBootstrapNode(NettyConnectionInfo nettyConnectionInfo) {
        this.nettyConnectionInfo = nettyConnectionInfo;
        this.scheduledExecutorService = new Timer();
        this.keyHashGenerator = key -> {
            try {
                return new BoundedHashUtil(NodeSettings.getInstance().getIdentifierSize()).hash(new BigInteger(HashUtil.convertIPtoHex(key, 16)), BigInteger.class);
            } catch (UnsupportedBoundingException e) {
                throw new IllegalArgumentException("Key hash generator not valid");
            }
        };
        this.repository = new KademliaRepositoryImp();
        this.ecdsaSign = new ECDSASign();
        //   this.InitHandler();
    }

    public DHTBootstrapNode(NettyConnectionInfo nettyConnectionInfo, BigInteger ID) {
        this.ID = ID;
        this.nettyConnectionInfo = nettyConnectionInfo;
        this.scheduledExecutorService = new Timer();
        this.keyHashGenerator = key -> {
            try {
                return new BoundedHashUtil(NodeSettings.getInstance().getIdentifierSize()).hash(new BigInteger(HashUtil.convertIPtoHex(key, 16)), BigInteger.class);
            } catch (UnsupportedBoundingException e) {
                throw new IllegalArgumentException("Key hash generator not valid");
            }
        };
        this.repository = new KademliaRepositoryImp();
        this.ecdsaSign = new ECDSASign();
        // this.InitHandler();
    }

    public DHTBootstrapNode(NettyConnectionInfo nettyConnectionInfo, BigInteger ID, KeyHashGenerator<BigInteger, String> keyHashGenerator) {
        this.scheduledExecutorService = new Timer();
        this.ID = ID;
        this.nettyConnectionInfo = nettyConnectionInfo;
        this.keyHashGenerator = keyHashGenerator;
        this.repository = new KademliaRepositoryImp();
        this.ecdsaSign = new ECDSASign();
        //  this.InitHandler();
    }


    private void InitHandler() {
        handler = new PongMessageHandler<BigInteger, NettyConnectionInfo>() {
            @Override
            public <I extends KademliaMessage<BigInteger, NettyConnectionInfo, ?>, O extends KademliaMessage<BigInteger, NettyConnectionInfo, ?>> O doHandle(KademliaNodeAPI<BigInteger, NettyConnectionInfo> kademliaNode, I message) {
                kademliaNode.getRoutingTable().getBuckets().stream().filter(val -> val != null).forEach(x -> {
                    x.getNodeIds().stream().forEach(y -> {
                        if (y != null && !y.equals(0)) {
                            System.out.println("esd" + y.toString());
                        }
                    });
                });
                return (O) doHandle(kademliaNode, (PongKademliaMessage<BigInteger, NettyConnectionInfo>) message);
            }
        };
    }


    public void Init() {
        bootStrapNode = new NettyKademliaDHTNodeBuilder<String, KademliaData>(
                String.class,
                KademliaData.class,
                this.ID,
                this.nettyConnectionInfo,
                this.repository,
                keyHashGenerator
        ).withNodeSettings(NodeSettings.getInstance()).build();
    }

    public void start() {
        bootStrapNode = new NettyKademliaDHTNodeBuilder<String, KademliaData>(
                String.class,
                KademliaData.class,
                this.ID,
                this.nettyConnectionInfo,
                this.repository,
                keyHashGenerator
        ).withNodeSettings(NodeSettings.getInstance()).build();
        //  bootStrapNode.registerMessageHandler(MessageType.PONG, handler);
        bootStrapNode.start();
    }

    public List<KademliaData> getActiveNodes() {
        ArrayList<KademliaData> active_nodes = new ArrayList<>();
        this.bootStrapNode.getRoutingTable().getBuckets().forEach(bucket -> {
            bucket.getNodeIds().forEach(node -> {
                try {
                    KademliaData value = bootStrapNode.lookup(node.toString()).get(KademliaConfiguration.KADEMLIA_GET_TIMEOUT, TimeUnit.SECONDS).getValue();
                    boolean verify = ecdsaSign.secp256Verify(HashUtil.sha256(StringUtils.getBytesUtf8(value.getAddressData().getAddress())), value.getAddressData().getAddress(), value.getAddressData().getECDSASignature());
                    if (!verify) {
                        LOG.info("Kademlia Data are not valid abort");
                    } else {

                        if (TreeFactory.getMemoryTree(0).getByaddress(value.getAddressData().getAddress()).get().getAmount().compareTo(BigDecimal.valueOf(StakingConfiguration.MINIMUM_STAKING)) < 0) {
                            LOG.info("Amount of this address not meet minimum requirements");
                        } else {
                            active_nodes.add(value);
                        }
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                } catch (Exception e) {
                    LOG.info("This address not found invalid");
                }
            });
        });
        active_nodes.removeIf(Objects::isNull);
        return active_nodes;
    }

    public void scheduledFuture() {
        task = new TimerTask() {
            @Override
            public void run() {
                //lookup for orphan keys stored on "this" node
                // that are down and not
                //exist anymore so it's ready for cleanup
                if (!bootStrapNode.getKademliaRepository().getList().isEmpty()) {
                    bootStrapNode.getKademliaRepository().getList().forEach(id -> {
                        EventLoopGroup workerGroup = new NioEventLoopGroup();
                        try {
                            KademliaData value = bootStrapNode.getKademliaRepository().get(id);
                            Bootstrap b = new Bootstrap(); // (1)
                            b.group(workerGroup);
                            b.channel(NioSocketChannel.class); // (3)
                            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
                            b.handler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                public void initChannel(SocketChannel ch) throws Exception {
                                }
                            });
                            ChannelFuture f = b.connect(value.getNettyConnectionInfo().getHost(), value.getNettyConnectionInfo().getPort()).sync(); // (5)
                            f.channel().close();
                        } catch (Exception e) {
                            bootStrapNode.getKademliaRepository().getList().forEach(x -> System.out.print(x));
                            bootStrapNode.getKademliaRepository().remove(id);
                            bootStrapNode.getKademliaRepository().getList().forEach(x -> System.out.print(x));
                        } finally {
                            workerGroup.shutdownGracefully();
                        }
                    });
                }

                //lookup for itself in
                //cases that another node is down
                LookupAnswer<BigInteger, String, KademliaData> lookupAnswer = null;
                try {
                    lookupAnswer = bootStrapNode.lookup(getID().toString()).get(KademliaConfiguration.KADEMLIA_GET_TIMEOUT, TimeUnit.SECONDS);
                    lookupAnswer.getValue();
                } catch (Exception ex) {
                    return;
                }

                if (lookupAnswer.getValue() == null) {
                    LOG.info("Data not existed trying to store");
                    try {
                        bootStrapNode.store(getID().toString(), getKademliaData()).get(KademliaConfiguration.KADEMLIA_GET_TIMEOUT, TimeUnit.SECONDS);
                    } catch (DuplicateStoreRequest duplicateStoreRequest) {
                        return;
                    } catch (Exception e) {
                    }
                } else {
                    System.out.println("Key " + lookupAnswer.getKey() + " found " + " from " + lookupAnswer.getNodeId());
                }
            }
        };
        scheduledExecutorService.scheduleAtFixedRate(task, 0, KademliaConfiguration.STORE_DELAY);
    }

    public void scheduledFuture(int delay) {
        task = new TimerTask() {
            @Override
            public void run() {
                //lookup for orphan keys stored on "this" node
                // that are down and not
                //exist anymore so it's ready for cleanup
                if (!bootStrapNode.getKademliaRepository().getList().isEmpty()) {
                    bootStrapNode.getKademliaRepository().getList().forEach(id -> {
                        EventLoopGroup workerGroup = new NioEventLoopGroup();
                        try {
                            KademliaData value = bootStrapNode.getKademliaRepository().get(id);
                            Bootstrap b = new Bootstrap(); // (1)
                            b.group(workerGroup);
                            b.channel(NioSocketChannel.class); // (3)
                            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
                            b.handler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                public void initChannel(SocketChannel ch) throws Exception {
                                }
                            });
                            ChannelFuture f = b.connect(value.getNettyConnectionInfo().getHost(), value.getNettyConnectionInfo().getPort()).sync(); // (5)
                            f.channel().close();
                        } catch (Exception e) {
                            bootStrapNode.getKademliaRepository().remove(id);
                        } finally {
                            workerGroup.shutdownGracefully();
                        }
                    });
                }

                //lookup for itself in
                //cases that another node is down
                LookupAnswer<BigInteger, String, KademliaData> lookupAnswer = null;
                try {
                    lookupAnswer = bootStrapNode.lookup(getID().toString()).get(KademliaConfiguration.KADEMLIA_GET_TIMEOUT, TimeUnit.SECONDS);
                    lookupAnswer.getValue();
                } catch (Exception ex) {
                    return;
                }

                if (lookupAnswer.getValue() == null) {
                    try {
                        bootStrapNode.store(getID().toString(), getKademliaData()).get(KademliaConfiguration.KADEMLIA_GET_TIMEOUT, TimeUnit.SECONDS);
                    } catch (DuplicateStoreRequest duplicateStoreRequest) {
                        return;
                    } catch (Exception e) {
                    }
                } else {
                }
            }
        };
        scheduledExecutorService.scheduleAtFixedRate(task, 0, Math.abs(delay));
    }

    public BigInteger getID() {
        return ID;
    }

    public void setID(BigInteger ID) {
        this.ID = ID;
    }

    public KademliaData getKademliaData() {
        return kademliaData;
    }

    public void setKademliaData(KademliaData kademliaData) {
        this.kademliaData = kademliaData;
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
