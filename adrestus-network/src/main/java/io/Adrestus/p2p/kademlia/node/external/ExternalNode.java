/* Copyright (c) 2012-2014, 2016. The SimGrid Team.
 * All rights reserved.                                                     */

/* This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package. */

package io.Adrestus.p2p.kademlia.node.external;

import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;
import io.Adrestus.p2p.kademlia.node.Node;
import io.Adrestus.p2p.kademlia.node.decorators.DateAwareNodeDecorator;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

/**
 * Kademlia node from outside of the system (other kademlia nodes are implementation of this class when they are seen in this system)
 * @param <ID> Number type of node ID between supported types
 * @param <C> Your implementation of connection info
 */
@Getter
@Setter
public abstract class ExternalNode<ID extends Number, C extends ConnectionInfo> extends DateAwareNodeDecorator<ID, C> implements Comparable<Object> {
  protected ID distance;

  public ExternalNode(Node<ID, C> node, ID distance) {
    super(node);
    this.distance = distance;
  }

  public abstract int compareTo(@NotNull Object o);

  @Override
  public String toString() {
    return "ExternalNode [id=" + getId() + ", distance=" + distance + "]";
  }
}