package io.distributedLedger;

public enum DatabaseInstance {
    COMMITTEE_BLOCK("COMMITTEE_BLOCK"),
    ZONE_0_TRANSACTION_BLOCK("ZONE_0_TRANSACTION_BLOCK"),
    ZONE_1_TRANSACTION_BLOCK("ZONE_1_TRANSACTION_BLOCK"),
    ZONE_2_TRANSACTION_BLOCK("ZONE_2_TRANSACTION_BLOCK"),
    ZONE_3_TRANSACTION_BLOCK("ZONE_3_TRANSACTION_BLOCK");

    private final String title;

    DatabaseInstance(String title) {
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
