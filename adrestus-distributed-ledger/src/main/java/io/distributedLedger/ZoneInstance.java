package io.distributedLedger;

public enum ZoneInstance {
    ZONE_0("ZONE_0"),
    ZONE_1("ZONE_1"),
    ZONE_2("ZONE_2"),
    ZONE_3("ZONE_3");

    private final String title;

    ZoneInstance(String title) {
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
