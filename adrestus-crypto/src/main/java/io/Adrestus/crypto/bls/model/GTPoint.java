package io.Adrestus.crypto.bls.model;

import io.Adrestus.crypto.bls.BLS381.*;
import io.Adrestus.crypto.bls.constants.Constants;
import io.Adrestus.crypto.bls.utils.BigNumberUtils;


public class GTPoint {

    public FP12 value;

    public GTPoint(FP12 value) {
        this.value = value;
    }

    public static GTPoint one() {
        FP4 zero = new FP4(0);
        FP4 one = new FP4(1);
        FP12 value = new FP12(one, zero, zero);
        return new GTPoint(value);
    }

    public GTPoint mul(GTPoint other) {
        FP12 newPoint = new FP12(other.value);
        newPoint.mul(value);
        return new GTPoint(newPoint);
    }

    public static GTPoint atePairing(G1Point g1, G2Point g2) {
        if (g1.getValue().is_infinity() || g2.getValue().is_infinity()) {
            return GTPoint.one();
        }
        FP12 fp12 = PAIR.ate(new ECP2(g2.getValue()), new ECP(g1.getValue()));
        return new GTPoint(PAIR.fexp(fp12));
    }

    public static GTPoint ate2Pairing(G1Point g1, G2Point g2, G1Point h1, G2Point h2) {
        if (g1.getValue().is_infinity() || g2.getValue().is_infinity()) {
            return atePairing(h1, h2);
        }
        if (h1.getValue().is_infinity() || h2.getValue().is_infinity()) {
            return atePairing(g1, g2);
        }

        FP12 fp12 = PAIR.ate2(new ECP2(g2.getValue()), new ECP(g1.getValue()), new ECP2(h2.getValue()), new ECP(h1.getValue()));
        return new GTPoint(PAIR.fexp(fp12));
    }

    public static GTPoint ateMultiPairing(G1Point[] g1Arr, G2Point[] g2Arr) {
        FP12[] accum = new FP12[Constants.ATE_BITS];
        for (int i = 0; i < accum.length; i++) {
            accum[i] = new FP12(1);
        }

        for (int i = 0; i < g1Arr.length; i++) {
            G1Point g1 = g1Arr[i];
            G2Point g2 = g2Arr[i];
            if (g1.getValue().is_infinity() || g2.getValue().is_infinity()) {
                continue;
            }
            BigNumberUtils.another(accum, new ECP2(g2.getValue()), new ECP(g1.getValue()));
        }
        FP12 e = BigNumberUtils.miller(accum);
        return new GTPoint(e);
    }

    public static GTPoint pair(G1Point p, G2Point q) {
        FP12 e = PAIR.ate(q.getValue(), p.getValue());
        return new GTPoint(PAIR.fexp(e));
    }

    public static GTPoint pairNoExp(G1Point p, G2Point q) {
        FP12 e = PAIR.ate(q.getValue(), p.getValue());
        return new GTPoint(e);
    }

    public static GTPoint pair2(G1Point p, G2Point q, G1Point r, G2Point s) {
        FP12 e = PAIR.ate2(q.getValue(), p.getValue(), s.getValue(), r.getValue());
        return new GTPoint(PAIR.fexp(e));
    }

    public static GTPoint pair2NoExp(G1Point p, G2Point q, G1Point r, G2Point s) {
        FP12 e = PAIR.ate2(q.getValue(), p.getValue(), s.getValue(), r.getValue());
        return new GTPoint(e);
    }

    public static GTPoint fexp(GTPoint point) {
        return new GTPoint(PAIR.fexp(point.value));
    }

    public boolean isunity() {
        return value.isunity();
    }
}
