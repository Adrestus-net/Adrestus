package io.Adrestus.crypto.elliptic;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.DSA;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.crypto.signers.DSAKCalculator;
import org.bouncycastle.crypto.signers.RandomDSAKCalculator;
import org.bouncycastle.math.ec.*;

import java.math.BigInteger;
import java.security.SecureRandom;


public class ECDSASigner implements ECConstants, DSA {
    private final DSAKCalculator kCalculator;

    private ECKeyParameters key;
    private SecureRandom random;


    public ECDSASigner() {
        this.kCalculator = new RandomDSAKCalculator();
    }


    public ECDSASigner(DSAKCalculator kCalculator) {
        this.kCalculator = kCalculator;
    }

    @Override
    public void init(boolean forSigning, CipherParameters param) {
        SecureRandom providedRandom = null;

        if (forSigning) {
            if (param instanceof ParametersWithRandom) {
                ParametersWithRandom rParam = (ParametersWithRandom) param;

                this.key = (ECPrivateKeyParameters) rParam.getParameters();
                providedRandom = rParam.getRandom();
            } else {
                this.key = (ECPrivateKeyParameters) param;
            }
        } else {
            this.key = (ECPublicKeyParameters) param;
        }

        this.random =
                initSecureRandom(forSigning && !kCalculator.isDeterministic(), providedRandom);
    }

    // 5.3 pg 28

    @Override
    public BigInteger[] generateSignature(byte[] message) {
        ECDomainParameters ec = key.getParameters();
        BigInteger n = ec.getN();
        BigInteger e = calculateE(n, message);
        BigInteger d = ((ECPrivateKeyParameters) key).getD();

        if (kCalculator.isDeterministic()) {
            kCalculator.init(n, d, message);
        } else {
            kCalculator.init(n, random);
        }

        BigInteger r, s;

        ECMultiplier basePointMultiplier = createBasePointMultiplier();

        // 5.3.2
        do // generate s
        {
            BigInteger k;
            do // generate r
            {
                k = kCalculator.nextK();

                ECPoint p = basePointMultiplier.multiply(ec.getG(), k).normalize();

                // 5.3.3
                r = p.getAffineXCoord().toBigInteger().mod(n);
            } while (r.equals(ZERO));

            s = k.modInverse(n).multiply(e.add(d.multiply(r))).mod(n);
        } while (s.equals(ZERO));

        return new BigInteger[]{r, s};
    }


    public Object[] generateSignature2(byte[] message) {
        ECDomainParameters ec = key.getParameters();
        BigInteger n = ec.getN();
        BigInteger e = calculateE(n, message);
        BigInteger d = ((ECPrivateKeyParameters) key).getD();

        if (kCalculator.isDeterministic()) {
            kCalculator.init(n, d, message);
        } else {
            kCalculator.init(n, random);
        }

        BigInteger r, s;

        /** */
        ECPoint p;

        ECMultiplier basePointMultiplier = createBasePointMultiplier();

        // 5.3.2
        do // generate s
        {
            BigInteger k;
            do // generate r
            {
                k = kCalculator.nextK();

                p = basePointMultiplier.multiply(ec.getG(), k).normalize();

                // 5.3.3
                r = p.getAffineXCoord().toBigInteger().mod(n);
            } while (r.equals(ZERO));

            s = k.modInverse(n).multiply(e.add(d.multiply(r))).mod(n);
        } while (s.equals(ZERO));

        return new Object[]{r, s, p};
    }

    // 5.4 pg 29

    @Override
    public boolean verifySignature(byte[] message, BigInteger r, BigInteger s) {
        ECDomainParameters ec = key.getParameters();
        BigInteger n = ec.getN();
        BigInteger e = calculateE(n, message);

        // r in the range [1,n-1]
        if (r.compareTo(ONE) < 0 || r.compareTo(n) >= 0) {
            return false;
        }

        // s in the range [1,n-1]
        if (s.compareTo(ONE) < 0 || s.compareTo(n) >= 0) {
            return false;
        }

        BigInteger c = s.modInverse(n);

        BigInteger u1 = e.multiply(c).mod(n);
        BigInteger u2 = r.multiply(c).mod(n);

        ECPoint G = ec.getG();
        ECPoint Q = ((ECPublicKeyParameters) key).getQ();

        ECPoint point = ECAlgorithms.sumOfTwoMultiplies(G, u1, Q, u2);

        // components must be bogus.
        if (point.isInfinity()) {
            return false;
        }

        /*
         * If possible, avoid normalizing the point (to save a modular inversion in the curve field).
         *
         * There are ~cofactor elements of the curve field that reduce (modulo the group order) to 'r'.
         * If the cofactor is known and small, we generate those possible field values and project each
         * of them to the same "denominator" (depending on the particular projective coordinates in use)
         * as the calculated point.X. If any of the projected values matches point.X, then we have:
         *     (point.X / Denominator mod p) mod n == r
         * as required, and verification succeeds.
         *
         * Based on an original idea by Gregory Maxwell (https://github.com/gmaxwell), as implemented in
         * the libsecp256k1 project (https://github.com/bitcoin/secp256k1).
         */
        ECCurve curve = point.getCurve();
        if (curve != null) {
            BigInteger cofactor = curve.getCofactor();
            if (cofactor != null && cofactor.compareTo(EIGHT) <= 0) {
                ECFieldElement D = getDenominator(curve.getCoordinateSystem(), point);
                if (D != null && !D.isZero()) {
                    ECFieldElement X = point.getXCoord();
                    while (curve.isValidFieldElement(r)) {
                        ECFieldElement R = curve.fromBigInteger(r).multiply(D);
                        if (R.equals(X)) {
                            return true;
                        }
                        r = r.add(n);
                    }
                    return false;
                }
            }
        }

        BigInteger v = point.normalize().getAffineXCoord().toBigInteger().mod(n);
        return v.equals(r);
    }

    protected BigInteger calculateE(BigInteger n, byte[] message) {
        int log2n = n.bitLength();
        int messageBitLength = message.length * 8;

        BigInteger e = new BigInteger(1, message);
        if (log2n < messageBitLength) {
            e = e.shiftRight(messageBitLength - log2n);
        }
        return e;
    }

    protected ECMultiplier createBasePointMultiplier() {
        return new FixedPointCombMultiplier();
    }

    protected ECFieldElement getDenominator(int coordinateSystem, ECPoint p) {
        switch (coordinateSystem) {
            case ECCurve.COORD_HOMOGENEOUS:
            case ECCurve.COORD_LAMBDA_PROJECTIVE:
            case ECCurve.COORD_SKEWED:
                return p.getZCoord(0);
            case ECCurve.COORD_JACOBIAN:
            case ECCurve.COORD_JACOBIAN_CHUDNOVSKY:
            case ECCurve.COORD_JACOBIAN_MODIFIED:
                return p.getZCoord(0).square();
            default:
                return null;
        }
    }

    protected SecureRandom initSecureRandom(boolean needed, SecureRandom provided) {
        return !needed
                ? null
                : (provided != null) ? provided : CryptoServicesRegistrar.getSecureRandom();
    }
}
