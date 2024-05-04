package io.distributedLedger;

import lombok.SneakyThrows;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;

public class DatabasePatriciaTreeZone0 implements IDriver<Options, RocksDB> {
    private static volatile DatabasePatriciaTreeZone0 instance;
    private static RocksDB rocksDB;

    private DatabasePatriciaTreeZone0() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
    }

    public static DatabasePatriciaTreeZone0 getInstance(Options options, String path) {

        var result = instance;
        if (result == null) {
            synchronized (DatabasePatriciaTreeZone0.class) {
                result = instance;
                if (result == null) {
                    instance = result = new DatabasePatriciaTreeZone0();
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
        DatabasePatriciaTreeZone0.rocksDB = rocksDB;
    }
}
