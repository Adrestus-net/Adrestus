package io.distributedLedger;

public class RocksDBPatriciaTree1Factory {
    private static volatile RocksDBPatriciaTree1Factory instance;

    private static RocksDBConnectionManager rocksDBConnectionManager;

    private RocksDBPatriciaTree1Factory(Class key, Class value, PatriciaTreeInstance patriciaTreeInstance) {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        this.rocksDBConnectionManager = new RocksDBConnectionManager<>(key, value, patriciaTreeInstance);
    }

    public static RocksDBPatriciaTree1Factory getInstance(Class key, Class value, PatriciaTreeInstance patriciaTreeInstance) {
        var result = instance;
        if (result == null) {
            synchronized (RocksDBPatriciaTree1Factory.class) {
                result = instance;
                if (result == null) {
                    result = new RocksDBPatriciaTree1Factory(key, value, patriciaTreeInstance);
                    instance = result;
                }
            }
        } else if (rocksDBConnectionManager != null) {
            synchronized (RocksDBPatriciaTree1Factory.class) {
                if (ZoneDatabaseFactory.isPatriciaTreeInstanceClosed(PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_1)) {
                    instance = null;
                    result = new RocksDBPatriciaTree1Factory(key, value, patriciaTreeInstance);
                    instance = result;
                }
            }
        }
        return result;
    }

    public RocksDBConnectionManager getRocksDBConnectionManager() {
        return rocksDBConnectionManager;
    }
}
