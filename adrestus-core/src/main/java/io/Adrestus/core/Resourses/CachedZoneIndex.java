package io.Adrestus.core.Resourses;

import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.network.IPFinder;

import java.util.LinkedHashMap;
import java.util.Map;

public class CachedZoneIndex {
    private static volatile CachedZoneIndex instance;
    private int ZONE_INDEX;


    private CachedZoneIndex() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        this.ZONE_INDEX = 0;
    }

    public int getZoneIndex() {
        return ZONE_INDEX;
    }

    public void setZONE_INDEX(int ZONE_INDEX) {
        this.ZONE_INDEX = ZONE_INDEX;
    }

    public void setZoneIndexInternalIP() {
        Map<Integer, LinkedHashMap<BLSPublicKey, String>> outer_map = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap();
        outerloop:
        for (Map.Entry<Integer, LinkedHashMap<BLSPublicKey, String>> outer : outer_map.entrySet()) {
            for (Map.Entry<BLSPublicKey, String> inner : outer.getValue().entrySet()) {
                if (inner.getValue().equals(IPFinder.getLocal_address())) {
                    ZONE_INDEX = outer.getKey();
                    break outerloop;
                }
            }
        }
    }

    public void setZoneIndexExternalIP() {
        Map<Integer, LinkedHashMap<BLSPublicKey, String>> outer_map = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap();
        outerloop:
        for (Map.Entry<Integer, LinkedHashMap<BLSPublicKey, String>> outer : outer_map.entrySet()) {
            for (Map.Entry<BLSPublicKey, String> inner : outer.getValue().entrySet()) {
                if (inner.getValue().equals(IPFinder.getExternal_address())) {
                    ZONE_INDEX = outer.getKey();
                    break outerloop;
                }
            }
        }
    }

    public static int[] getAvailableZones() {

        if (CachedZoneIndex.getInstance().getZoneIndex() == 0)
            return new int[]{0, 1, 2};


        int arr[] = new int[3];
        for (int i = 0; i < arr.length; i++) {
            if (i != CachedZoneIndex.getInstance().getZoneIndex())
                arr[i] = i;
        }
        return arr;
    }

    public static CachedZoneIndex getInstance() {
        var result = instance;
        if (result == null) {
            synchronized (CachedZoneIndex.class) {
                result = instance;
                if (result == null) {
                    instance = result = new CachedZoneIndex();
                }
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "CachedZoneIndex{}";
    }

}
