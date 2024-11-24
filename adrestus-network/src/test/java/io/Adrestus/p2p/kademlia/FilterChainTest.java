package io.Adrestus.p2p.kademlia;

import io.Adrestus.config.KademliaConfiguration;
import io.Adrestus.config.NodeSettings;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.p2p.kademlia.builder.NettyKademliaDHTNodeBuilder;
import io.Adrestus.p2p.kademlia.client.OkHttpMessageSender;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.exception.UnsupportedBoundingException;
import io.Adrestus.p2p.kademlia.factory.KademliaMessageHandlerFactory;
import io.Adrestus.p2p.kademlia.node.KeyHashGenerator;
import io.Adrestus.p2p.kademlia.repository.KademliaRepository;
import io.Adrestus.p2p.kademlia.serialization.GsonMessageSerializer;
import io.Adrestus.p2p.kademlia.server.filter.KademliaMainHandlerFilter;
import io.Adrestus.p2p.kademlia.server.filter.NettyKademliaServerFilter;
import io.Adrestus.p2p.kademlia.server.filter.NettyKademliaServerFilterChain;
import io.Adrestus.p2p.kademlia.util.BoundedHashUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class FilterChainTest {
    private static OkHttpMessageSender<String, String> nettyMessageSender1;
    private static NettyKademliaDHTNode<String, String> node1;

    private static class EmptyFilter extends NettyKademliaServerFilter<String, String> {
    }

    @SneakyThrows
    @BeforeAll
    public static void init() {
        KademliaConfiguration.IDENTIFIER_SIZE = 4;
        KademliaConfiguration.BUCKET_SIZE = 100;
        KademliaConfiguration.PING_SCHEDULE_TIME_VALUE = 5;
        NodeSettings.getInstance();
        KeyHashGenerator<BigInteger, String> keyHashGenerator = key -> {
            try {
                return new BoundedHashUtil(NodeSettings.getInstance().getIdentifierSize()).hash(new BigInteger(HashUtil.convertIPtoHex(key, 16)), BigInteger.class);
            } catch (UnsupportedBoundingException e) {
                throw new IllegalArgumentException("Key hash generator not valid");
            }
        };

        nettyMessageSender1 = new OkHttpMessageSender<>(String.class, String.class);

        // node 1
        node1 = new NettyKademliaDHTNodeBuilder<String, String>(
                String.class,
                String.class,
                BigInteger.valueOf(1),
                new NettyConnectionInfo("127.0.0.1", 8081),
                new SampleRepository(),
                keyHashGenerator
        ).withNodeSettings(NodeSettings.getInstance()).build();
        node1.start();

    }

    @AfterAll
    public static void cleanup() {
        nettyMessageSender1.stop();
        if (node1 != null)
            node1.stopNow();
    }

    @Test
    void testFilterChain() throws ExecutionException, InterruptedException, IOException, TimeoutException {
        @SuppressWarnings("unchecked")

        NettyKademliaServerFilterChain<String, String> filterChain = new NettyKademliaServerFilterChain<>();
        filterChain.addFilter(new EmptyFilter());
        filterChain.addFilter(new KademliaMainHandlerFilter<>(new GsonMessageSerializer<String, String>(String.class, String.class)));


        NettyKademliaDHTNode<String, String> node2 = new NettyKademliaDHTNodeBuilder<>(
                String.class,
                String.class,
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

        @Override
        public List<String> getList() {
            return null;
        }
    }
}
