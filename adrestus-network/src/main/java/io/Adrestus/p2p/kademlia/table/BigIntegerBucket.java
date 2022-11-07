/* Copyright (c) 2012-2014, 2016. The SimGrid Team.
 * All rights reserved.                                                     */

/* This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package. */

package io.Adrestus.p2p.kademlia.table;

import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;

import java.math.BigInteger;

public class BigIntegerBucket<C extends ConnectionInfo> extends AbstractBucket<BigInteger, C> {
    private static final long serialVersionUID = 2319131318326916205L;

    public BigIntegerBucket(int id) {
        super(id);
    }
}
