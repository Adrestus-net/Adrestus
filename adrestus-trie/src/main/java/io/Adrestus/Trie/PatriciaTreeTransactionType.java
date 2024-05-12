package io.Adrestus.Trie;

public enum PatriciaTreeTransactionType {
    REGULAR("REGULAR"),
    STAKING("STAKING"),
    REWARDS("REWARDS"),
    UNCLAIMED_FEE_REWARD("UNCLAIMED_FEE_REWARD");
    //UNSTAKING("UNSTAKING"),
    //DELEGATING("DELEGATING"),
    //UNDELEGATING("UNDELEGATING");
    private final String title;

    PatriciaTreeTransactionType(String title) {
        this.title = title;
    }
}
