package io.distributedLedger;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;

public interface IDriver<T> {

    void close(Options options);

    RocksDB getRocksDB();

}
