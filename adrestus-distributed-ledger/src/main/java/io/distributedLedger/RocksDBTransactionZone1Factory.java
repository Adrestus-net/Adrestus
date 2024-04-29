package io.distributedLedger;

public class RocksDBTransactionZone1Factory {

    private static volatile RocksDBTransactionZone1Factory instance;

    private static RocksDBConnectionManager rocksDBConnectionManager;

    private RocksDBTransactionZone1Factory(Class key, Class value) {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        this.rocksDBConnectionManager = new RocksDBConnectionManager<>(key, value, DatabaseInstance.ZONE_1_TRANSACTION_BLOCK);
    }

    public static RocksDBTransactionZone1Factory getInstance(Class key, Class value) {
        var result = instance;
        if (result == null) {
            synchronized (RocksDBTransactionZone1Factory.class) {
                result = instance;
                if (result == null) {
                    result = new RocksDBTransactionZone1Factory(key, value);
                    instance = result;
                }
            }
        } else if (rocksDBConnectionManager != null) {
            synchronized (RocksDBTransactionZone1Factory.class) {
                if (ZoneDatabaseFactory.isDatabaseInstanceClosed(DatabaseInstance.ZONE_1_TRANSACTION_BLOCK)) {
                    instance = null;
                    result = new RocksDBTransactionZone1Factory(key, value);
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
