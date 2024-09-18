package io.Adrestus.core.RewardMechanism;

import java.util.Objects;

public class DelegateObject {
    private double weights;
    private double reward;

    public DelegateObject(double weights, double reward) {
        this.weights = weights;
        this.reward = reward;
    }

    public double getWeights() {
        return weights;
    }

    public void setWeights(double weights) {
        this.weights = weights;
    }

    public double getReward() {
        return reward;
    }

    public void setReward(double reward) {
        this.reward = reward;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DelegateObject that = (DelegateObject) o;
        return Double.compare(weights, that.weights) == 0 && Double.compare(reward, that.reward) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(weights, reward);
    }

    @Override
    public String toString() {
        return "WeightsObject{" +
                "weights=" + weights +
                ", reward=" + reward +
                '}';
    }
}
