package io.distributedLedger;

import java.util.function.Supplier;

public enum DatabaseType {

    PAL_DB(PalDBConnectionManager::getInstance),
    ROCKS_DB(RocksDBConnectionManager::getInstance);

    private final IDriver constructor;


    DatabaseType(IDriver constructor) {
        this.constructor = constructor;
    }

    public IDriver getConstructor() {
        return constructor;
    }

    @Override
    public String toString() {
        return "DatabaseType{" +
                "constructor=" + constructor +
                '}';
    }
}
