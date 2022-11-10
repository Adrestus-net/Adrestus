package io.distributedLedger;

import java.lang.reflect.Type;

public class DatabaseFactory {

    private final Class key;
    private final Class value;
    private final Type value_type;

    public DatabaseFactory(Class key, Class value) {
        this.key = key;
        this.value = value;
        this.value_type = null;
    }

    public DatabaseFactory(Class key, Type type) {
        this.key = key;
        this.value = null;
        this.value_type = type;
    }

    public IDatabase getDatabase(DatabaseType type) {

        switch (type) {
            case LEVEL_DB:
                if (value_type == null)
                    return (IDatabase) LevelDBConnectionManager.getInstance(key, value);
                else
                    return (IDatabase) LevelDBConnectionManager.getInstance(key, value_type);
            case ROCKS_DB:
                return (IDatabase) RocksDBConnectionManager.getInstance(key, value);
            default:
                throw new IllegalArgumentException("Database not supported.");
        }
    }
}
