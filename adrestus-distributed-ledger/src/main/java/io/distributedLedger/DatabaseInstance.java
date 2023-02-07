package io.distributedLedger;

public enum DatabaseInstance {
    COMMITTEE_BLOCK("COMMITTEE_BLOCK"),
    RAW_TRANSACTION("RAW_TRANSACTION"),
    ZONE_0_TRANSACTION_BLOCK(ZoneInstance.ZONE_0.getTitle() + "\\" + "TRANSACTION_BLOCK_0"),
    ZONE_1_TRANSACTION_BLOCK(ZoneInstance.ZONE_1.getTitle() + "\\" + "TRANSACTION_BLOCK_1"),
    ZONE_2_TRANSACTION_BLOCK(ZoneInstance.ZONE_2.getTitle() + "\\" + "TRANSACTION_BLOCK_2"),
    ZONE_3_TRANSACTION_BLOCK(ZoneInstance.ZONE_3.getTitle() + "\\" + "TRANSACTION_BLOCK_3");

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
