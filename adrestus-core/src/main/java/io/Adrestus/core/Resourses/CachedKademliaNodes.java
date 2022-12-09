package io.Adrestus.core.Resourses;

import com.google.common.base.Objects;
import io.Adrestus.p2p.kademlia.node.DHTBootstrapNode;
import io.Adrestus.p2p.kademlia.node.DHTRegularNode;

public class CachedKademliaNodes {

    private static volatile CachedKademliaNodes instance;
    private DHTBootstrapNode dhtBootstrapNode;
    private DHTRegularNode dhtRegularNode;

    /**
     * private constructor to prevent client from instantiating.
     */
    private CachedKademliaNodes() {
    }

    /**
     * Public accessor.
     *
     * @return an instance of the class.
     */
    public static CachedKademliaNodes getInstance() {
        // local variable increases performance by 25 percent
        // Joshua Bloch "Effective Java, Second Edition", p. 283-284

        var result = instance;
        // Check if singleton instance is initialized.
        // If it is initialized then we can return the instance.
        if (result == null) {
            // It is not initialized but we cannot be sure because some other thread might have
            // initialized it in the meanwhile.
            // So to make sure we need to lock on an object to get mutual exclusion.
            synchronized (CachedKademliaNodes.class) {
                // Again assign the instance to local variable to check if it was initialized by some
                // other thread while current thread was blocked to enter the locked zone.
                // If it was initialized then we can return the previously created instance
                // just like the previous null check.
                result = instance;
                if (result == null) {
                    // The instance is still not initialized so we can safely
                    // (no other thread can enter this zone)
                    // create an instance and make it our singleton instance.
                    instance = result = new CachedKademliaNodes();
                }
            }
        }
        return result;
    }

    public DHTBootstrapNode getDhtBootstrapNode() {
        return dhtBootstrapNode;
    }

    public void setDhtBootstrapNode(DHTBootstrapNode dhtBootstrapNode) {
        this.dhtBootstrapNode = dhtBootstrapNode;
    }

    public DHTRegularNode getDhtRegularNode() {
        return dhtRegularNode;
    }

    public void setDhtRegularNode(DHTRegularNode dhtRegularNode) {
        this.dhtRegularNode = dhtRegularNode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CachedKademliaNodes that = (CachedKademliaNodes) o;
        return Objects.equal(dhtBootstrapNode, that.dhtBootstrapNode) && Objects.equal(dhtRegularNode, that.dhtRegularNode);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(dhtBootstrapNode, dhtRegularNode);
    }

    @Override
    public String toString() {
        return "DHTCachedNodes{" +
                "dhtBootstrapNode=" + dhtBootstrapNode +
                ", dhtRegularNode=" + dhtRegularNode +
                '}';
    }
}
