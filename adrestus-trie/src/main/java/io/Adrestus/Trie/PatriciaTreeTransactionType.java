package io.Adrestus.Trie;

public enum PatriciaTreeTransactionType {
    REGULAR("REGULAR"),
    STAKING("STAKING"),
    REWARDS("REWARDS"),
    DELEGATE("DELEGATE"),
    UNCLAIMED_FEE_REWARD("UNCLAIMED_FEE_REWARD"),
    UNSTAKING("UNSTAKING"),
    UNDELEGATE("UNDELEGATE");
    private final String title;

    PatriciaTreeTransactionType(String title) {
        this.title = title;
    }
}
