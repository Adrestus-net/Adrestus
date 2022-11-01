package io.Adrestus.p2p.kademlia.table;

import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;
import io.Adrestus.p2p.kademlia.node.Node;
import io.Adrestus.p2p.kademlia.node.external.ExternalNode;

import java.io.Serializable;
import java.util.List;

public interface Bucket<ID extends Number, C extends ConnectionInfo> extends Serializable {
    int getId();
    int size();
    boolean contains(ID id);
    boolean contains(Node<ID, C> node);
    /**
     * Add a node to the front of the bucket
     * @param node to add to this bucket
     */
    void add(ExternalNode<ID, C> node);
    void remove(Node<ID, C> node);
    void remove(ID nodeId);
    void pushToFront(ExternalNode<ID, C> node);
    ExternalNode<ID, C> getNode(ID id);
    List<ID> getNodeIds();
}
