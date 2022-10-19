/* Copyright (c) 2012-2014, 2016. The SimGrid Team.
 * All rights reserved.                                                     */

/* This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package. */

package io.Adrestus.p2p.kademlia.model;

import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;
import io.Adrestus.p2p.kademlia.node.external.ExternalNode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Answer to a "FIND_NODE" query. Contains the nodes closest to an id given
 * @param <ID> Number type of node ID between supported types
 * @param <C> Your implementation of connection info
 */
@Getter
@Setter
public class FindNodeAnswer<ID extends Number, C extends ConnectionInfo> implements Serializable {
  private ID destinationId;
  /* Closest nodes in the answer. */
  private List<ExternalNode<ID, C>> nodes;

  public FindNodeAnswer() {
    nodes = new ArrayList<>();
  }

  public FindNodeAnswer(ID destinationId) {
    this();
    this.destinationId = destinationId;
  }

  public int size() {
    return nodes.size();
  }

  public void remove(int index) {
    nodes.remove(index);
  }

  public void add(ExternalNode<ID, C> externalNode) {
    nodes.add(externalNode);
  }

  public void update(List<ExternalNode<ID, C>> nodes){
    this.nodes = nodes;
  }


  /**
   * Merge the contents of this answer with another answer
   * @param findNodeAnswer another answer
   * @return number of nodes added to answer
   */
  public int merge(FindNodeAnswer<ID, C> findNodeAnswer, int findNodeSize) {
    int nbAdded = 0;

    for (ExternalNode<ID, C> c: findNodeAnswer.getNodes()) {
      if (!nodes.contains(c)) {
        nbAdded++;
        nodes.add(c);
      }
    }
    Collections.sort(nodes);
    //Trim the list
    while (findNodeAnswer.size() > findNodeSize) {
      findNodeAnswer.remove(findNodeAnswer.size() - 1);
    }
    return nbAdded;
  }


  /**
   * @return if the destination has been found
   */
  public boolean destinationFound() {
    if (nodes.size() < 1) {
      return false;
    }
    ExternalNode<ID, C> tail = nodes.get(0);
    return tail.getDistance().equals(0);
  }

  @Override
  public String toString() {
    return "Answer [destinationId=" + destinationId + ", nodes=" + nodes + "]";
  }
}
