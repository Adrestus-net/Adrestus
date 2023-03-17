package io.Adrestus;

public final class TreeFactory {

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
                TreeZone0.getInstance().setTree(iMemoryTreePool);
            case 1:
                TreeZone1.getInstance().setTree(iMemoryTreePool);
            case 2:
                TreeZone2.getInstance().setTree(iMemoryTreePool);
            case 3:
                TreeZone3.getInstance().setTree(iMemoryTreePool);
            default:
                TreeZone0.getInstance().setTree(iMemoryTreePool);
        }
    }
}
