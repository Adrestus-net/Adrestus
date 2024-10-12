package io.Adrestus.core.comparators;

import io.Adrestus.crypto.bls.model.BLSPublicKey;

import java.io.Serializable;
import java.util.Comparator;

public class SortSignatureMapByBlsPublicKey implements Comparator<BLSPublicKey>, Serializable {
    public int compare(BLSPublicKey o1, BLSPublicKey o2) {
        return o1.toRaw().compareTo(o2.toRaw());
    }
}

