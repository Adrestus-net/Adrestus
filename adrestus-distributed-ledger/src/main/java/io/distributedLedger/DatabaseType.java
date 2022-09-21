package io.distributedLedger;

public enum DatabaseType {

    LEVEL_DB("LEVEL_DB"),
    ROCKS_DB("ROCKS_DB");

    private final String title;

    DatabaseType(String title) {
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
