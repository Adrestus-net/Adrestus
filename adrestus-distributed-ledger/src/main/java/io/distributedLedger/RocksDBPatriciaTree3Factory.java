package io.distributedLedger;

public class RocksDBPatriciaTree3Factory {
    private static volatile RocksDBPatriciaTree3Factory instance;

    private static RocksDBConnectionManager rocksDBConnectionManager;

    private RocksDBPatriciaTree3Factory(Class key, Class value, PatriciaTreeInstance patriciaTreeInstance) {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        this.rocksDBConnectionManager = new RocksDBConnectionManager<>(key, value, patriciaTreeInstance);
    }

    public static RocksDBPatriciaTree3Factory getInstance(Class key, Class value, PatriciaTreeInstance patriciaTreeInstance) {
        var result = instance;
        if (result == null) {
            synchronized (RocksDBPatriciaTree3Factory.class) {
                result = instance;
                if (result == null) {
                    result = new RocksDBPatriciaTree3Factory(key, value, patriciaTreeInstance);
                    instance = result;
                }
            }
        } else if (rocksDBConnectionManager != null) {
            synchronized (RocksDBPatriciaTree3Factory.class) {
                if (ZoneDatabaseFactory.isPatriciaTreeInstanceClosed(PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_3)) {
                    instance = null;
                    result = new RocksDBPatriciaTree3Factory(key, value, patriciaTreeInstance);
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
