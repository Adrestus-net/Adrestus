package io.distributedLedger;

import lombok.SneakyThrows;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;

public class DatabasePatriciaTreeZone implements IDriver<Options, RocksDB> {
    private static volatile DatabasePatriciaTreeZone instance;
    private static RocksDB rocksDB;

    private DatabasePatriciaTreeZone() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
    }

    public static DatabasePatriciaTreeZone getInstance(Options options, String path) {

        var result = instance;
        if (result == null) {
            synchronized (DatabasePatriciaTreeZone.class) {
                result = instance;
                if (result == null) {
                    instance = result = new DatabasePatriciaTreeZone();
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

    public static boolean isNull() {
        if (rocksDB == null)
            return true;
        return false;
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
        DatabasePatriciaTreeZone.rocksDB = rocksDB;
    }
}
