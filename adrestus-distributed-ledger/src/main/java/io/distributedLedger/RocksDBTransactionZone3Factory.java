package io.distributedLedger;

public class RocksDBTransactionZone3Factory {
    private static volatile RocksDBTransactionZone3Factory instance;

    private static RocksDBConnectionManager rocksDBConnectionManager;

    private RocksDBTransactionZone3Factory(Class key, Class value) {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        this.rocksDBConnectionManager = new RocksDBConnectionManager<>(key, value, DatabaseInstance.ZONE_3_TRANSACTION_BLOCK);
    }

    public static RocksDBTransactionZone3Factory getInstance(Class key, Class value) {
        var result = instance;
        if (result == null) {
            synchronized (RocksDBTransactionZone3Factory.class) {
                result = instance;
                if (result == null) {
                    result = new RocksDBTransactionZone3Factory(key, value);
                    instance = result;
                }
            }
        } else if (rocksDBConnectionManager != null) {
            synchronized (RocksDBTransactionZone3Factory.class) {
                if (ZoneDatabaseFactory.isDatabaseInstanceClosed(DatabaseInstance.ZONE_3_TRANSACTION_BLOCK)) {
                    instance = null;
                    result = new RocksDBTransactionZone3Factory(key, value);
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
