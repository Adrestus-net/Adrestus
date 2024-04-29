package io.distributedLedger;

import lombok.SneakyThrows;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;

public class DatabaseTransactionZone3 implements IDriver<Options, RocksDB> {
    private static volatile DatabaseTransactionZone3 instance;
    private static RocksDB rocksDB;

    private DatabaseTransactionZone3() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
    }

    public static DatabaseTransactionZone3 getInstance(Options options, String path) {

        var result = instance;
        if (result == null) {
            synchronized (DatabaseTransactionZone3.class) {
                result = instance;
                if (result == null) {
                    instance = result = new DatabaseTransactionZone3();
                    load_connection(options, path);
                }
            }
        }
        return result;
    }

    public static boolean isNull() {
        if (rocksDB == null)
            return true;
        return false;
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
        options = null;
    }

    @Override
    public RocksDB getDB() {
        return rocksDB;
    }

    public void setRocksDB(RocksDB rocksDB) {
        DatabaseTransactionZone3.rocksDB = rocksDB;
    }
}
