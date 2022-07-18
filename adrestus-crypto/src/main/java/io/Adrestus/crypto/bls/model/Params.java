package io.Adrestus.crypto.bls.model;

import io.Adrestus.crypto.HashUtil;

public class Params {

    public G1Point g;

    public Params(G1Point g) {
        this.g = g;
    }

    public Params(byte[] label) {
        byte[] b = HashUtil.Shake256(label);
        this.g = new G1Point(b);
    }
}
