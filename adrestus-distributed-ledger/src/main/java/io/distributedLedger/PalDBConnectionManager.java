package io.distributedLedger;

public class PalDBConnectionManager implements IDriver<PalDBConnectionManager>, IDatabase {

    private static volatile PalDBConnectionManager instance;

    private PalDBConnectionManager() {
        // Protect against instantiation via reflection
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        } else {
            load_connection();
        }
    }

    public static synchronized PalDBConnectionManager getInstance() {
        if (instance == null) {
            synchronized (PalDBConnectionManager.class) {
                if (instance == null) {
                    instance = new PalDBConnectionManager();
                }
            }
        }
        return instance;
    }

    @Override
    public PalDBConnectionManager get() {
        return instance;
    }

    @Override
    public void setupOptions() {

    }

    @Override
    public void load_connection() {

    }
}
