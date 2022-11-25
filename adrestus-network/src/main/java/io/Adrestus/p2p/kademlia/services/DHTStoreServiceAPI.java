package io.Adrestus.p2p.kademlia.services;

import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;
import io.Adrestus.p2p.kademlia.exception.DuplicateStoreRequest;
import io.Adrestus.p2p.kademlia.model.StoreAnswer;
import io.Adrestus.p2p.kademlia.protocol.handler.MessageHandler;

import java.io.Serializable;
import java.util.concurrent.Future;

public interface DHTStoreServiceAPI<ID extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> extends MessageHandler<ID, C> {
    default void cleanUp(){}
    Future<StoreAnswer<ID, K>> store(K key, V value) throws DuplicateStoreRequest;
}
