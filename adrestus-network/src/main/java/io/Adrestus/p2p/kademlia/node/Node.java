package io.Adrestus.p2p.kademlia.node;

import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;

import java.io.Serializable;

/**
 * Core node. All the information we need of any sort of kademlia node to contact them.
 *
 * @param <ID> Type of the node ID
 * @param <C>  Type of the node ConnectionInfo
 */
public interface Node<ID extends Number, C extends ConnectionInfo> extends Serializable {
    C getConnectionInfo();

    ID getId();
}
