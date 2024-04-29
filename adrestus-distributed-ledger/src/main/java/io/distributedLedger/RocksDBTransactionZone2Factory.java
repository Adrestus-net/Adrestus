package io.distributedLedger;

public class RocksDBTransactionZone2Factory {
    private static volatile RocksDBTransactionZone2Factory instance;

    private static RocksDBConnectionManager rocksDBConnectionManager;

    private RocksDBTransactionZone2Factory(Class key, Class value) {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        this.rocksDBConnectionManager = new RocksDBConnectionManager<>(key, value, DatabaseInstance.ZONE_2_TRANSACTION_BLOCK);
    }

    public static RocksDBTransactionZone2Factory getInstance(Class key, Class value) {
        var result = instance;
        if (result == null) {
            synchronized (RocksDBTransactionZone2Factory.class) {
                result = instance;
                if (result == null) {
                    result = new RocksDBTransactionZone2Factory(key, value);
                    instance = result;
                }
            }
        } else if (rocksDBConnectionManager != null) {
            synchronized (RocksDBTransactionZone2Factory.class) {
                if (ZoneDatabaseFactory.isDatabaseInstanceClosed(DatabaseInstance.ZONE_2_TRANSACTION_BLOCK)) {
                    instance = null;
                    result = new RocksDBTransactionZone2Factory(key, value);
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
