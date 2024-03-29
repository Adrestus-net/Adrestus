package io.Adrestus.p2p.kademlia.table;

import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;
import io.Adrestus.p2p.kademlia.model.FindNodeAnswer;
import io.Adrestus.p2p.kademlia.node.external.BigIntegerExternalNode;
import io.Adrestus.p2p.kademlia.node.external.ExternalNode;
import io.Adrestus.p2p.kademlia.node.external.IntegerExternalNode;
import io.Adrestus.p2p.kademlia.node.external.LongExternalNode;

import java.math.BigInteger;

@SuppressWarnings("unchecked")
public class BucketHelper {

    private BucketHelper() {
    }

    public static <ID extends Number, C extends ConnectionInfo> void addToAnswer(Bucket<ID, C> bucket, FindNodeAnswer<ID, C> answer, ID destination) {
        if (bucket instanceof LongBucket) {
            for (long id : ((Bucket<Long, C>) bucket).getNodeIds()) {
                ExternalNode<Long, C> node = ((Bucket<Long, C>) bucket).getNode(id);
                long destination1 = (Long) destination;
                answer.add((ExternalNode<ID, C>) new LongExternalNode<>(node, id ^ destination1));
            }
        }

        if (bucket instanceof IntegerBucket) {
            for (int id : ((Bucket<Integer, C>) bucket).getNodeIds()) {
                ExternalNode<Integer, C> node = ((Bucket<Integer, C>) bucket).getNode(id);
                int destination1 = (Integer) destination;
                answer.add((ExternalNode<ID, C>) new IntegerExternalNode<>(node, id ^ destination1));
            }
        }

        if (bucket instanceof BigIntegerBucket) {
            for (BigInteger id : ((Bucket<BigInteger, C>) bucket).getNodeIds()) {
                ExternalNode<BigInteger, C> node = ((Bucket<BigInteger, C>) bucket).getNode(id);
                BigInteger destination1 = (BigInteger) destination;
                answer.add((ExternalNode<ID, C>) new BigIntegerExternalNode<>(node, destination1.xor(id)));
            }
        }

    }

}
