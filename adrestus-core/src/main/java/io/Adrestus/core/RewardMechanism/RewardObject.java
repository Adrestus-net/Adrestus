package io.Adrestus.core.RewardMechanism;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RewardObject {

    private double effective_stake;
    private double effective_stake_ratio;
    private int block_participation;
    private int transactions_leader_participation;
    private boolean committee_leader_participation;
    private double unreal_reward;
    private double real_reward;
    private Map<String, DelegateObject> delegate_stake;

    public RewardObject(double effective_stake, double effective_stake_ratio) {
        this.effective_stake = effective_stake;
        this.effective_stake_ratio = effective_stake_ratio;
        this.delegate_stake = new HashMap<String, DelegateObject>();
        this.block_participation = 0;
        this.transactions_leader_participation = 0;
        this.committee_leader_participation = false;
    }

    public RewardObject(double effective_stake, Map<String, DelegateObject> delegate_stake, double real_reward, double unreal_reward, int block_participation, double effective_stake_ratio) {
        this.effective_stake = effective_stake;
        this.delegate_stake = delegate_stake;
        this.real_reward = real_reward;
        this.unreal_reward = unreal_reward;
        this.block_participation = block_participation;
        this.effective_stake_ratio = effective_stake_ratio;
        this.transactions_leader_participation = 0;
        this.committee_leader_participation = false;
    }

    public double getEffective_stake() {
        return effective_stake;
    }

    public void setEffective_stake(double effective_stake) {
        this.effective_stake = effective_stake;
    }

    public double getEffective_stake_ratio() {
        return effective_stake_ratio;
    }

    public void setEffective_stake_ratio(double effective_stake_ratio) {
        this.effective_stake_ratio = effective_stake_ratio;
    }

    public Map<String, DelegateObject> getDelegate_stake() {
        return delegate_stake;
    }

    public void setDelegate_stake(Map<String, DelegateObject> delegate_stake) {
        this.delegate_stake = delegate_stake;
    }


    public double getReal_reward() {
        return real_reward;
    }

    public void setReal_reward(double real_reward) {
        this.real_reward = real_reward;
    }

    public double getUnreal_reward() {
        return unreal_reward;
    }

    public void setUnreal_reward(double unreal_reward) {
        this.unreal_reward = unreal_reward;
    }

    public int getBlock_participation() {
        return block_participation;
    }

    public void setBlock_participation(int block_participation) {
        this.block_participation = block_participation;
    }

    public int getTransactions_leader_participation() {
        return transactions_leader_participation;
    }

    public void setTransactions_leader_participation(int transactions_leader_participation) {
        this.transactions_leader_participation = transactions_leader_participation;
    }

    public boolean isCommittee_leader_participation() {
        return committee_leader_participation;
    }

    public void setCommittee_leader_participation(boolean committee_leader_participation) {
        this.committee_leader_participation = committee_leader_participation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RewardObject that = (RewardObject) o;
        return Double.compare(effective_stake, that.effective_stake) == 0 && Double.compare(effective_stake_ratio, that.effective_stake_ratio) == 0 && block_participation == that.block_participation && transactions_leader_participation == that.transactions_leader_participation && committee_leader_participation == that.committee_leader_participation && Double.compare(unreal_reward, that.unreal_reward) == 0 && Double.compare(real_reward, that.real_reward) == 0 && Objects.equals(delegate_stake, that.delegate_stake);
    }

    @Override
    public int hashCode() {
        return Objects.hash(effective_stake, effective_stake_ratio, block_participation, transactions_leader_participation, committee_leader_participation, unreal_reward, real_reward, delegate_stake);
    }

    @Override
    public String toString() {
        return "StakeObject{" +
                "effective_stake=" + effective_stake +
                ", effective_stake_ratio=" + effective_stake_ratio +
                ", block_participation=" + block_participation +
                ", transactions_leader_participation=" + transactions_leader_participation +
                ", committee_leader_participation=" + committee_leader_participation +
                ", unreal_reward=" + unreal_reward +
                ", real_reward=" + real_reward +
                ", delegate_stake=" + delegate_stake +
                '}';
    }
}