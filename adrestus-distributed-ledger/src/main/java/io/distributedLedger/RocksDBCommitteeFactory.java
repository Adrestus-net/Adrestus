package io.distributedLedger;

public class RocksDBCommitteeFactory {
    private static volatile RocksDBCommitteeFactory instance;

    private static RocksDBConnectionManager rocksDBConnectionManager;

    private RocksDBCommitteeFactory(Class key, Class value) {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        this.rocksDBConnectionManager = new RocksDBConnectionManager<>(key, value);
    }

    public static RocksDBCommitteeFactory getInstance(Class key, Class value) {
        var result = instance;
        if (result == null) {
            synchronized (RocksDBCommitteeFactory.class) {
                result = instance;
                if (result == null) {
                    result = new RocksDBCommitteeFactory(key, value);
                    instance = result;
                }
            }
        } else if (rocksDBConnectionManager != null) {
            synchronized (RocksDBCommitteeFactory.class) {
                if (ZoneDatabaseFactory.isDatabaseInstanceClosed(DatabaseInstance.COMMITTEE_BLOCK)) {
                    instance = null;
                    result = new RocksDBCommitteeFactory(key, value);
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
