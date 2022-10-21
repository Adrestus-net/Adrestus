package io.Adrestus.p2p.kademlia;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import io.Adrestus.p2p.kademlia.builder.NettyKademliaDHTNodeBuilder;
import io.Adrestus.p2p.kademlia.client.NettyMessageSender;
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

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class NodeTest {

    private static NettyMessageSender<String, String> nettyMessageSender1;

    private static NettyKademliaDHTNode<String, String> node1;
    private static NettyKademliaDHTNode<String, String> node2;
    private static NettyKademliaDHTNode<String, String> node3, node4, node5;

    @BeforeAll
    public static void setup() throws ExecutionException, InterruptedException, TimeoutException {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("io.netty");
        rootLogger.setLevel(ch.qos.logback.classic.Level.OFF);
        NodeSettings.Default.IDENTIFIER_SIZE = 4;
        NodeSettings.Default.BUCKET_SIZE = 100;
        NodeSettings.Default.PING_SCHEDULE_TIME_VALUE = 1;
        NodeSettings.Default.MAXIMUM_STORE_AND_LOOKUP_TIMEOUT_VALUE = 1;
        NodeSettings.Default.MAXIMUM_STORE_AND_LOOKUP_TIMEOUT_TIME_UNIT = TimeUnit.SECONDS;
        NodeSettings.Default.ENABLED_FIRST_STORE_REQUEST_FORCE_PASS = true;
        NodeSettings.Default.PING_SCHEDULE_TIME_UNIT = TimeUnit.SECONDS;
        NodeSettings.Default.PING_SCHEDULE_TIME_VALUE = 2;
        NodeSettings settings = NodeSettings.Default.build();
        KeyHashGenerator<BigInteger, String> keyHashGenerator = key -> new BoundedHashUtil(NodeSettings.Default.IDENTIFIER_SIZE).hash(key.hashCode(), BigInteger.class);


        MessageHandler<BigInteger, NettyConnectionInfo> handler = new PongMessageHandler<BigInteger, NettyConnectionInfo>() {
            @Override
            public <I extends KademliaMessage<BigInteger, NettyConnectionInfo, ?>, O extends KademliaMessage<BigInteger, NettyConnectionInfo, ?>> O doHandle(KademliaNodeAPI<BigInteger, NettyConnectionInfo> kademliaNode, I message) {
                kademliaNode.getRoutingTable().getBuckets().stream().forEach(x -> System.out.println(x.toString()));
                return (O) doHandle(kademliaNode, (PongKademliaMessage<BigInteger, NettyConnectionInfo>) message);
            }
        };
        nettyMessageSender1 = new NettyMessageSender<>();

        node1 = new NettyKademliaDHTNodeBuilder<>(
                BigInteger.valueOf(1),
                new NettyConnectionInfo("127.0.0.1", 8081),
                new SampleRepository(),
                keyHashGenerator
        ).withNodeSettings(settings).build();
        node1.registerMessageHandler(MessageType.PONG, handler);
        node1.start();


        // node 2
        node2 = new NettyKademliaDHTNodeBuilder<>(
                BigInteger.valueOf(2),
                new NettyConnectionInfo("127.0.0.1", 8082),
                new SampleRepository(),
                keyHashGenerator
        ).withNodeSettings(settings).build();

        System.out.println("Bootstrapped? " + node2.start(node1).get(1, TimeUnit.SECONDS));

        node3 = new NettyKademliaDHTNodeBuilder<>(
                BigInteger.valueOf(3),
                new NettyConnectionInfo("127.0.0.1", 8083),
                new SampleRepository(),
                keyHashGenerator
        ).withNodeSettings(settings).build();

        System.out.println("Bootstrapped? " + node3.start(node1).get(1, TimeUnit.SECONDS));

        node4 = new NettyKademliaDHTNodeBuilder<>(
                BigInteger.valueOf(4),
                new NettyConnectionInfo("127.0.0.1", 8084),
                new SampleRepository(),
                keyHashGenerator
        ).withNodeSettings(settings).build();

        System.out.println("Bootstrapped? " + node4.start(node1).get(1, TimeUnit.SECONDS));


        node5 = new NettyKademliaDHTNodeBuilder<>(
                BigInteger.valueOf(5),
                new NettyConnectionInfo("127.0.0.1", 8085),
                new SampleRepository(),
                keyHashGenerator
        ).withNodeSettings(settings).build();

        System.out.println("Bootstrapped? " + node5.start(node1).get(5, TimeUnit.SECONDS));
    }

    @Test
    public void test_one() throws ExecutionException, InterruptedException, TimeoutException, DuplicateStoreRequest, NotExistStoreRequest {
        StoreAnswer<BigInteger, String> storeAnswer = node5.store("K", "V").get();
        System.out.println(storeAnswer.toString());
        System.out.println(storeAnswer.getResult());
        System.out.println(storeAnswer.getNodeId());

        LookupAnswer<BigInteger, String, String> k = node1.lookup("K").get();
        //node1.getKademliaNode().getRoutingTable().findClosest(BigInteger.valueOf(3)).getNodes().stream().forEach(x-> System.out.println(x.toString()));
        //node1.getKademliaNode().getRoutingTable().getBuckets().stream().forEach(x-> System.out.println(x.getNodeIds().toString()));
        // node2.getKademliaNode().getRoutingTable().getBuckets().stream().forEach(x-> System.out.println(x.getNodeIds().toString()));
        //node2.getKademliaNode().getRoutingTable().findClosest(BigInteger.valueOf(3)).getNodes().stream().forEach(x-> System.out.println(x.toString()));
        System.out.println(k.getResult());
        System.out.println(k.getValue());
        node4.store("ena", "dio");
        Thread.sleep(2000);
        node5.stopNow();
        StoreAnswer<BigInteger, String> storeAnswer2 = node4.store("G", "M").get();
        node4.remove("G").get();
        System.out.println(storeAnswer.toString());
        System.out.println(storeAnswer.getResult());
        System.out.println(storeAnswer.getNodeId());
        Thread.sleep(4000);

        System.out.println(node1.lookup("ena").get().getValue());
        LookupAnswer<BigInteger, String, String> k2 = node2.lookup("G").get();
        //node1.getKademliaNode().getRoutingTable().findClosest(BigInteger.valueOf(3)).getNodes().stream().forEach(x-> System.out.println(x.toString()));
        //node1.getKademliaNode().getRoutingTable().getBuckets().stream().forEach(x-> System.out.println(x.getNodeIds().toString()));
        // node2.getKademliaNode().getRoutingTable().getBuckets().stream().forEach(x-> System.out.println(x.getNodeIds().toString()));
        //node2.getKademliaNode().getRoutingTable().findClosest(BigInteger.valueOf(3)).getNodes().stream().forEach(x-> System.out.println(x.toString()));
        System.out.println(k2.getResult());
        System.out.println(k2.getValue());
        int y = 1;
    }

    @AfterAll
    public static void cleanup() {
        node4.stop();
        node3.stop();
        node2.stop();
        node1.stop();
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
