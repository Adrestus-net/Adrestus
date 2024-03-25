package io.Adrestus.consensus;

public enum ConsensusMessageType {

    DISPERSE("DISPERSE"),
    ANNOUNCE("ANNOUNCE"),
    PREPARE("PREPARE"),
    COMMIT("COMMIT");
    private final String title;

    ConsensusMessageType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
