package io.Adrestus.p2p.kademlia;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import io.Adrestus.config.KademliaConfiguration;
import io.Adrestus.config.NodeSettings;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.p2p.kademlia.builder.NettyKademliaDHTNodeBuilder;
import io.Adrestus.p2p.kademlia.client.OkHttpMessageSender;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.exception.DuplicateStoreRequest;
import io.Adrestus.p2p.kademlia.exception.NotExistStoreRequest;
import io.Adrestus.p2p.kademlia.model.LookupAnswer;
import io.Adrestus.p2p.kademlia.model.StoreAnswer;
import io.Adrestus.p2p.kademlia.node.KademliaNodeAPI;
import io.Adrestus.p2p.kademlia.node.KeyHashGenerator;
import io.Adrestus.p2p.kademlia.protocol.MessageType;
import io.Adrestus.p2p.kademlia.protocol.handler.MessageHandler;
import io.Adrestus.p2p.kademlia.protocol.handler.PongMessageHandler;
import io.Adrestus.p2p.kademlia.protocol.message.KademliaMessage;
import io.Adrestus.p2p.kademlia.protocol.message.PongKademliaMessage;
import io.Adrestus.p2p.kademlia.repository.KademliaRepository;
import io.Adrestus.p2p.kademlia.util.BoundedHashUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class NodeTest {

    private static OkHttpMessageSender<String, String> nettyMessageSender1;

    private static NettyKademliaDHTNode<String, String> node1;
    private static NettyKademliaDHTNode<String, String> node2;
    private static NettyKademliaDHTNode<String, String> node3, node4, node5;

    @BeforeAll
    public static void setup() throws ExecutionException, InterruptedException, TimeoutException {

        KademliaConfiguration.IDENTIFIER_SIZE = 4;
        KademliaConfiguration.BUCKET_SIZE = 100;
        KademliaConfiguration.MAXIMUM_STORE_AND_LOOKUP_TIMEOUT_VALUE = 1;
        KademliaConfiguration.MAXIMUM_STORE_AND_LOOKUP_TIMEOUT_TIME_UNIT = TimeUnit.SECONDS;
        KademliaConfiguration.ENABLED_FIRST_STORE_REQUEST_FORCE_PASS = true;
        KademliaConfiguration.PING_SCHEDULE_TIME_UNIT = TimeUnit.SECONDS;
        KademliaConfiguration.PING_SCHEDULE_TIME_VALUE = 2;
        NodeSettings.getInstance();

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("io.netty");
        rootLogger.setLevel(ch.qos.logback.classic.Level.OFF);

        KeyHashGenerator<Long, String> keyHashGenerator = key -> new BoundedHashUtil(NodeSettings.getInstance().getIdentifierSize()).hash(new Long(HashUtil.convertIPtoHex(String.valueOf(key.hashCode()),16)), Long.class);


        MessageHandler<Long, NettyConnectionInfo> handler = new PongMessageHandler<Long, NettyConnectionInfo>() {
            @Override
            public <I extends KademliaMessage<Long, NettyConnectionInfo, ?>, O extends KademliaMessage<Long, NettyConnectionInfo, ?>> O doHandle(KademliaNodeAPI<Long, NettyConnectionInfo> kademliaNode, I message) {
                kademliaNode.getRoutingTable().getBuckets().stream().forEach(x -> System.out.println(x.toString()));
                return (O) doHandle(kademliaNode, (PongKademliaMessage<Long, NettyConnectionInfo>) message);
            }
        };
        nettyMessageSender1 = new OkHttpMessageSender<>();

        node1 = new NettyKademliaDHTNodeBuilder<>(
                Long.valueOf(1),
                new NettyConnectionInfo("127.0.0.1", 8081),
                new SampleRepository(),
                keyHashGenerator
        ).withNodeSettings(NodeSettings.getInstance()).build();
       // node1.registerMessageHandler(MessageType.PONG, handler);
        node1.start();


        // node 2
        node2 = new NettyKademliaDHTNodeBuilder<>(
                Long.valueOf(2),
                new NettyConnectionInfo("127.0.0.1", 8082),
                new SampleRepository(),
                keyHashGenerator
        ).withNodeSettings(NodeSettings.getInstance()).build();

        System.out.println("Bootstrapped? " + node2.start(node1).get(1, TimeUnit.SECONDS));

        node3 = new NettyKademliaDHTNodeBuilder<>(
                Long.valueOf(3),
                new NettyConnectionInfo("127.0.0.1", 8083),
                new SampleRepository(),
                keyHashGenerator
        ).withNodeSettings(NodeSettings.getInstance()).build();

        System.out.println("Bootstrapped? " + node3.start(node1).get(1, TimeUnit.SECONDS));

        node4 = new NettyKademliaDHTNodeBuilder<>(
                Long.valueOf(4),
                new NettyConnectionInfo("127.0.0.1", 8084),
                new SampleRepository(),
                keyHashGenerator
        ).withNodeSettings(NodeSettings.getInstance()).build();

        System.out.println("Bootstrapped? " + node4.start(node1).get(1, TimeUnit.SECONDS));


        node5 = new NettyKademliaDHTNodeBuilder<>(
                Long.valueOf(5),
                new NettyConnectionInfo("127.0.0.1", 8085),
                new SampleRepository(),
                keyHashGenerator
        ).withNodeSettings(NodeSettings.getInstance()).build();

        System.out.println("Bootstrapped? " + node5.start(node1).get(5, TimeUnit.SECONDS));
    }

    @Test
    public void test_one() throws ExecutionException, InterruptedException, TimeoutException, DuplicateStoreRequest, NotExistStoreRequest {
        StoreAnswer<Long, String> storeAnswer = node5.store("K", "V").get();
        System.out.println(storeAnswer.toString());
        System.out.println(storeAnswer.getResult());
        System.out.println(storeAnswer.getNodeId());

        LookupAnswer<Long, String, String> k = node1.lookup("K").get();
        //node1.getKademliaNode().getRoutingTable().findClosest(Long.valueOf(3)).getNodes().stream().forEach(x-> System.out.println(x.toString()));
        //node1.getKademliaNode().getRoutingTable().getBuckets().stream().forEach(x-> System.out.println(x.getNodeIds().toString()));
        // node2.getKademliaNode().getRoutingTable().getBuckets().stream().forEach(x-> System.out.println(x.getNodeIds().toString()));
        //node2.getKademliaNode().getRoutingTable().findClosest(Long.valueOf(3)).getNodes().stream().forEach(x-> System.out.println(x.toString()));
        System.out.println(k.getResult());
        System.out.println(k.getValue());
        node4.store("ena", "dio");
        node4.store("F", "dio");
        Thread.sleep(2000);
        node4.store("gg", "M");
        node5.store("D", "sad");
        Thread.sleep(4000);

        System.out.println(node1.lookup("gg").get().getValue());
        System.out.println(node2.lookup("gg").get().getValue());
        System.out.println(node3.lookup("gg").get().getValue());
        System.out.println(node4.lookup("gg").get().getValue());
        System.out.println(node5.lookup("gg").get().getValue());
        LookupAnswer<Long, String, String> k2 = node2.lookup("D").get();
        LookupAnswer<Long, String, String> k3 = node3.lookup("D").get();
        LookupAnswer<Long, String, String> k4 = node4.lookup("D").get();
        LookupAnswer<Long, String, String> k5 = node5.lookup("D").get();
        //node1.getKademliaNode().getRoutingTable().findClosest(Long.valueOf(3)).getNodes().stream().forEach(x-> System.out.println(x.toString()));
        //node1.getKademliaNode().getRoutingTable().getBuckets().stream().forEach(x-> System.out.println(x.getNodeIds().toString()));
        // node2.getKademliaNode().getRoutingTable().getBuckets().stream().forEach(x-> System.out.println(x.getNodeIds().toString()));
        //node2.getKademliaNode().getRoutingTable().findClosest(Long.valueOf(3)).getNodes().stream().forEach(x-> System.out.println(x.toString()));
        System.out.println(k2.getResult());
        System.out.println(k2.getValue());
        int y = 1;
    }

    @AfterAll
    public static void cleanup() {
        if (node5 != null)
            node5.stop();
        if (node4 != null)
            node4.stop();
        if (node3 != null)
            node3.stop();
        if (node2 != null)
            node2.stop();
        if (node1 != null)
            node1.stop();
        if (nettyMessageSender1 != null)
            nettyMessageSender1.stop();
    }

    public static class SampleRepository implements KademliaRepository<String, String> {
        protected final Map<String, String> data = new HashMap<>();

        @Override
        public void store(String key, String value) {

            data.putIfAbsent(key, value);
        }

        @Override
        public String get(String key) {
            return data.get(key);
        }

        @Override
        public void remove(String key) {
            data.remove(key);
        }

        @Override
        public boolean contains(String key) {
            return data.containsKey(key);
        }
    }
}
