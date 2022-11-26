package io.Adrestus.p2p.kademlia;

import io.Adrestus.config.KademliaConfiguration;
import io.Adrestus.config.NodeSettings;
import io.Adrestus.p2p.kademlia.builder.NettyKademliaDHTNodeBuilder;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.common.NettyExternalNode;
import io.Adrestus.p2p.kademlia.exception.DuplicateStoreRequest;
import io.Adrestus.p2p.kademlia.exception.UnsupportedBoundingException;
import io.Adrestus.p2p.kademlia.model.LookupAnswer;
import io.Adrestus.p2p.kademlia.model.StoreAnswer;
import io.Adrestus.p2p.kademlia.node.KeyHashGenerator;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.p2p.kademlia.repository.KademliaRepository;
import io.Adrestus.p2p.kademlia.util.BoundedHashUtil;
import io.Adrestus.p2p.kademlia.util.LoggerKademlia;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Server1Test {

    @Test
    public void test() throws ExecutionException, InterruptedException, DuplicateStoreRequest {
        LoggerKademlia.setLevelOFF();
        int port = 1080;
        KademliaConfiguration.IDENTIFIER_SIZE=3;
        NodeSettings.getInstance();
        KeyHashGenerator<BigInteger, String> keyHashGenerator = key -> {
            try {
                return new BoundedHashUtil(NodeSettings.getInstance().getIdentifierSize()).hash(key.hashCode(), BigInteger.class);
                // return new BoundedHashUtil(NodeSettings.getInstance().getIdentifierSize()).hash(new BigInteger(HashUtil.convertIPtoHex(key, 16)), BigInteger.class);
            } catch (UnsupportedBoundingException e) {
                throw new IllegalArgumentException("Key hash generator not valid");
            }
        };


        // Starting node 1
        NettyExternalNode node1 = new NettyExternalNode(
                new NettyConnectionInfo("192.168.1.116", 8000),
                BigInteger.valueOf(1L)
        );

        NettyKademliaDHTNode< String, String> node2 = new NettyKademliaDHTNodeBuilder<>(
                BigInteger.valueOf(2L),
                new NettyConnectionInfo("192.168.1.106", 8001),
                new SampleRepository(),
                keyHashGenerator
        ).build();
        node2.start(node1).get();  // Wait till

        StoreAnswer<BigInteger, String> storeAnswer = node2.store("2", "2").get();


        LookupAnswer<BigInteger, String, String> lookupAnswer = node2.lookup("2").get();
        System.out.printf("Lookup result: %s - Value: %s%n", lookupAnswer.getResult(), lookupAnswer.getValue());

        LookupAnswer<BigInteger, String, String> lookupAnswer2 = node2.lookup("1").get();
        System.out.printf("Lookup result: %s - Value: %s%n", lookupAnswer2.getResult(), lookupAnswer2.getValue());
        Thread.sleep(6000);


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
