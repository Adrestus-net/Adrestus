package io.Adrestus.consensus;

public enum ConsensusMessageType {
    VDF("VDF"),
    VRF("VRF"),
    TRANSACTION_BLOCK("TRANSACTION_BLOCK"),
    COMMITTEE_BLOCK("COMMITEE_BLOCK");
    private final String title;

    ConsensusMessageType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
