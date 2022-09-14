package io.distributedLedger;

public class DatabaseFactory {

    public static IDriver getCoin(DatabaseType type) {
        return (IDriver) type.getConstructor().get();
    }
}
