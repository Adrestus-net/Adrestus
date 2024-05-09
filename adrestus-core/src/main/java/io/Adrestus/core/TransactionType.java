package io.Adrestus.core;

public enum TransactionType {
    REGULAR("REGULAR"),
    STAKING("STAKING"),
    DELEGATING("DELEGATING"),
    REWARDS("REWARDS"),
    UNCLAIMED_FEE_REWARD("UNCLAIMED_FEE_REWARD");
    private final String title;

    TransactionType(String title) {
        this.title = title;
    }


}
