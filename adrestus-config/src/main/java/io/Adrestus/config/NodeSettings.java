package io.Adrestus.config;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class NodeSettings implements Serializable {

    private static volatile NodeSettings instance;

    public int alpha;
    public int identifierSize;
    /* Maximum size of the buckets */
    public int bucketSize;
    public int findNodeSize;
    public int maximumLastSeenAgeToConsiderAlive;

    public int pingScheduleTimeValue;
    public TimeUnit pingScheduleTimeUnit;
    public int dhtExecutorPoolSize;
    public int dhtScheduledExecutorPoolSize;
    public int maximumStoreAndLookupTimeoutValue;
    public TimeUnit maximumStoreAndGetTimeoutTimeUnit;
    public boolean enabledFirstStoreRequestForcePass;


    private NodeSettings() {
        // to prevent instantiating by Reflection call
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
    }

    public static NodeSettings getInstance() {
        var result = instance;
        if (result == null) {
            synchronized (NodeSettings.class) {
                result = instance;
                if (result == null) {
                    instance = new NodeSettings();
                    build();
                }
            }
        }
        return result;
    }

   private static NodeSettings build() {
        NodeSettings.getInstance().setAlpha(KademliaConfiguration.ALPHA);
        NodeSettings.getInstance().setIdentifierSize(KademliaConfiguration.IDENTIFIER_SIZE);
        NodeSettings.getInstance().setBucketSize(KademliaConfiguration.BUCKET_SIZE);
        NodeSettings.getInstance().setFindNodeSize(KademliaConfiguration.FIND_NODE_SIZE);
        NodeSettings.getInstance().setMaximumLastSeenAgeToConsiderAlive(KademliaConfiguration.MAXIMUM_LAST_SEEN_AGE_TO_CONSIDER_ALIVE);
        NodeSettings.getInstance().setPingScheduleTimeUnit(KademliaConfiguration.PING_SCHEDULE_TIME_UNIT);
        NodeSettings.getInstance().setPingScheduleTimeValue(KademliaConfiguration.PING_SCHEDULE_TIME_VALUE);
        NodeSettings.getInstance().setDhtExecutorPoolSize(KademliaConfiguration.DHT_EXECUTOR_POOL_SIZE);
        NodeSettings.getInstance().setDhtScheduledExecutorPoolSize(KademliaConfiguration.DHT_SCHEDULED_EXECUTOR_POOL_SIZE);
        NodeSettings.getInstance().setMaximumStoreAndLookupTimeoutValue(KademliaConfiguration.MAXIMUM_STORE_AND_LOOKUP_TIMEOUT_VALUE);
        NodeSettings.getInstance().setMaximumStoreAndGetTimeoutTimeUnit(KademliaConfiguration.MAXIMUM_STORE_AND_LOOKUP_TIMEOUT_TIME_UNIT);
        NodeSettings.getInstance().setEnabledFirstStoreRequestForcePass(KademliaConfiguration.ENABLED_FIRST_STORE_REQUEST_FORCE_PASS);
        return NodeSettings.getInstance();
    }

    public static void setInstance(NodeSettings instance) {
        NodeSettings.instance = instance;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public int getIdentifierSize() {
        return identifierSize;
    }

    public void setIdentifierSize(int identifierSize) {
        this.identifierSize = identifierSize;
    }

    public int getBucketSize() {
        return bucketSize;
    }

    public void setBucketSize(int bucketSize) {
        this.bucketSize = bucketSize;
    }

    public int getFindNodeSize() {
        return findNodeSize;
    }

    public void setFindNodeSize(int findNodeSize) {
        this.findNodeSize = findNodeSize;
    }

    public int getMaximumLastSeenAgeToConsiderAlive() {
        return maximumLastSeenAgeToConsiderAlive;
    }

    public void setMaximumLastSeenAgeToConsiderAlive(int maximumLastSeenAgeToConsiderAlive) {
        this.maximumLastSeenAgeToConsiderAlive = maximumLastSeenAgeToConsiderAlive;
    }

    public int getPingScheduleTimeValue() {
        return pingScheduleTimeValue;
    }

    public void setPingScheduleTimeValue(int pingScheduleTimeValue) {
        this.pingScheduleTimeValue = pingScheduleTimeValue;
    }

    public TimeUnit getPingScheduleTimeUnit() {
        return pingScheduleTimeUnit;
    }

    public void setPingScheduleTimeUnit(TimeUnit pingScheduleTimeUnit) {
        this.pingScheduleTimeUnit = pingScheduleTimeUnit;
    }

    public int getDhtExecutorPoolSize() {
        return dhtExecutorPoolSize;
    }

    public void setDhtExecutorPoolSize(int dhtExecutorPoolSize) {
        this.dhtExecutorPoolSize = dhtExecutorPoolSize;
    }

    public int getDhtScheduledExecutorPoolSize() {
        return dhtScheduledExecutorPoolSize;
    }

    public void setDhtScheduledExecutorPoolSize(int dhtScheduledExecutorPoolSize) {
        this.dhtScheduledExecutorPoolSize = dhtScheduledExecutorPoolSize;
    }

    public int getMaximumStoreAndLookupTimeoutValue() {
        return maximumStoreAndLookupTimeoutValue;
    }

    public void setMaximumStoreAndLookupTimeoutValue(int maximumStoreAndLookupTimeoutValue) {
        this.maximumStoreAndLookupTimeoutValue = maximumStoreAndLookupTimeoutValue;
    }

    public TimeUnit getMaximumStoreAndGetTimeoutTimeUnit() {
        return maximumStoreAndGetTimeoutTimeUnit;
    }

    public void setMaximumStoreAndGetTimeoutTimeUnit(TimeUnit maximumStoreAndGetTimeoutTimeUnit) {
        this.maximumStoreAndGetTimeoutTimeUnit = maximumStoreAndGetTimeoutTimeUnit;
    }

    public boolean isEnabledFirstStoreRequestForcePass() {
        return enabledFirstStoreRequestForcePass;
    }

    public void setEnabledFirstStoreRequestForcePass(boolean enabledFirstStoreRequestForcePass) {
        this.enabledFirstStoreRequestForcePass = enabledFirstStoreRequestForcePass;
    }
}
