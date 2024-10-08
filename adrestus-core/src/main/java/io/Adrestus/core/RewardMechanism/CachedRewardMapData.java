package io.Adrestus.core.RewardMechanism;

import java.io.Serializable;
import java.util.HashMap;

public class CachedRewardMapData implements Serializable {

    private static volatile CachedRewardMapData instance;
    private final HashMap<String, RewardObject> effective_stakes_map;

    private CachedRewardMapData() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        this.effective_stakes_map = new HashMap<>();
    }

    public static CachedRewardMapData getInstance() {
        var result = instance;
        if (result == null) {
            synchronized (CachedRewardMapData.class) {
                result = instance;
                if (result == null) {
                    result = new CachedRewardMapData();
                    instance = result;
                }
            }
        }
        return result;
    }

    public static void setInstance(CachedRewardMapData instance) {
        CachedRewardMapData.instance = instance;
    }

    public HashMap<String, RewardObject> getEffective_stakes_map() {
        return effective_stakes_map;
    }

    public void clearInstance() {
        synchronized (CachedRewardMapData.class) {
            effective_stakes_map.values().forEach(val->val.getDelegate_stake().clear());
            effective_stakes_map.clear();
            instance = null;
        }
    }
}
