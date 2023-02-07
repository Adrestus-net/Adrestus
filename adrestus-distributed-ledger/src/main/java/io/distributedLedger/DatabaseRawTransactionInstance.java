package io.distributedLedger;

import lombok.SneakyThrows;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

import java.io.File;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

public class DatabaseRawTransactionInstance implements IDriver<Options, DB> {
    private static volatile DatabaseRawTransactionInstance instance;
    private static DB level_db;

    private DatabaseRawTransactionInstance() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
    }

    public static DatabaseRawTransactionInstance getInstance(Options options, String path) {

        var result = instance;
        if (result == null) {
            synchronized (DatabaseRawTransactionInstance.class) {
                result = instance;
                if (result == null) {
                    instance = result = new DatabaseRawTransactionInstance();
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
