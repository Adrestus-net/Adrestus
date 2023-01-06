package io.distributedLedger;

public enum PatriciaTreeInstance {
    PATRICIA_TREE_INSTANCE_0(ZoneInstance.ZONE_0.getTitle() + "\\" + "PATRICIA_TREE_INSTANCE_0"),
    PATRICIA_TREE_INSTANCE_1(ZoneInstance.ZONE_1.getTitle() + "\\" + "PATRICIA_TREE_INSTANCE_1"),
    PATRICIA_TREE_INSTANCE_2(ZoneInstance.ZONE_2.getTitle() + "\\" + "PATRICIA_TREE_INSTANCE_2"),
    PATRICIA_TREE_INSTANCE_3(ZoneInstance.ZONE_3.getTitle() + "\\" + "PATRICIA_TREE_INSTANCE_3");

    private final String title;

    PatriciaTreeInstance(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return "DatabaseType{" +
                "title='" + title + '\'' +
                '}';
    }
}
