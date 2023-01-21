package io.Adrestus.consensus;

public enum ConsensusType {
    VDF("VDF"),
    VRF("VRF"),
    TRANSACTION_BLOCK("TRANSACTION_BLOCK"),
    COMMITTEE_BLOCK("COMMITTEE_BLOCK"),
    CHANGE_VIEW_TRANSACTION_BLOCK("CHANGE_VIEW_TRANSACTION_BLOCK"),
    CHANGE_VIEW_COMMITTEE_BLOCK("CHANGE_VIEW_COMMITTEE_BLOCK");
    private final String title;

    ConsensusType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
