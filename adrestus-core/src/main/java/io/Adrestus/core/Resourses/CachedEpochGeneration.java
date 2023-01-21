package io.Adrestus.core.Resourses;

import com.google.common.base.Objects;

public class CachedEpochGeneration {
    private static volatile CachedEpochGeneration instance;
    private int epoch_counter;

    private CachedEpochGeneration() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
    }

    public int getEpoch_counter() {
        return epoch_counter;
    }

    public void setEpoch_counter(int epoch_counter) {
        this.epoch_counter = epoch_counter;
    }

    public static CachedEpochGeneration getInstance() {
        var result = instance;
        if (result == null) {
            synchronized (CachedEpochGeneration.class) {
                result = instance;
                if (result == null) {
                    instance = result = new CachedEpochGeneration();
                }
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CachedEpochGeneration that = (CachedEpochGeneration) o;
        return epoch_counter == that.epoch_counter;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(epoch_counter);
    }

    @Override
    public String toString() {
        return "CachedEpochGeneration{" +
                "epoch_counter=" + epoch_counter +
                '}';
    }
}
