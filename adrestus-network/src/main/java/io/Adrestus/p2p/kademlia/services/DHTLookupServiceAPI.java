package io.Adrestus.p2p.kademlia.services;

import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;
import io.Adrestus.p2p.kademlia.model.LookupAnswer;
import io.Adrestus.p2p.kademlia.protocol.handler.MessageHandler;

import java.io.Serializable;
import java.util.concurrent.Future;

public interface DHTLookupServiceAPI<ID extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> extends MessageHandler<ID, C> {
    default void cleanUp() {
    }

    Future<LookupAnswer<ID, K, V>> lookup(K key);
}
