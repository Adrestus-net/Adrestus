package io.Adrestus;

public class TreeZone0 implements ITreeZone {
    private static volatile TreeZone0 instance;
    private final IMemoryTreePool memoryTreePool;

    private TreeZone0() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        this.memoryTreePool = new MemoryTreePool();
    }

    public static TreeZone0 getInstance() {

        var result = instance;
        if (result == null) {
            synchronized (TreeZone0.class) {
                result = instance;
                if (result == null) {
                    instance = result = new TreeZone0();
                }
            }
        }
        return result;
    }

    @Override
    public IMemoryTreePool getTree() {
        return this.memoryTreePool;
    }
}
