package io.Adrestus.core;

public enum TransactionType {
    REGULAR("REGULAR"),
    STAKING("STAKING"),
    DELEGATING("DELEGATING"),
    REWARDS("REWARDS");
    private final String title;

    TransactionType(String title) {
        this.title = title;
    }


}
