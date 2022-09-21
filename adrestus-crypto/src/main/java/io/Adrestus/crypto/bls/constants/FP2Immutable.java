package io.Adrestus.crypto.bls.constants;


import io.Adrestus.crypto.bls.BLS381.BIG;
import io.Adrestus.crypto.bls.BLS381.FP;
import io.Adrestus.crypto.bls.BLS381.FP2;

import java.util.Objects;

import static io.Adrestus.crypto.bls.constants.CurveUtil.bigFromHex;
import static io.Adrestus.crypto.bls.constants.CurveUtil.fpFromHex;


public class FP2Immutable {

    public static final FP2Immutable ZERO = new FP2Immutable(0);
    public static final FP2Immutable ONE = new FP2Immutable(1);

    // The threshold for ordering elements is (P - 1) // 2
    public static final BIG THRESHOLD =
            bigFromHex(
                    "0x0d0088f51cbff34d258dd3db21a5d66bb23ba5c279c2895fb39869507b587b120f55ffff58a9ffffdcff7fffffffd555");

    private final FP2 fp2;


    public FP2Immutable(FP2Immutable fp2Immutable) {
        fp2 = fp2Immutable.getFp2();
    }

    public FP2Immutable(FP2 fp2) {
        this.fp2 = new FP2(fp2);
    }


    public FP2Immutable(FP fpA, FP fpB) {
        fp2 = new FP2(fpA, fpB);
    }


    public FP2Immutable(BIG big1, BIG big2) {
        fp2 = new FP2(big1, big2);
    }


    public FP2Immutable(int c) {
        fp2 = new FP2(c);
    }


    public FP2Immutable(String hex1, String hex2) {
        fp2 = new FP2(fpFromHex(hex1), fpFromHex(hex2));
    }


    public FP2Immutable sqr() {
        FP2 result = new FP2(fp2);
        result.sqr();
        return new FP2Immutable(result);
    }


    public FP2Immutable mul(FP2Immutable a) {
        FP2 result = new FP2(fp2);
        result.mul(a.fp2);
        return new FP2Immutable(result);
    }


    public FP2Immutable mul(int c) {
        FP2 result = new FP2(fp2);
        result.imul(c);
        result.norm();
        return new FP2Immutable(result);
    }


    public FP2Immutable mul(FP c) {
        FP2 result = new FP2(fp2);
        result.pmul(c);
        result.norm();
        return new FP2Immutable(result);
    }


    public FP2Immutable add(FP2Immutable a) {
        FP2 result = new FP2(fp2);
        result.add(a.fp2);
        result.norm();
        return new FP2Immutable(result);
    }


    public FP2Immutable sub(FP2Immutable a) {
        FP2 result = new FP2(fp2);
        result.sub(a.fp2);
        result.norm();
        return new FP2Immutable(result);
    }


    public FP2Immutable neg() {
        FP2 result = new FP2(fp2);
        result.neg();
        return new FP2Immutable(result);
    }


    public FP2Immutable reduce() {
        FP2 result = new FP2(fp2);
        result.reduce();
        return new FP2Immutable(result);
    }


    public FP2Immutable inverse() {
        FP2 result = new FP2(fp2);
        result.inverse();
        return new FP2Immutable(result);
    }


    public FP2Immutable dbl() {
        return this.add(this);
    }

    public boolean iszilch() {
        return fp2.iszilch();
    }


    public int sgn0() {
        final int sign0 = fp2.getA().parity();
        final int zero0 = fp2.getA().iszilch() ? 1 : 0;
        final int sign1 = fp2.getB().parity();
        return sign0 | (zero0 & sign1);
    }


    public FP2Immutable sqrs(int n) {
        FP2 result = new FP2(fp2);
        while (n-- > 0) {
            result.sqr();
        }
        result.norm();
        return new FP2Immutable(result);
    }


    public FP2Immutable pow(int exponent) {
        if (exponent == 0) return ONE;
        if (exponent == 1) return this;
        if (exponent == 2) return this.sqr();
        FP2 res = new FP2(1);
        FP2 tmp = new FP2(fp2);
        while (exponent > 0) {
            if ((exponent & 1) == 1) {
                res.mul(tmp);
            }
            tmp.sqr();
            exponent >>>= 1;
        }
        return new FP2Immutable(res);
    }

    public FP2 getFp2() {
        // Return a copy to preserve immutability
        return new FP2(fp2);
    }

    @Override
    public String toString() {
        return fp2.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (Objects.isNull(obj)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FP2Immutable)) {
            return false;
        }
        FP2Immutable other = (FP2Immutable) obj;
        return this.fp2.equals(other.fp2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.reduce().toString());
    }
}
