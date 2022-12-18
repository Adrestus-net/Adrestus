package io.Adrestus;

public class TreeZone3 implements ITreeZone {
    private static volatile TreeZone3 instance;
    private final IMemoryTreePool memoryTreePool;

    private TreeZone3() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        this.memoryTreePool = new MemoryTreePool();
    }

    public static TreeZone3 getInstance() {

        var result = instance;
        if (result == null) {
            synchronized (TreeZone3.class) {
                result = instance;
                if (result == null) {
                    instance = result = new TreeZone3();
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
