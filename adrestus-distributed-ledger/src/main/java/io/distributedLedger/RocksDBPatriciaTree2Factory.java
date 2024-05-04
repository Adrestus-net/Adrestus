package io.distributedLedger;

public class RocksDBPatriciaTree2Factory {
    private static volatile RocksDBPatriciaTree2Factory instance;

    private static RocksDBConnectionManager rocksDBConnectionManager;

    private RocksDBPatriciaTree2Factory(Class key, Class value, PatriciaTreeInstance patriciaTreeInstance) {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        this.rocksDBConnectionManager = new RocksDBConnectionManager<>(key, value, patriciaTreeInstance);
    }

    public static RocksDBPatriciaTree2Factory getInstance(Class key, Class value, PatriciaTreeInstance patriciaTreeInstance) {
        var result = instance;
        if (result == null) {
            synchronized (RocksDBPatriciaTree2Factory.class) {
                result = instance;
                if (result == null) {
                    result = new RocksDBPatriciaTree2Factory(key, value, patriciaTreeInstance);
                    instance = result;
                }
            }
        } else if (rocksDBConnectionManager != null) {
            synchronized (RocksDBPatriciaTree2Factory.class) {
                if (ZoneDatabaseFactory.isPatriciaTreeInstanceClosed(PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_2)) {
                    instance = null;
                    result = new RocksDBPatriciaTree2Factory(key, value, patriciaTreeInstance);
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
