package io.distributedLedger;

import lombok.SneakyThrows;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;

public class DatabaseZone0 implements IDriver {
    private static volatile DatabaseZone0 instance;
    private static RocksDB rocksDB;

    private DatabaseZone0() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
    }

    public static DatabaseZone0 getInstance(Options options, String path) {

        var result = instance;
        if (result == null) {
            synchronized (DatabaseZone0.class) {
                result = instance;
                if (result == null) {
                    instance = result = new DatabaseZone0();
                    load_connection(options, path);
                }
            }
        }
        return result;
    }

    @SneakyThrows
    private static void load_connection(Options options, String path) {
        if (instance != null) {
            rocksDB = RocksDB.open(options, path);
        }
    }

    @Override
    public void close(Options options) {
        options.close();
        rocksDB.close();
        instance = null;
        rocksDB = null;
        options=null;
    }

    @Override
    public RocksDB getRocksDB() {
        return rocksDB;
    }

    public void setRocksDB(RocksDB rocksDB) {
        DatabaseZone0.rocksDB = rocksDB;
    }
}
