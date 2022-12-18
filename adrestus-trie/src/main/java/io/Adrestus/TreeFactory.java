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


}
