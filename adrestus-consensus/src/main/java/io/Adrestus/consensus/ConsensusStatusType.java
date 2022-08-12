package io.Adrestus.consensus;

public enum ConsensusStatusType {
    SUCCESS("SUCCESS"),
    ABORT("ABORT"),
    PENDING("PENDING");
    private final String title;

    ConsensusStatusType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
