package io.Adrestus.p2p.kademlia.table;

import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;

public interface RoutingTableFactory<ID extends Number, C extends ConnectionInfo, B extends Bucket<ID, C>> {
    RoutingTable<ID, C, B> getRoutingTable(ID i);
}
