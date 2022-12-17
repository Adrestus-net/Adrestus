package io.Adrestus.core;

import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.crypto.bls.model.BLSPublicKey;

import java.util.ArrayList;

public class BlockIndex implements IBlockIndex {
    public int getPublicKeyIndex(int zone, BLSPublicKey pub_key) {
        int pos = new ArrayList<BLSPublicKey>(
                CachedLatestBlocks
                        .getInstance()
                        .getCommitteeBlock()
                        .getStructureMap()
                        .get(zone).keySet()).indexOf(pub_key);
        return pos;
    }

    public BLSPublicKey getPublicKeyByIndex(int zone, int index) {
        return new ArrayList<BLSPublicKey>(CachedLatestBlocks
                .getInstance()
                .getCommitteeBlock()
                .getStructureMap()
                .get(zone).keySet()).get(index);
    }

    public String getIpValue(int zone, BLSPublicKey blsPublicKey) {
        return CachedLatestBlocks
                .getInstance()
                .getCommitteeBlock()
                .getStructureMap()
                .get(zone)
                .get(blsPublicKey);
    }
}
