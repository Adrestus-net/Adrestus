package io.Adrestus.p2p.kademlia;

import com.google.common.net.InetAddresses;
import io.Adrestus.config.KademliaConfiguration;
import io.Adrestus.config.NodeSettings;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.p2p.kademlia.builder.NettyKademliaDHTNodeBuilder;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.exception.DuplicateStoreRequest;
import io.Adrestus.p2p.kademlia.exception.FullBucketException;
import io.Adrestus.p2p.kademlia.exception.UnsupportedBoundingException;
import io.Adrestus.p2p.kademlia.node.KeyHashGenerator;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.p2p.kademlia.repository.KademliaRepository;
import io.Adrestus.p2p.kademlia.table.Bucket;
import io.Adrestus.p2p.kademlia.table.DefaultRoutingTableFactory;
import io.Adrestus.p2p.kademlia.table.RoutingTable;
import io.Adrestus.p2p.kademlia.table.RoutingTableFactory;
import io.Adrestus.p2p.kademlia.util.BoundedHashUtil;
import io.Adrestus.p2p.kademlia.util.LoggerKademlia;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RandomBigIntegerTest {
    private static final String HASH_MD5_ALGORITHM_NAME = "MD5";

    //@Test
    public void randomID() throws InterruptedException, ExecutionException, FullBucketException, DuplicateStoreRequest {
        LoggerKademlia.setLevelOFF();
        int port = 1080;
        //use this only for debug not for tests because nodesjoiningtest
        //produces error and need size of 4
        KademliaConfiguration.IDENTIFIER_SIZE=3;
        NodeSettings.getInstance();
        KeyHashGenerator<BigInteger, String> keyHashGenerator = key -> {
            try {
                return new BoundedHashUtil(NodeSettings.getInstance().getIdentifierSize()).hash(new BigInteger(convertIPtoHex(key, 16)), BigInteger.class);
            } catch (UnsupportedBoundingException e) {
                throw new IllegalArgumentException("Key hash generator not valid");
            }
        };

        Random random=new Random();
        String ipString = InetAddresses.fromInteger(random.nextInt()).getHostAddress();
        BigInteger id = new BigInteger(convertIPtoHex(ipString, 16));
        // node 1
        NettyKademliaDHTNode<String, KademliaData> bootsrtap = new NettyKademliaDHTNodeBuilder<>(
                id,
                new NettyConnectionInfo("127.0.0.1", port),
                new AdrestusNodeTest.SampleRepository(),
                keyHashGenerator
        ).build();
        bootsrtap.start();

        RoutingTableFactory<BigInteger, NettyConnectionInfo, Bucket<BigInteger, NettyConnectionInfo>> routingTableFactory = new DefaultRoutingTableFactory<>();

        port = port + 1;
        ArrayList<NettyKademliaDHTNode<String, String>> list = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            String ipString1 = InetAddresses.fromInteger(random.nextInt()).getHostAddress();
            BigInteger id1 = new BigInteger(convertIPtoHex(ipString1, 16));
            RoutingTable<BigInteger, NettyConnectionInfo, Bucket<BigInteger, NettyConnectionInfo>> routingTable = routingTableFactory.getRoutingTable(BigInteger.valueOf(i));
            NettyKademliaDHTNode<String, String> nextnode = new NettyKademliaDHTNodeBuilder<>(
                    id1,
                    new NettyConnectionInfo("127.0.0.1", port + (int) i),
                    new SampleRepository(),
                    keyHashGenerator
            ).routingTable(routingTable).build();
            System.out.println("Starting node " + nextnode.getId() + ": " + nextnode.start(bootsrtap).get());
            routingTable.update(nextnode);
            list.add(nextnode);
            nextnode.store(String.valueOf(id1),String.valueOf(i));
        }



        Thread.sleep(5000);
        assertEquals(7,getActiveNode(list.get(4)).size());

        list.forEach(x -> x.stop());
        bootsrtap.stop();
    }

    @Test
    public void incrementID() throws InterruptedException, ExecutionException, FullBucketException, DuplicateStoreRequest {
        LoggerKademlia.setLevelOFF();
        int port = 1070;
        //use this only for debug not for tests because nodesjoiningtest
        //produces error and need size of 4
        KademliaConfiguration.IDENTIFIER_SIZE=3;
        NodeSettings.getInstance();
        KeyHashGenerator<BigInteger, String> keyHashGenerator = key -> {
            try {
                return new BoundedHashUtil(NodeSettings.getInstance().getIdentifierSize()).hash(key.hashCode(), BigInteger.class);
            } catch (UnsupportedBoundingException e) {
                throw new IllegalArgumentException("Key hash generator not valid");
            }
        };

        // node 1
        NettyKademliaDHTNode<String, KademliaData> bootsrtap = new NettyKademliaDHTNodeBuilder<>(
                BigInteger.valueOf(7L),
                new NettyConnectionInfo("127.0.0.1", port),
                new AdrestusNodeTest.SampleRepository(),
                keyHashGenerator
        ).build();
        bootsrtap.start();

        RoutingTableFactory<BigInteger, NettyConnectionInfo, Bucket<BigInteger, NettyConnectionInfo>> routingTableFactory = new DefaultRoutingTableFactory<>();

        port = port + 1;
        ArrayList<NettyKademliaDHTNode<String, String>> list = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            RoutingTable<BigInteger, NettyConnectionInfo, Bucket<BigInteger, NettyConnectionInfo>> routingTable = routingTableFactory.getRoutingTable(BigInteger.valueOf(i));
            NettyKademliaDHTNode<String, String> nextnode = new NettyKademliaDHTNodeBuilder<>(
                    BigInteger.valueOf(i),
                    new NettyConnectionInfo("127.0.0.1", port + (int) i),
                    new SampleRepository(),
                    keyHashGenerator
            ).routingTable(routingTable).build();
            System.out.println("Starting node " + nextnode.getId() + ": " + nextnode.start(bootsrtap).get());
            routingTable.update(nextnode);
            list.add(nextnode);
            nextnode.store(String.valueOf(i),String.valueOf(i));
        }



        Thread.sleep(2000);
        assertEquals(7,getActiveNode(list.get(4)).size());

        list.forEach(x -> x.stop());
        bootsrtap.stop();

    }

    public List<String> getActiveNode(NettyKademliaDHTNode<String, String> active_node) {
        ArrayList<String> active_nodes = new ArrayList<>();
        active_node.getRoutingTable().getBuckets().forEach(bucket -> {
            bucket.getNodeIds().forEach(node -> {
                try {
                    active_nodes.add(active_node.lookup(node.toString()).get(KademliaConfiguration.KADEMLIA_GET_TIMEOUT, TimeUnit.SECONDS).getValue());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
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
