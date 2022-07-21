package io.Adrestus.crypto.elliptic;

import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;

public class ECDSASignature {
    private BigInteger r;
    private BigInteger s;

    public ECPoint p;

    public ECDSASignature() {
    }

    public ECDSASignature(BigInteger r, BigInteger s, ECPoint p) {
        this.r = r;
        this.s = s;
        this.p = p;
    }

    public ECDSASignature(BigInteger r, BigInteger s) {
        this(r, s, null);
    }

    public boolean isCanonical() {
        return s.compareTo(Sign.HALF_CURVE_ORDER) <= 0;
    }

    public ECDSASignature toCanonicalised() {
        if (!isCanonical()) {
            return new ECDSASignature(r, Sign.CURVE.getN().subtract(s), p);
        } else {
            return this;
        }
    }

    public BigInteger getR() {
        return r;
    }

    public void setR(BigInteger r) {
        this.r = r;
    }

    public BigInteger getS() {
        return s;
    }

    public void setS(BigInteger s) {
        this.s = s;
    }

    public ECPoint getP() {
        return p;
    }

    public void setP(ECPoint p) {
        this.p = p;
    }
}
