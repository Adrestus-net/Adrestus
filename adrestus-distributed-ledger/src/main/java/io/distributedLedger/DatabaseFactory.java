package io.distributedLedger;

public class DatabaseFactory {

    private final Class key;
    private final Class value;

    public DatabaseFactory(Class key, Class value) {
        this.key = key;
        this.value = value;
    }

    public IDatabase getDatabase(DatabaseType type) {

        switch (type) {
            case LEVEL_DB:
                return (IDatabase) LevelDBConnectionManager.getInstance(key, value);
            case ROCKS_DB:
                return (IDatabase) RocksDBConnectionManager.getInstance(key, value);
            default:
                throw new IllegalArgumentException("Database not supported.");
        }
    }
}
