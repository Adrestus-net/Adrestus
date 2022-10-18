package io.Adrestus.p2p.kademlia;

import io.ep2p.kademlia.NodeSettings;
import io.Adrestus.p2p.kademlia.builder.NettyKademliaDHTNodeBuilder;
import io.Adrestus.p2p.kademlia.client.NettyMessageSender;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.factory.GsonFactory;
import io.Adrestus.p2p.kademlia.factory.KademliaMessageHandlerFactory;
import io.Adrestus.p2p.kademlia.server.filter.KademliaMainHandlerFilter;
import io.Adrestus.p2p.kademlia.server.filter.NettyKademliaServerFilter;
import io.Adrestus.p2p.kademlia.server.filter.NettyKademliaServerFilterChain;
import io.ep2p.kademlia.node.KeyHashGenerator;
import io.ep2p.kademlia.repository.KademliaRepository;
import io.ep2p.kademlia.util.BoundedHashUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class FilterChainTest {
    private static NettyMessageSender<String, String> nettyMessageSender1;
    private static NettyKademliaDHTNode<String, String> node1;

    private static class EmptyFilter extends NettyKademliaServerFilter<String, String>{}

    @SneakyThrows
    @BeforeAll
    public static void init() {
        NodeSettings.Default.IDENTIFIER_SIZE = 4;
        NodeSettings.Default.BUCKET_SIZE = 100;
        NodeSettings.Default.PING_SCHEDULE_TIME_VALUE = 5;

        KeyHashGenerator<BigInteger, String> keyHashGenerator = key -> new BoundedHashUtil(NodeSettings.Default.IDENTIFIER_SIZE).hash(key.hashCode(), BigInteger.class);

        nettyMessageSender1 = new NettyMessageSender<>();

        // node 1
        node1 = new NettyKademliaDHTNodeBuilder<>(
                BigInteger.valueOf(1),
                new NettyConnectionInfo("127.0.0.1", 8081),
                new SampleRepository(),
                keyHashGenerator
        ).build();
        node1.start();

    }

    @AfterAll
    public static void cleanup(){
        nettyMessageSender1.stop();
        node1.stopNow();
    }

    @Test
    void testFilterChain() throws ExecutionException, InterruptedException, IOException, TimeoutException {
        @SuppressWarnings("unchecked")

        NettyKademliaServerFilterChain<String, String> filterChain = new NettyKademliaServerFilterChain<>();
        filterChain.addFilter(new EmptyFilter());
        filterChain.addFilter(new KademliaMainHandlerFilter<>(new GsonFactory.DefaultGsonFactory<>().gson()));


        NettyKademliaDHTNode<String, String> node2 = new NettyKademliaDHTNodeBuilder<>(
                BigInteger.valueOf(2),
                new NettyConnectionInfo("127.0.0.1", 8082),
                new SampleRepository(),
                node1.getKeyHashGenerator()
        )
                .kademliaMessageHandlerFactory(new KademliaMessageHandlerFactory.DefaultKademliaMessageHandlerFactory<>(filterChain))
                .build();
        System.out.println("Bootstrapped? " + node2.start(node1).get(5, TimeUnit.SECONDS));


        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            node2.stop();
            latch.countDown();
        }).start();

        Assertions.assertTrue(filterChain.getFilters().get(0) instanceof EmptyFilter);

        latch.await();
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
