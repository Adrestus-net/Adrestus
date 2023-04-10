package io.Adrestus.core;

import io.Adrestus.crypto.bls.model.BLSPublicKey;

public interface IBlockIndex {
    int getPublicKeyIndex(int zone, BLSPublicKey pub_key);

    int getIndexFromIP(int zone, String ip);

    BLSPublicKey getPublicKeyByIndex(int zone, int index);

    String getIpValue(int zone, BLSPublicKey blsPublicKey);

    Integer getZone(BLSPublicKey blsPublicKey);

    Integer getZone(String IP);

}
