package io.Adrestus.kademlia.p2p.factory;

import io.Adrestus.kademlia.p2p.server.DefaultNettyServerInitializer;
import io.Adrestus.kademlia.p2p.server.NettyServerInitializer;
import lombok.Getter;

import java.io.Serializable;

public interface NettyServerInitializerFactory<K extends Serializable, V extends Serializable> {

    NettyServerInitializer<K, V> getKademliaNodeServerInitializerAPI();

    class DefaultNettyServerInitializerFactory<K extends Serializable, V extends Serializable> implements NettyServerInitializerFactory<K, V> {
        @Getter
        protected final KademliaMessageHandlerFactory<K, V> kademliaMessageHandlerFactory;

        public DefaultNettyServerInitializerFactory(KademliaMessageHandlerFactory<K, V> kademliaMessageHandlerFactory) {
            this.kademliaMessageHandlerFactory = kademliaMessageHandlerFactory;
        }

        public NettyServerInitializer<K, V> getKademliaNodeServerInitializerAPI(){
            return new DefaultNettyServerInitializer<>(
                    this.kademliaMessageHandlerFactory
            );
        }
    }
}
