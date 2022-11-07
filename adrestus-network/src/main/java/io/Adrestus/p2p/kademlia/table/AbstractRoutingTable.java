/* Copyright (c) 2012-2014, 2016. The SimGrid Team.
 * All rights reserved.                                                     */

/* This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package. */

package io.Adrestus.p2p.kademlia.table;


import io.Adrestus.config.NodeSettings;
import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;
import io.Adrestus.p2p.kademlia.exception.FullBucketException;
import io.Adrestus.p2p.kademlia.model.FindNodeAnswer;
import io.Adrestus.p2p.kademlia.node.Node;
import io.Adrestus.p2p.kademlia.node.external.ExternalNode;
import io.Adrestus.p2p.kademlia.util.FindNodeAnswerReducer;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @param <ID> Number type of node ID between supported types
 * @param <C>  Your implementation of connection info
 * @param <B>  Bucket type
 */
@NoArgsConstructor
public abstract class AbstractRoutingTable<ID extends Number, C extends ConnectionInfo, B extends Bucket<ID, C>> implements RoutingTable<ID, C, B> {
    /* Bucket list */
    protected ArrayList<B> buckets;
    /* Id of the routing table owner (node id) */
    protected ID id;
    protected transient NodeSettings nodeSettings;

    /**
     * @param id Node id of the table owner
     */
    protected AbstractRoutingTable(ID id, NodeSettings nodeSettings) {
        this.id = id;
        this.nodeSettings = nodeSettings;
        buckets = new ArrayList<>();
        for (int i = 0; i < nodeSettings.getIdentifierSize() + 1; i++) {
            buckets.add(createBucketOfId(i));
        }
    }

    protected abstract B createBucketOfId(int i);


    /**
     * Updates the routing table with a new value. Returns true if node didnt exist in table before
     *
     * @param node to add or update (push to front)
     * @return if node is added newly (not updated)
     */
    public boolean update(Node<ID, C> node) throws FullBucketException {
        //Setting last seen date on node

        ExternalNode<ID, C> externalNode;

        if (!(node instanceof ExternalNode))
            externalNode = this.getExternalNode(node);
        else
            externalNode = (ExternalNode<ID, C>) node;

        externalNode.setLastSeen(new Date());
        Bucket<ID, C> bucket = this.findBucket(node.getId());
        if (bucket.contains(node)) {
            // If the element is already in the bucket, we update it and push it to the front of the bucket.
            bucket.pushToFront(externalNode);
            return false;
        } else if (bucket.size() < this.nodeSettings.getBucketSize()) {
            bucket.add(externalNode);
            return true;
        }
        throw new FullBucketException();
    }

    @Override
    public synchronized void forceUpdate(Node<ID, C> node) {
        try {
            this.update(node);
        } catch (FullBucketException e) {
            Bucket<ID, C> bucket = this.findBucket(node.getId());
            Date date = null;
            ID oldestNode = null;
            for (ID nodeId : bucket.getNodeIds()) {
                if (nodeId.equals(this.id)) {
                    continue;
                }
                if (date == null || bucket.getNode(nodeId).getLastSeen().before(date)) {
                    date = bucket.getNode(nodeId).getLastSeen();
                    oldestNode = nodeId;
                }
            }
            bucket.remove(oldestNode);
            // recursive, because some other thread may add new node to the bucket while we are making a space
            this.forceUpdate(node);
        }
    }

    /**
     * Delete node from table
     *
     * @param node to delete
     */
    public void delete(Node<ID, C> node) {
        Bucket<ID, C> bucket = this.findBucket(node.getId());
        bucket.remove(node);
    }


    /**
     * Returns the closest nodes we know to a given id
     * TODO: probably needs a better algorithm
     *
     * @param destinationId lookup
     * @return result for closest nodes to destination
     */
    public FindNodeAnswer<ID, C> findClosest(ID destinationId) {
        FindNodeAnswer<ID, C> findNodeAnswer = new FindNodeAnswer<>(destinationId);
        Bucket<ID, C> bucket = this.findBucket(destinationId);
        BucketHelper.addToAnswer(bucket, findNodeAnswer, destinationId);

        // Loop over every bucket (max common.BucketSize or lte identifier size) and add it to answer
        for (int i = 1; findNodeAnswer.size() < this.nodeSettings.getBucketSize() && ((bucket.getId() - i) >= 0 ||
                (bucket.getId() + i) <= this.nodeSettings.getIdentifierSize()); i++) {
            //Check the previous buckets
            if (bucket.getId() - i >= 0) {
                Bucket<ID, C> bucketP = this.buckets.get(bucket.getId() - i);
                BucketHelper.addToAnswer(bucketP, findNodeAnswer, destinationId);
            }
            //Check the next buckets
            if (bucket.getId() + i <= this.nodeSettings.getIdentifierSize()) {
                Bucket<ID, C> bucketN = this.buckets.get(bucket.getId() + i);
                BucketHelper.addToAnswer(bucketN, findNodeAnswer, destinationId);
            }
        }

        //We sort the list
        Collections.sort(findNodeAnswer.getNodes());
        //We trim the list
        new FindNodeAnswerReducer<>(this.id, findNodeAnswer, this.nodeSettings.getFindNodeSize(), this.nodeSettings.getIdentifierSize()).reduce();
        while (findNodeAnswer.size() > this.nodeSettings.getFindNodeSize()) {
            findNodeAnswer.remove(findNodeAnswer.size() - 1); //TODO: Not the best thing.
        }
        return findNodeAnswer;
    }

    @Override
    public boolean contains(ID nodeId) {
        Bucket<ID, C> bucket = this.findBucket(nodeId);
        return bucket.contains(nodeId);
    }

    public List<B> getBuckets() {
        return buckets;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("LongRoutingTable [ id=" + id + " ");
        for (Bucket<ID, C> bucket : buckets) {
            if (bucket.size() > 0) {
                string.append(bucket.getId()).append(" ");
            }
        }
        return string.toString();
    }

    public NodeSettings getNodeSettings() {
        return nodeSettings;
    }
}
