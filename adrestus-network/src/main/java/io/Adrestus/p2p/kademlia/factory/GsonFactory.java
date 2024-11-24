package io.Adrestus.p2p.kademlia.factory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.Adrestus.p2p.kademlia.model.FindNodeAnswer;
import io.Adrestus.p2p.kademlia.node.Node;
import io.Adrestus.p2p.kademlia.node.external.ExternalNode;
import io.Adrestus.p2p.kademlia.protocol.message.*;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.p2p.kademlia.repository.KademliaDataDeserializer;
import io.Adrestus.p2p.kademlia.serialization.*;

import java.io.Serializable;

public interface GsonFactory {
    Gson gson();

    GsonBuilder gsonBuilder();

    class DefaultGsonFactory<K extends Serializable, V extends Serializable> implements GsonFactory {

        private final Class<K> kClass;
        private final Class<V> vClass;

        public DefaultGsonFactory(Class<K> kClass, Class<V> vClass) {
            this.kClass = kClass;
            this.vClass = vClass;
        }

        @Override
        public GsonBuilder gsonBuilder() {
            GsonBuilder gsonBuilder = new GsonBuilder();
            return gsonBuilder
                    .enableComplexMapKeySerialization()
                    .serializeNulls()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)

                    .registerTypeAdapter(KademliaMessage.class, new KademliaMessageDeserializer<K, V>(kClass, vClass))
                    .registerTypeAdapter(DHTLookupKademliaMessage.DHTLookup.class, new DHTLookUpDataDeserializer<K>(kClass))
                    .registerTypeAdapter(DHTLookupResultKademliaMessage.DHTLookupResult.class, new DHTLookUpResultDeserializer<K, V>(kClass, vClass))
                    .registerTypeAdapter(DHTStoreKademliaMessage.DHTData.class, new DHTStoreDataDeserializer<K, V>(kClass, vClass))
                    .registerTypeAdapter(DHTStoreResultKademliaMessage.DHTStoreResult.class, new DHTStoreResultDataDataDeserializer<K>(kClass))
                    .registerTypeAdapter(ExternalNode.class, new ExternalNodeDeserializer())
                    .registerTypeAdapter(FindNodeAnswer.class, new FindNodeAnswerDeserializer())
//                    .registerTypeAdapter(Node.class, new NodeInstanceCreator())
                    .registerTypeAdapter(Node.class, new NodeSerializer())
                    .registerTypeAdapter(Node.class, new NodeDeserializer())
                    .registerTypeAdapter(ExternalNode.class, new ExternalNodeSerializer())
                    .registerTypeAdapter(KademliaData.class, new KademliaDataDeserializer());
        }

        @Override
        public Gson gson() {
            return gsonBuilder().create();
        }
    }

}
