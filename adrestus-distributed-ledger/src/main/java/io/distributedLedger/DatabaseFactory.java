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

    public DatabaseFactory(Class key, Class value, Type type) {
        this.key = key;
        this.value = value;
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
                return (IDatabase) RocksDBCommitteeFactory.getInstance(key, value).getRocksDBConnectionManager();
            default:
                throw new IllegalArgumentException("Database not supported.");
        }
    }

    public IDatabase getDatabase(DatabaseType type, DatabaseInstance instance) {

        switch (type) {
            case LEVEL_DB:
                if (value_type == null)
                    return (IDatabase) LevelDBConnectionManager.getInstance(key, value);
                else
                    return (IDatabase) LevelDBConnectionManager.getInstance(key, value_type);
            case ROCKS_DB:
                switch (instance) {
                    case ZONE_0_TRANSACTION_BLOCK:
                        return (IDatabase) RocksDBTransactionZone0Factory.getInstance(key, value).getRocksDBConnectionManager();
                    case ZONE_1_TRANSACTION_BLOCK:
                        return (IDatabase) RocksDBTransactionZone1Factory.getInstance(key, value).getRocksDBConnectionManager();
                    case ZONE_2_TRANSACTION_BLOCK:
                        return (IDatabase) RocksDBTransactionZone2Factory.getInstance(key, value).getRocksDBConnectionManager();
                    case ZONE_3_TRANSACTION_BLOCK:
                        return (IDatabase) RocksDBTransactionZone3Factory.getInstance(key, value).getRocksDBConnectionManager();
                    case COMMITTEE_BLOCK:
                        return (IDatabase) RocksDBCommitteeFactory.getInstance(key, value).getRocksDBConnectionManager();
                }
            default:
                throw new IllegalArgumentException("Database not supported.");
        }
    }

    public IDatabase getDatabase(DatabaseType type, PatriciaTreeInstance instance) {

        switch (type) {
            case LEVEL_DB:
                if (value_type == null)
                    return (IDatabase) LevelDBConnectionManager.getInstance(key, value);
                else
                    return (IDatabase) LevelDBConnectionManager.getInstance(key, value_type);
            case ROCKS_DB:
                switch (instance) {
                    case PATRICIA_TREE_INSTANCE_0:
                        return (IDatabase) RocksDBPatriciaTree0Factory.getInstance(key, value, instance).getRocksDBConnectionManager();
                    case PATRICIA_TREE_INSTANCE_1:
                        return (IDatabase) RocksDBPatriciaTree1Factory.getInstance(key, value, instance).getRocksDBConnectionManager();
                    case PATRICIA_TREE_INSTANCE_2:
                        return (IDatabase) RocksDBPatriciaTree2Factory.getInstance(key, value, instance).getRocksDBConnectionManager();
                    case PATRICIA_TREE_INSTANCE_3:
                        return (IDatabase) RocksDBPatriciaTree3Factory.getInstance(key, value, instance).getRocksDBConnectionManager();
                }
            default:
                throw new IllegalArgumentException("Database not supported.");
        }
    }
}
