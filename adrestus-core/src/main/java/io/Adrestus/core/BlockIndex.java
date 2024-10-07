package io.Adrestus.core;

import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.crypto.bls.model.BLSPublicKey;

import java.util.*;
import java.util.stream.Collectors;

public class BlockIndex implements IBlockIndex {

    @Override
    public int getPublicKeyIndex(int zone, BLSPublicKey pub_key) {
        int pos = new ArrayList<BLSPublicKey>(
                CachedLatestBlocks
                        .getInstance()
                        .getCommitteeBlock()
                        .getStructureMap()
                        .get(zone).keySet()).indexOf(pub_key);
        return pos;
    }

    @Override
    public BLSPublicKey getPublicKeyByIndex(int zone, int index) {
        return new ArrayList<BLSPublicKey>(CachedLatestBlocks
                .getInstance()
                .getCommitteeBlock()
                .getStructureMap()
                .get(zone).keySet()).get(index);
    }

    @Override
    public String getAddressByPublicKey(BLSPublicKey blsPublicKey) {
        return CachedLatestBlocks
                .getInstance()
                .getCommitteeBlock()
                .getStakingMap()
                .values()
                .stream()
                .filter(val -> val.getAddressData().getValidatorBlSPublicKey().equals(blsPublicKey))
                .findFirst().get().getAddressData().getAddress();
    }

    @Override
    public int getIndexFromIP(int zone, String ip) {
        int count = 0;
        for (Map.Entry<BLSPublicKey, String> entry : CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(zone).entrySet()) {
            if (entry.getValue().equals(ip)) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(zone).append(count);
                return Integer.valueOf(stringBuilder.toString());
            }
            count++;
        }
        return count;
    }

    @Override
    public String getIpValue(int zone, BLSPublicKey blsPublicKey) {
        return CachedLatestBlocks
                .getInstance()
                .getCommitteeBlock()
                .getStructureMap()
                .get(zone)
                .get(blsPublicKey);
    }

    @Override
    public Integer getZone(BLSPublicKey blsPublicKey) {
        for (Map.Entry<Integer, LinkedHashMap<BLSPublicKey, String>> entry : CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().entrySet()) {
            Optional<BLSPublicKey> find = entry.getValue().keySet().stream().filter(val -> val.equals(blsPublicKey)).findFirst();
            if (!find.isEmpty()) {
                return entry.getKey();
            }
        }
        return 0;
    }

    @Override
    public Integer getZone(String IP) {
        for (Map.Entry<Integer, LinkedHashMap<BLSPublicKey, String>> entry : CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().entrySet()) {
            Optional<String> find = entry.getValue().values().stream().filter(val -> val.equals(IP)).findFirst();
            if (!find.isEmpty()) {
                return entry.getKey();
            }
        }
        return 0;
    }

    @Override
    public boolean containsAll(Set<BLSPublicKey> left, Set<BLSPublicKey> right) {
        return new HashSet<>(left).containsAll(right);
    }

}
