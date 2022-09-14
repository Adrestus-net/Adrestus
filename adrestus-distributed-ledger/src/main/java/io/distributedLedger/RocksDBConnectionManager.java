package io.distributedLedger;

public class RocksDBConnectionManager implements IDriver<RocksDBConnectionManager>, IDatabase{

    private static volatile RocksDBConnectionManager instance;

    private RocksDBConnectionManager() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
    }


    public static synchronized RocksDBConnectionManager getInstance() {
        if (instance == null) {
            synchronized (RocksDBConnectionManager.class) {
                if (instance == null) {
                    instance = new RocksDBConnectionManager();
                }
            }
        }
        return instance;
    }


    @Override
    public RocksDBConnectionManager get() {
        return instance;
    }


    @Override
    public void setupOptions() {

    }

    @Override
    public void load_connection() {

    }
}
