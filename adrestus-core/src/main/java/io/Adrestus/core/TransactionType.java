package io.Adrestus.core;

public enum TransactionType {
    REGULAR("REGULAR"),
    STAKING("STAKING"),
    DELEGATE("DELEGATE"),
    REWARDS("REWARDS"),
    UNCLAIMED_FEE_REWARD("UNCLAIMED_FEE_REWARD"),
    UNSTAKING("UNSTAKING"),
    UNDELEGATE("UNDELEGATE");
    private final String title;

    TransactionType(String title) {
        this.title = title;
    }


}
