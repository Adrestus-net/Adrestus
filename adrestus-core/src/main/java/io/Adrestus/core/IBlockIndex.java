package io.Adrestus.core;

import io.Adrestus.crypto.bls.model.BLSPublicKey;

import java.util.ArrayList;
import java.util.Set;

public interface IBlockIndex {
    int getPublicKeyIndex(int zone, BLSPublicKey pub_key);

    int getIndexFromIP(int zone, String ip);

    BLSPublicKey getPublicKeyByIndex(int zone, int index);

    String getAddressByPublicKey(BLSPublicKey blsPublicKey);

    String getIpValue(int zone, BLSPublicKey blsPublicKey);

    Integer getZone(BLSPublicKey blsPublicKey);

    Integer getZone(String IP);

    boolean containsAll(Set<BLSPublicKey> left, Set<BLSPublicKey> right);

    ArrayList<String> getIpList(int zoneIndex);
}
