package io.Adrestus.core.RewardMechanism;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class DelegateObject implements Serializable {
    private BigDecimal weights;
    private BigDecimal reward;

    public DelegateObject(BigDecimal weights, BigDecimal reward) {
        this.weights = weights;
        this.reward = reward;
    }

    public BigDecimal getWeights() {
        return weights;
    }

    public void setWeights(BigDecimal weights) {
        this.weights = weights;
    }

    public BigDecimal getReward() {
        return reward;
    }

    public void setReward(BigDecimal reward) {
        this.reward = reward;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DelegateObject that = (DelegateObject) o;
        return Objects.equals(weights, that.weights) && Objects.equals(reward, that.reward);
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
