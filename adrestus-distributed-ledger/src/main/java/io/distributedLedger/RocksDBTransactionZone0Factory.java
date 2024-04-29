package io.distributedLedger;

public class RocksDBTransactionZone0Factory {
    private static volatile RocksDBTransactionZone0Factory instance;

    private static RocksDBConnectionManager rocksDBConnectionManager;

    private RocksDBTransactionZone0Factory(Class key, Class value) {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        this.rocksDBConnectionManager = new RocksDBConnectionManager<>(key, value, DatabaseInstance.ZONE_0_TRANSACTION_BLOCK);
    }

    public static RocksDBTransactionZone0Factory getInstance(Class key, Class value) {
        var result = instance;
        if (result == null) {
            synchronized (RocksDBTransactionZone0Factory.class) {
                result = instance;
                if (result == null) {
                    result = new RocksDBTransactionZone0Factory(key, value);
                    instance = result;
                }
            }
        } else if (rocksDBConnectionManager != null) {
            synchronized (RocksDBTransactionZone0Factory.class) {
                if (ZoneDatabaseFactory.isDatabaseInstanceClosed(DatabaseInstance.ZONE_0_TRANSACTION_BLOCK)) {
                    instance = null;
                    result = new RocksDBTransactionZone0Factory(key, value);
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
