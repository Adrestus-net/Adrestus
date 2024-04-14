package io.Adrestus;

public class TreeFactory {
    private static volatile TreeFactory instance;


    private TreeFactory() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
    }

    public static IMemoryTreePool getMemoryTree(int zone) {
        switch (zone) {
            case 0:
                return TreeZone0.getInstance().getTree();
            case 1:
                return TreeZone1.getInstance().getTree();
            case 2:
                return TreeZone2.getInstance().getTree();
            case 3:
                return TreeZone3.getInstance().getTree();
            default:
                return TreeZone0.getInstance().getTree();
        }
    }


    public static void setMemoryTree(IMemoryTreePool iMemoryTreePool, int zone) {
        switch (zone) {
            case 0:
                TreeZone0.getInstance().setTree((MemoryTreePool) iMemoryTreePool);
                break;
            case 1:
                TreeZone1.getInstance().setTree((MemoryTreePool) iMemoryTreePool);
                break;
            case 2:
                TreeZone2.getInstance().setTree((MemoryTreePool) iMemoryTreePool);
                break;
            case 3:
                TreeZone3.getInstance().setTree((MemoryTreePool) iMemoryTreePool);
                break;
        }
    }
}
