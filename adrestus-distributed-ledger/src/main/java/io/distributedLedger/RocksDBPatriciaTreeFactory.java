package io.distributedLedger;

public class RocksDBPatriciaTreeFactory {
    private static volatile RocksDBPatriciaTreeFactory instance;

    private static RocksDBConnectionManager rocksDBConnectionManager;

    private RocksDBPatriciaTreeFactory(Class key, Class value, PatriciaTreeInstance patriciaTreeInstance) {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        this.rocksDBConnectionManager = new RocksDBConnectionManager<>(key, value, patriciaTreeInstance);
    }

    public static RocksDBPatriciaTreeFactory getInstance(Class key, Class value, PatriciaTreeInstance patriciaTreeInstance) {
        var result = instance;
        if (result == null) {
            synchronized (RocksDBPatriciaTreeFactory.class) {
                result = instance;
                if (result == null) {
                    result = new RocksDBPatriciaTreeFactory(key, value, patriciaTreeInstance);
                    instance = result;
                }
            }
        } else if (rocksDBConnectionManager != null) {
            synchronized (RocksDBPatriciaTreeFactory.class) {
                if (ZoneDatabaseFactory.isPatriciaTreeInstanceClosed(PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_0)) {
                    instance = null;
                    result = new RocksDBPatriciaTreeFactory(key, value, patriciaTreeInstance);
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
