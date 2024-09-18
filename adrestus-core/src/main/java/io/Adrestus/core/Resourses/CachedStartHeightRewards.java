package io.Adrestus.core.Resourses;

import java.util.Objects;


public class CachedStartHeightRewards {
    private static volatile CachedStartHeightRewards instance;
    // this variable find the start height block to start calculate rewards from latest checkpoint
    private int height;
    // this variable find if the committee rewards has been already calculated
    private boolean rewardsCommitteeEnabled;

    private CachedStartHeightRewards() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
    }


    public static CachedStartHeightRewards getInstance() {
        var result = instance;
        if (result == null) {
            synchronized (CachedStartHeightRewards.class) {
                result = instance;
                if (result == null) {
                    instance = result = new CachedStartHeightRewards();
                }
            }
        }
        return result;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isRewardsCommitteeEnabled() {
        return rewardsCommitteeEnabled;
    }

    public void setRewardsCommitteeEnabled(boolean rewardsCommitteeEnabled) {
        this.rewardsCommitteeEnabled = rewardsCommitteeEnabled;
    }

    public static void setInstance(CachedStartHeightRewards instance) {
        CachedStartHeightRewards.instance = instance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CachedStartHeightRewards that = (CachedStartHeightRewards) o;
        return height == that.height;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(height);
    }
}
