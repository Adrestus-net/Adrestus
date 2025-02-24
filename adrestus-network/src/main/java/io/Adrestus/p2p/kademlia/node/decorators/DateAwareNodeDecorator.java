package io.Adrestus.p2p.kademlia.node.decorators;

import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;
import io.Adrestus.p2p.kademlia.node.Node;
import io.Adrestus.p2p.kademlia.node.NodeDecorator;

import java.time.Instant;


/**
 * Node decorator to hold last seen of a node
 *
 * @param <ID> Node ID type
 * @param <C>  Node ConnectionInfo type
 */
public class DateAwareNodeDecorator<ID extends Number, C extends ConnectionInfo> extends NodeDecorator<ID, C> {
    private Instant lastSeen =
            Instant.now();

    public DateAwareNodeDecorator(Node<ID, C> node) {
        super(node);
    }

    public void setLastSeen(Instant date) {
        this.lastSeen = date;
    }

    public Instant getLastSeen() {
        return this.lastSeen;
    }
}
