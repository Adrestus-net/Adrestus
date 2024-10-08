package io.Adrestus.crypto.elliptic.mapper;

import io.activej.serializer.annotations.Serialize;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class StakingData implements Serializable {
    private int uid;
    private BigDecimal stake;

    public StakingData(int uid, BigDecimal stake) {
        this.uid = uid;
        this.stake = stake;
    }

    public StakingData() {
        this.uid = 0;
        this.stake = BigDecimal.ZERO;
    }

    @Serialize
    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    @Serialize
    public BigDecimal getStake() {
        return stake;
    }

    public void setStake(BigDecimal stake) {
        this.stake = stake;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StakingData that = (StakingData) o;
        return uid == that.uid && Objects.equals(stake, that.stake);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, stake);
    }

    @Override
    public String toString() {
        return "StakingData{" +
                "uid=" + uid +
                ", stake=" + stake +
                '}';
    }
}
