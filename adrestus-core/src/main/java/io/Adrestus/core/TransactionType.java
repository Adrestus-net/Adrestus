package io.Adrestus.core;

public enum TransactionType {
    ORDINARY("ORDINARY"),
    STAKING("STAKING"),
    DELEGATING("DELEGATING"),
    REWARDS("REWARDS");
    private final String title;

    TransactionType(String title) {
        this.title = title;
    }


}
