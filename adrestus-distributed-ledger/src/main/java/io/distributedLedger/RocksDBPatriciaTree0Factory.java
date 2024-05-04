package io.distributedLedger;

public class RocksDBPatriciaTree0Factory {
    private static volatile RocksDBPatriciaTree0Factory instance;

    private static RocksDBConnectionManager rocksDBConnectionManager;

    private RocksDBPatriciaTree0Factory(Class key, Class value, PatriciaTreeInstance patriciaTreeInstance) {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        this.rocksDBConnectionManager = new RocksDBConnectionManager<>(key, value, patriciaTreeInstance);
    }

    public static RocksDBPatriciaTree0Factory getInstance(Class key, Class value, PatriciaTreeInstance patriciaTreeInstance) {
        var result = instance;
        if (result == null) {
            synchronized (RocksDBPatriciaTree0Factory.class) {
                result = instance;
                if (result == null) {
                    result = new RocksDBPatriciaTree0Factory(key, value, patriciaTreeInstance);
                    instance = result;
                }
            }
        } else if (rocksDBConnectionManager != null) {
            synchronized (RocksDBPatriciaTree0Factory.class) {
                if (ZoneDatabaseFactory.isPatriciaTreeInstanceClosed(PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_0)) {
                    instance = null;
                    result = new RocksDBPatriciaTree0Factory(key, value, patriciaTreeInstance);
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
