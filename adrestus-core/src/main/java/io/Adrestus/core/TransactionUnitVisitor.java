package io.Adrestus.core;

public interface TransactionUnitVisitor {

    void visit(RegularTransaction regularTransaction);

    void visit(RewardsTransaction rewardsTransaction);

    void visit(StakingTransaction stakingTransaction);

    void visit(DelegateTransaction delegateTransaction);

    void visit(UnclaimedFeeRewardTransaction unclaimedFeeRewardTransaction);
}
