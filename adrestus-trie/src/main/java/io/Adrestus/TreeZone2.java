package io.Adrestus;

public class TreeZone2 implements ITreeZone {
    private static volatile TreeZone2 instance;
    private IMemoryTreePool memoryTreePool;

    private TreeZone2() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        this.memoryTreePool = new MemoryTreePool();
    }

    public static TreeZone2 getInstance() {

        var result = instance;
        if (result == null) {
            synchronized (TreeZone2.class) {
                result = instance;
                if (result == null) {
                    instance = result = new TreeZone2();
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
    public void setTree(MemoryTreePool iMemoryTreePool) {
        this.memoryTreePool = iMemoryTreePool;
    }

    @Override
    public void clear() {
        this.memoryTreePool = null;
        instance = null;
    }
}
