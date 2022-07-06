package io.Adrestus.crypto.bls.model;

import io.Adrestus.crypto.HashUtil;

public class Params {

    public G1 g;
    
    public Params(G1 g) {
        this.g = g;
    }
    
    public Params(byte[] label) {
        this.g = new G1(HashUtil.Shake256(label));
    }
}
