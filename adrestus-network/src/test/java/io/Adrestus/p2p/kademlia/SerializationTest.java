package io.Adrestus.p2p.kademlia;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.Adrestus.p2p.kademlia.common.NettyExternalNode;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.factory.GsonFactory;
import io.Adrestus.p2p.kademlia.model.LookupAnswer;
import io.Adrestus.p2p.kademlia.model.StoreAnswer;
import io.Adrestus.p2p.kademlia.node.Node;
import io.Adrestus.p2p.kademlia.node.external.LongExternalNode;
import io.Adrestus.p2p.kademlia.node.external.ExternalNode;
import io.Adrestus.p2p.kademlia.protocol.message.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.Type;


public class SerializationTest {
    private static Gson gson = null;
    private static Node<Long, NettyConnectionInfo> node = null;

    @BeforeAll
    public static void initGson() {
        gson = new GsonFactory.DefaultGsonFactory<String, String>().gson();
        node = NettyExternalNode.builder()
                .id(Long.valueOf(1))
                .connectionInfo(new NettyConnectionInfo("localhost", 8000))
                .build();
    }

    @Test
    void testDHTLookupSerialization() {
        DHTLookupKademliaMessage<Long, NettyConnectionInfo, String> kademliaMessage = new DHTLookupKademliaMessage<>();
        kademliaMessage.setData(new DHTLookupKademliaMessage.DHTLookup<>(node, "key", 1));
        kademliaMessage.setNode(node);


        String json = gson.toJson(kademliaMessage);
        System.out.println(json);

        Type type = new TypeToken<KademliaMessage<Long, NettyConnectionInfo, DHTLookupKademliaMessage.DHTLookup<Long, NettyConnectionInfo, String>>>() {
        }.getType();
        KademliaMessage<Long, NettyConnectionInfo, DHTLookupKademliaMessage.DHTLookup<Long, NettyConnectionInfo, String>> kademliaMessage1 = gson.fromJson(json, type);
        Assertions.assertTrue(kademliaMessage1 instanceof DHTLookupKademliaMessage);
        Assertions.assertEquals(kademliaMessage1.getType(), kademliaMessage.getType());
        Assertions.assertEquals(kademliaMessage1.getNode(), kademliaMessage.getNode());
        Assertions.assertEquals(kademliaMessage1.getData(), kademliaMessage.getData());
        Assertions.assertEquals(kademliaMessage1.getData().getCurrentTry(), kademliaMessage.getData().getCurrentTry());
        Assertions.assertEquals(kademliaMessage1.getData().getKey(), kademliaMessage.getData().getKey());
        Assertions.assertEquals(kademliaMessage1.getData().getRequester(), kademliaMessage.getData().getRequester());
    }

    @Test
    void testDHTLookUpSerialization() {
        DHTLookupResultKademliaMessage<Long, NettyConnectionInfo, String, String> kademliaMessage = new DHTLookupResultKademliaMessage<>();
        kademliaMessage.setData(new DHTLookupResultKademliaMessage.DHTLookupResult<>(LookupAnswer.Result.FOUND, "key", "value"));
        kademliaMessage.setNode(node);


        String json = gson.toJson(kademliaMessage);
        System.out.println(json);

        Type type = new TypeToken<KademliaMessage<Long, NettyConnectionInfo, Serializable>>() {
        }.getType();
        KademliaMessage<Long, NettyConnectionInfo, Serializable> kademliaMessage1 = gson.fromJson(json, type);
        Assertions.assertTrue(kademliaMessage1 instanceof DHTLookupResultKademliaMessage);
        Assertions.assertEquals(kademliaMessage1.getType(), kademliaMessage.getType());
        Assertions.assertEquals(kademliaMessage1.getNode(), kademliaMessage.getNode());
        Assertions.assertEquals(kademliaMessage1.getData(), kademliaMessage.getData());
    }

    @Test
    void testDHTStoreSerialization() {
        DHTStoreKademliaMessage<Long, NettyConnectionInfo, String, String> kademliaMessage = new DHTStoreKademliaMessage<>();

        kademliaMessage.setData(new DHTStoreKademliaMessage.DHTData<>(node, "key", "value"));
        kademliaMessage.setNode(node);

        String json = gson.toJson(kademliaMessage);
        System.out.println(json);

        Type type = new TypeToken<KademliaMessage<Long, NettyConnectionInfo, Serializable>>() {
        }.getType();
        KademliaMessage<Long, NettyConnectionInfo, Serializable> kademliaMessage1 = gson.fromJson(json, type);
        Assertions.assertTrue(kademliaMessage1 instanceof DHTStoreKademliaMessage);
        Assertions.assertEquals(kademliaMessage1.getType(), kademliaMessage.getType());
        Assertions.assertEquals(kademliaMessage1.getNode(), kademliaMessage.getNode());
        Assertions.assertEquals(kademliaMessage1.getData(), kademliaMessage.getData());
    }

    @Test
    void testDHTStoreResultSerialization() {
        DHTStoreResultKademliaMessage<Long, NettyConnectionInfo, String> kademliaMessage = new DHTStoreResultKademliaMessage<>();

        kademliaMessage.setData(new DHTStoreResultKademliaMessage.DHTStoreResult<>("key", StoreAnswer.Result.STORED));
        kademliaMessage.setNode(node);

        String json = gson.toJson(kademliaMessage);
        System.out.println(json);

        Type type = new TypeToken<KademliaMessage<Long, NettyConnectionInfo, Serializable>>() {
        }.getType();
        KademliaMessage<Long, NettyConnectionInfo, Serializable> kademliaMessage1 = gson.fromJson(json, type);
        Assertions.assertTrue(kademliaMessage1 instanceof DHTStoreResultKademliaMessage);
        Assertions.assertEquals(kademliaMessage1.getType(), kademliaMessage.getType());
        Assertions.assertEquals(kademliaMessage1.getNode(), kademliaMessage.getNode());
        Assertions.assertEquals(kademliaMessage1.getData(), kademliaMessage.getData());
    }

    @Test
    void testEmptyKademliaMessageSerialization() {
        EmptyKademliaMessage<Long, NettyConnectionInfo> kademliaMessage = new EmptyKademliaMessage<>();
        kademliaMessage.setNode(node);

        String json = gson.toJson(kademliaMessage);
        System.out.println(json);

        Type type = new TypeToken<KademliaMessage<Long, NettyConnectionInfo, Serializable>>() {
        }.getType();
        KademliaMessage<Long, NettyConnectionInfo, Serializable> kademliaMessage1 = gson.fromJson(json, type);
        Assertions.assertTrue(kademliaMessage1 instanceof EmptyKademliaMessage);
        Assertions.assertEquals(kademliaMessage1.getType(), kademliaMessage.getType());
        Assertions.assertEquals(kademliaMessage1.getNode(), kademliaMessage.getNode());
        Assertions.assertEquals(kademliaMessage1.getData(), kademliaMessage.getData());
    }

    @Test
    void testFindNodeRequestSerialization() {
        FindNodeRequestMessage<Long, NettyConnectionInfo> kademliaMessage = new FindNodeRequestMessage<>();
        kademliaMessage.setNode(node);
        kademliaMessage.setData(Long.valueOf(100));

        String json = gson.toJson(kademliaMessage);
        System.out.println(json);

        Type type = new TypeToken<KademliaMessage<Long, NettyConnectionInfo, Serializable>>() {
        }.getType();
        KademliaMessage<Long, NettyConnectionInfo, Serializable> kademliaMessage1 = gson.fromJson(json, type);
        Assertions.assertTrue(kademliaMessage1 instanceof FindNodeRequestMessage);
        Assertions.assertEquals(kademliaMessage1.getType(), kademliaMessage.getType());
        Assertions.assertEquals(kademliaMessage1.getNode(), kademliaMessage.getNode());
        Assertions.assertEquals(kademliaMessage1.getData(), kademliaMessage.getData());
    }

    @Test
    void testExternalNodeSerialization() {
        ExternalNode<Long, NettyConnectionInfo> externalNode = new LongExternalNode<>(node, Long.valueOf(1));

        String json = gson.toJson(externalNode);
        System.out.println(json);

        Type type = new TypeToken<ExternalNode<Long, NettyConnectionInfo>>() {
        }.getType();
        ExternalNode<Long, NettyConnectionInfo> externalNode1 = gson.fromJson(json, type);
        Assertions.assertNotNull(externalNode1);
        Assertions.assertEquals(externalNode.getId(), externalNode1.getId());
        Assertions.assertEquals(externalNode.getDistance(), externalNode1.getDistance());
    }
}
