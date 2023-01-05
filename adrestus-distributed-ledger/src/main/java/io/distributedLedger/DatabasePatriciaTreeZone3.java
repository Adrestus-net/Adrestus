package io.distributedLedger;

import lombok.SneakyThrows;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;

public class DatabasePatriciaTreeZone3 implements IDriver {
    private static volatile DatabasePatriciaTreeZone3 instance;
    private static RocksDB rocksDB;

    private DatabasePatriciaTreeZone3() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
    }

    public static DatabasePatriciaTreeZone3 getInstance(Options options, String path) {

        var result = instance;
        if (result == null) {
            synchronized (DatabasePatriciaTreeZone2.class) {
                result = instance;
                if (result == null) {
                    instance = result = new DatabasePatriciaTreeZone3();
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
        options = null;
    }

    @Override
    public RocksDB getRocksDB() {
        return rocksDB;
    }

    public void setRocksDB(RocksDB rocksDB) {
        DatabasePatriciaTreeZone3.rocksDB = rocksDB;
    }
}
