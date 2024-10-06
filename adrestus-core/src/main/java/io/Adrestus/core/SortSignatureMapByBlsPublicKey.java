package io.Adrestus.core;

import io.Adrestus.crypto.bls.model.BLSPublicKey;

import java.io.Serializable;
import java.util.Comparator;

public final class SortSignatureMapByBlsPublicKey implements Comparator<BLSPublicKey>, Serializable {
    public int compare(BLSPublicKey o1, BLSPublicKey o2) {
        return o1.toRaw().compareTo(o2.toRaw());
    }
}
