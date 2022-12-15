package io.distributedLedger;

import lombok.SneakyThrows;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;

public class DatabaseZone3 implements IDriver {
    private static volatile DatabaseZone3 instance;
    private static RocksDB rocksDB;

    private DatabaseZone3() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
    }

    public static DatabaseZone3 getInstance(Options options, String path) {

        var result = instance;
        if (result == null) {
            synchronized (DatabaseZone3.class) {
                result = instance;
                if (result == null) {
                    instance = result = new DatabaseZone3();
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
        DatabaseZone3.rocksDB = rocksDB;
    }
}
