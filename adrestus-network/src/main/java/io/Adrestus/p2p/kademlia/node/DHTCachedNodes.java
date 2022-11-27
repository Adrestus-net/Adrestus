package io.Adrestus.p2p.kademlia.node;

import com.google.common.base.Objects;

public class DHTCachedNodes {

    private static volatile DHTCachedNodes instance;
    private DHTBootstrapNode dhtBootstrapNode;
    private DHTRegularNode dhtRegularNode;

    /**
     * private constructor to prevent client from instantiating.
     */
    private DHTCachedNodes() {
    }

    /**
     * Public accessor.
     *
     * @return an instance of the class.
     */
    public static DHTCachedNodes getInstance() {
        // local variable increases performance by 25 percent
        // Joshua Bloch "Effective Java, Second Edition", p. 283-284

        var result = instance;
        // Check if singleton instance is initialized.
        // If it is initialized then we can return the instance.
        if (result == null) {
            // It is not initialized but we cannot be sure because some other thread might have
            // initialized it in the meanwhile.
            // So to make sure we need to lock on an object to get mutual exclusion.
            synchronized (DHTCachedNodes.class) {
                // Again assign the instance to local variable to check if it was initialized by some
                // other thread while current thread was blocked to enter the locked zone.
                // If it was initialized then we can return the previously created instance
                // just like the previous null check.
                result = instance;
                if (result == null) {
                    // The instance is still not initialized so we can safely
                    // (no other thread can enter this zone)
                    // create an instance and make it our singleton instance.
                    instance = result = new DHTCachedNodes();
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
        DHTCachedNodes that = (DHTCachedNodes) o;
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
