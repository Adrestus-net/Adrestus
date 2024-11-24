package io.Adrestus.p2p.kademlia;

import io.Adrestus.config.KademliaConfiguration;
import io.Adrestus.config.NodeSettings;
import io.Adrestus.p2p.kademlia.builder.NettyKademliaDHTNodeBuilder;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.exception.DuplicateStoreRequest;
import io.Adrestus.p2p.kademlia.exception.UnsupportedBoundingException;
import io.Adrestus.p2p.kademlia.node.KeyHashGenerator;
import io.Adrestus.p2p.kademlia.repository.KademliaRepository;
import io.Adrestus.p2p.kademlia.util.BoundedHashUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GetActiveNodes {
    private static final String HASH_MD5_ALGORITHM_NAME = "MD5";

    @BeforeAll
    public static void setup() {
    }


    @Test
    public void test() throws ExecutionException, InterruptedException, DuplicateStoreRequest {
        int port = 1180;
        int size = 300;
        //use this only for debug not for tests because nodesjoiningtest
        //produces error and need size of 4
        KademliaConfiguration.IDENTIFIER_SIZE = 7;
        KademliaConfiguration.BUCKET_SIZE = 300;
        NodeSettings.getInstance();
        KeyHashGenerator<BigInteger, String> keyHashGenerator = key -> {
            try {
                // return new BoundedHashUtil(NodeSettings.getInstance().getIdentifierSize()).hash(key.hashCode(), BigInteger.class);
                return new BoundedHashUtil(NodeSettings.getInstance().getIdentifierSize()).hash(new BigInteger(convertIPtoHex(key, 1)), BigInteger.class);
            } catch (UnsupportedBoundingException e) {
                throw new IllegalArgumentException("Key hash generator not valid");
            }
        };

        // node 1
        NettyKademliaDHTNode<String, String> bootsrtap = new NettyKademliaDHTNodeBuilder<String, String>(
                String.class,
                String.class,
                BigInteger.valueOf(1L),
                new NettyConnectionInfo("127.0.0.1", port),
                new SampleRepository(),
                keyHashGenerator
        ).build();
        bootsrtap.start();
        ArrayList<NettyKademliaDHTNode<String, String>> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            String ipString = "192.168.1." + String.valueOf(i);
            BigInteger id1 = new BigInteger(convertIPtoHex(ipString, 24));
            NettyKademliaDHTNode<String, String> nextnode = null;
            if (i == 0) {
                nextnode = new NettyKademliaDHTNodeBuilder<String, String>(
                        String.class,
                        String.class,
                        id1,
                        new NettyConnectionInfo("127.0.0.1", port - 1),
                        new SampleRepository(),
                        keyHashGenerator
                ).withNodeSettings(NodeSettings.getInstance()).build();
            } else {
                nextnode = new NettyKademliaDHTNodeBuilder<String, String>(
                        String.class,
                        String.class,
                        id1,
                        new NettyConnectionInfo("127.0.0.1", port + (int) i),
                        new SampleRepository(),
                        keyHashGenerator
                ).withNodeSettings(NodeSettings.getInstance()).build();
            }
            System.out.println("Starting node " + nextnode.getId() + ": " + nextnode.start(bootsrtap).get());
            nextnode.store(String.valueOf(nextnode.getId()), ipString);
            list.add(nextnode);
        }
        Thread.sleep(8000);
        List<String> vals = getActiveNode(bootsrtap);
        assertEquals(size, vals.size());
        List duplicates =
                vals.stream().collect(Collectors.groupingBy(Function.identity()))
                        .entrySet()
                        .stream()
                        .filter(e -> e.getValue().size() > 1)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());
        assertEquals(0, duplicates.size());
        //list.forEach(x -> x.stop());
        //bootsrtap.stop();
    }


    public List<String> getActiveNode(NettyKademliaDHTNode<String, String> active_node) {
        ArrayList<String> active_nodes = new ArrayList<>();
        active_node.getRoutingTable().getBuckets().forEach(bucket -> {
            bucket.getNodeIds().forEach(node -> {
                try {
                    active_nodes.add(active_node.lookup(String.valueOf(node.toString())).get().getValue());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            });
        });
        active_nodes.removeIf(Objects::isNull);
        return active_nodes;
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

    public static String convertIPtoHex(String ip, int bits) {
        try {
            MessageDigest md5digest = MessageDigest.getInstance(HASH_MD5_ALGORITHM_NAME);
            byte[] hased_data = md5digest.digest(ip.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(convertStringToHex(Hex.toHexString(hased_data)));
            return hex.substring(0, bits);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String convertStringToHex(String str) {

        StringBuffer hex = new StringBuffer();

        // loop chars one by one
        for (char temp : str.toCharArray()) {

            // convert char to int, for char `a` decimal 97
            int decimal = (int) temp;

            // convert int to hex, for decimal 97 hex 61
            hex.append(Integer.toHexString(decimal));
        }

        return hex.toString();

    }
}
