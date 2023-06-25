package io.distributedLedger;

import lombok.SneakyThrows;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;

public class DatabaseCommitteeZone implements IDriver<Options, RocksDB> {
    private static volatile DatabaseCommitteeZone instance;
    private static RocksDB rocksDB;

    private DatabaseCommitteeZone() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
    }

    public static DatabaseCommitteeZone getInstance(Options options, String path) {

        var result = instance;
        if (result == null || rocksDB==null) {
            synchronized (DatabaseCommitteeZone.class) {
                result = instance;
                if (result == null) {
                    instance = result = new DatabaseCommitteeZone();
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
    public RocksDB getDB() {
        return rocksDB;
    }

    public void setRocksDB(RocksDB rocksDB) {
        DatabaseCommitteeZone.rocksDB = rocksDB;
    }
}
