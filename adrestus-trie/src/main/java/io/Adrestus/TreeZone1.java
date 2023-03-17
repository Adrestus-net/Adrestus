package io.Adrestus;

public class TreeZone1 implements ITreeZone {
    private static volatile TreeZone1 instance;
    private IMemoryTreePool memoryTreePool;

    private TreeZone1() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        this.memoryTreePool = new MemoryTreePool();
    }

    public static TreeZone1 getInstance() {

        var result = instance;
        if (result == null) {
            synchronized (TreeZone1.class) {
                result = instance;
                if (result == null) {
                    instance = result = new TreeZone1();
                }
            }
        }
        return result;
    }

    @Override
    public IMemoryTreePool getTree() {
        return this.memoryTreePool;
    }

    @Override
    public void setTree(IMemoryTreePool iMemoryTreePool) {
        this.memoryTreePool = iMemoryTreePool;
    }
}
