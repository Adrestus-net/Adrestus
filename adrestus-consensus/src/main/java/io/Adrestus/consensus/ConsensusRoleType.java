package io.Adrestus.consensus;

public enum ConsensusRoleType {
    SUPERVISOR("SUPERVISOR"),
    ORGANIZER("ORGANIZER"),
    VALIDATOR("VALIDATOR");
    private final String title;

    ConsensusRoleType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
