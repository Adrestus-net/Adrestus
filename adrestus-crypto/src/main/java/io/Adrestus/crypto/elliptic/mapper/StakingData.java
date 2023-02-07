package io.Adrestus.crypto.elliptic.mapper;

import com.google.common.base.Objects;
import io.activej.serializer.annotations.Serialize;

import java.io.Serializable;

public class StakingData implements Serializable {
    private int uid;
    private double stake;

    public StakingData(int uid, double stake) {
        this.uid = uid;
        this.stake = stake;
    }

    public StakingData() {
        this.uid = 0;
        this.stake = 0.0;
    }

    @Serialize
    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    @Serialize
    public double getStake() {
        return stake;
    }

    public void setStake(double stake) {
        this.stake = stake;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StakingData that = (StakingData) o;
        return uid == that.uid && Double.compare(that.stake, stake) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uid, stake);
    }

    @Override
    public String toString() {
        return "StakingData{" +
                "uid=" + uid +
                ", stake=" + stake +
                '}';
    }
}
