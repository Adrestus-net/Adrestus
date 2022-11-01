package io.Adrestus.p2p.kademlia.factory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.Adrestus.p2p.kademlia.common.NettyExternalNode;
import io.Adrestus.p2p.kademlia.model.FindNodeAnswer;
import io.Adrestus.p2p.kademlia.node.Node;
import io.Adrestus.p2p.kademlia.node.external.ExternalNode;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.p2p.kademlia.repository.KademliaDataDeserializer;
import io.Adrestus.p2p.kademlia.serialization.ExternalNodeDeserializer;
import io.Adrestus.p2p.kademlia.serialization.FindNodeAnswerDeserializer;
import io.Adrestus.p2p.kademlia.serialization.NodeDeserializer;
import io.Adrestus.p2p.kademlia.serialization.NodeSerializer;

public final class SingletonGsonFactory implements GsonFactory {
    private static volatile SingletonGsonFactory instance;
    private static volatile Gson gson;

    public static GsonBuilder gsonBuilder() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        return gsonBuilder
                .enableComplexMapKeySerialization()
                .serializeNulls()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(ExternalNode.class, new ExternalNodeDeserializer())
                .registerTypeAdapter(FindNodeAnswer.class, new FindNodeAnswerDeserializer())
                .registerTypeAdapter(NettyExternalNode.class, new NodeDeserializer())
                .registerTypeAdapter(Node.class, new NodeSerializer())
                .registerTypeAdapter(KademliaData.class, new KademliaDataDeserializer());
    }

    private SingletonGsonFactory() {
        // Protect against instantiation via reflection
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
    }

    public static synchronized SingletonGsonFactory getInstance() {
        if (instance == null) {
            synchronized (SingletonGsonFactory.class) {
                if (instance == null) {
                    instance = new SingletonGsonFactory();
                    gson = gsonBuilder().create();
                }
            }
        }
        return instance;
    }

    @Override
    public Gson gson() {
        return gson;
    }
}
