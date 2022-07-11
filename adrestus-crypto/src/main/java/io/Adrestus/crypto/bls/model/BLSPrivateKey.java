package io.Adrestus.crypto.bls.model;

import java.security.SecureRandom;

public class BLSPrivateKey {

    private FieldElement x;

    public BLSPrivateKey(FieldElement x) {
        this.x = x;
    }

    public BLSPrivateKey(int entropy) {
        this.x = new FieldElement(entropy);
    }

    public BLSPrivateKey(SecureRandom random) {
        this.x = new FieldElement(random);
    }

    public BLSPrivateKey(byte[] buff) {
        this.x = FieldElement.fromBytes(buff);
    }


    public byte[] toBytes() {
        return this.x.toBytes();
    }

    public BLSPrivateKey fromBytes(byte[] buf) {
        return new BLSPrivateKey(FieldElement.fromBytes(buf));
    }

    public FieldElement getX() {
        return x;
    }

    public void setX(FieldElement x) {
        this.x = x;
    }
}
