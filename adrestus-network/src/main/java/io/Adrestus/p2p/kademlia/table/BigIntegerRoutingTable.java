/* Copyright (c) 2012-2014, 2016. The SimGrid Team.
 * All rights reserved.                                                     */

/* This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package. */

package io.Adrestus.p2p.kademlia.table;


import io.Adrestus.config.NodeSettings;
import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;

import java.math.BigInteger;

public class BigIntegerRoutingTable<C extends ConnectionInfo> extends AbstractRoutingTable<BigInteger, C, Bucket<BigInteger, C>> {

  private static final long serialVersionUID = 184823754242287459L;

  public BigIntegerRoutingTable(BigInteger id, NodeSettings nodeSettings) {
    super(id, nodeSettings);
  }

  @Override
  protected BigIntegerBucket<C> createBucketOfId(int i) {
    return new BigIntegerBucket<>(i);
  }

  /**
   * Returns an identifier which is in a specific bucket of a routing table
   * @param id id of the routing table owner
   * @param prefix id of the bucket where we want that identifier to be
   */
  public BigInteger getIdInPrefix(BigInteger id, int prefix) {
    if (prefix == 0) {
      return BigInteger.valueOf(0);
    }
    BigInteger identifier = BigInteger.valueOf(1);
    identifier = identifier.shiftLeft(prefix - 1);
    identifier = identifier.xor(id);
    return identifier;
  }

  /* Returns the corresponding node prefix for a given id */
  public int getNodePrefix(BigInteger id) {
    for (int j = 0; j < this.nodeSettings.getIdentifierSize(); j++) {
      if (!id.shiftRight((this.nodeSettings.getIdentifierSize() - 1 - j) & 0x1).equals(BigInteger.valueOf(0))) {
        return this.nodeSettings.getIdentifierSize() - j;
      }
    }
    return 0;
  }

  /* Finds the corresponding bucket in a routing table for a given identifier */
  public Bucket<BigInteger, C> findBucket(BigInteger id) {
    BigInteger xorNumber = id.xor(this.id);
    int prefix = this.getNodePrefix(xorNumber);
    return buckets.get(prefix);
  }
}
