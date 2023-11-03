package io.distributedLedger;

import lombok.SneakyThrows;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

import java.io.File;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

public class DatabaseRawReceiptInstance implements IDriver<Options, DB> {
    private static volatile DatabaseRawReceiptInstance instance;
    private static DB level_db;

    private DatabaseRawReceiptInstance() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
    }

    public static DatabaseRawReceiptInstance getInstance(Options options, String path) {

        var result = instance;
        if (result == null) {
            synchronized (DatabaseRawReceiptInstance.class) {
                result = instance;
                if (result == null) {
                    instance = result = new DatabaseRawReceiptInstance();
                    load_connection(options, path);
                }
            }
        }
        return result;
    }

    @SneakyThrows
    private static void load_connection(Options options, String path) {
        level_db = factory.open(new File(path), options);
    }

    @SneakyThrows
    @Override
    public void close(Options options) {
        level_db.close();
        instance = null;
        level_db = null;
        options = null;
    }

    @Override
    public DB getDB() {
        return level_db;
    }
}
