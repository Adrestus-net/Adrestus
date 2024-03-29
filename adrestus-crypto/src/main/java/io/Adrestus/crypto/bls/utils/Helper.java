package io.Adrestus.crypto.bls.utils;

import com.google.common.annotations.VisibleForTesting;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.bls.BLS381.FP;
import io.Adrestus.crypto.bls.BLS381.FP2;
import io.Adrestus.crypto.bls.constants.Constants;
import io.Adrestus.crypto.bls.constants.FP2Immutable;
import io.Adrestus.crypto.bls.model.JacobianPoint;
import org.apache.tuweni.bytes.Bytes;

import static com.google.common.base.Preconditions.checkArgument;
import static io.Adrestus.crypto.bls.constants.Chains.expChain;
import static io.Adrestus.crypto.bls.constants.Chains.mxChain;
import static io.Adrestus.crypto.bls.constants.Constants.*;
import static io.Adrestus.crypto.bls.constants.CurveUtil.os2ip_modP;
import static io.Adrestus.crypto.bls.constants.FP2Immutable.ONE;

public class Helper {
    private static final int SHA256_HASH_SIZE = 32;
    private static final int SHA256_BLOCK_SIZE = 64;

    @VisibleForTesting
    public static boolean isOnCurve(JacobianPoint p) {
        if (p.isInfinity()) {
            return true;
        }

        FP2Immutable x = p.getX();
        FP2Immutable y = p.getY();
        FP2Immutable z = p.getZ();

        FP2Immutable y2 = y.sqr();
        FP2Immutable x3 = x.pow(3);
        FP2Immutable z6 = z.pow(6);

        FP2Immutable four = new FP2Immutable(new FP(4), new FP(4));
        return y2.equals(x3.add(z6.mul(four)));
    }


    public static boolean isInG2(JacobianPoint p) {
        return isOnCurve(p) && mapG2ToInfinity(p).isInfinity();
    }


    @VisibleForTesting
    public static JacobianPoint mapG2ToInfinity(JacobianPoint p) {
        JacobianPoint psi3 = psi2(psi(p));
        return mxChain(psi3).add(psi2(p)).neg().add(p);
    }


    @VisibleForTesting
    public static Bytes expandMessage(Bytes message, Bytes dst, int lengthInBytes) {
        checkArgument(dst.size() < 256, "The DST must be 255 bytes or fewer.");
        checkArgument(lengthInBytes > 0, "Number of bytes requested must be greater than zero.");

        final int ell = 1 + (lengthInBytes - 1) / SHA256_HASH_SIZE;
        checkArgument(ell <= 255, "Too many bytes of output were requested.");

        byte[] uniformBytes = new byte[ell * SHA256_HASH_SIZE];

        Bytes dstPrime = Bytes.concatenate(dst, Bytes.of((byte) dst.size()));
        Bytes zPad = Bytes.wrap(new byte[SHA256_BLOCK_SIZE]);
        Bytes libStr = Bytes.ofUnsignedShort(lengthInBytes);
        Bytes b0 = Bytes.wrap(HashUtil.sha256(Bytes.concatenate(zPad, message, libStr, Bytes.of((byte) 0), dstPrime).toArray()));
        Bytes bb = Bytes.wrap(HashUtil.sha256(Bytes.concatenate(b0, Bytes.of((byte) 1), dstPrime).toArray()));
        System.arraycopy(bb.toArrayUnsafe(), 0, uniformBytes, 0, SHA256_HASH_SIZE);
        for (int i = 1; i < ell; i++) {
            bb = Bytes.wrap(HashUtil.sha256(Bytes.concatenate(b0.xor(bb), Bytes.of((byte) (i + 1)), dstPrime).toArray()));
            System.arraycopy(bb.toArrayUnsafe(), 0, uniformBytes, i * SHA256_HASH_SIZE, SHA256_HASH_SIZE);
        }
        return Bytes.wrap(uniformBytes, 0, lengthInBytes);
    }


    public static FP2Immutable[] hashToField(Bytes message, int count, Bytes dst) {

        // See https://tools.ietf.org/html/draft-irtf-cfrg-hash-to-curve-07#section-8.8.1
        final int l = 64;
        // The extension degree of our field, FP2
        final int m = 2;

        final int lenInBytes = count * m * l;
        final Bytes uniformBytes = expandMessage(message, dst, lenInBytes);
        FP2Immutable[] u = new FP2Immutable[count];

        for (int i = 0; i < count; i++) {
            FP e0 = os2ip_modP(uniformBytes.slice(l * i * m, l));
            FP e1 = os2ip_modP(uniformBytes.slice(l * (1 + i * m), l));
            u[i] = new FP2Immutable(e0, e1);
        }

        return u;
    }


    public static JacobianPoint mapToCurve(FP2Immutable t) {

        // First, compute X0(t), detecting and handling exceptional case
        FP2Immutable tPow2 = t.sqr();
        FP2Immutable tPow4 = tPow2.sqr();
        FP2Immutable num_den_common = Constants.xi_2Pow2.mul(tPow4).add(Constants.xi_2.mul(tPow2));
        FP2Immutable x0_num = Constants.Ell2p_b.mul(num_den_common.add(ONE));
        FP2Immutable x0_den = Constants.Ell2p_a.neg().mul(num_den_common);
        if (x0_den.iszilch()) {
            x0_den = Constants.Ell2p_a.mul(Constants.xi_2);
        }

        // Compute num and den of g(X0(t))
        FP2Immutable gx0_den = x0_den.pow(3);
        FP2Immutable gx0_num = Constants.Ell2p_b.mul(gx0_den);
        gx0_num = gx0_num.add(Constants.Ell2p_a.mul(x0_num).mul(x0_den.pow(2)));
        gx0_num = gx0_num.add(x0_num.pow(3));

        // try taking sqrt of g(X0(t))
        // this uses the trick for combining division and sqrt from Section 5 of
        // Bernstein, Duif, Lange, Schwabe, and Yang, "High-speed high-security signatures."
        // J Crypt Eng 2(2):77--89, Sept. 2012. http://ed25519.cr.yp.to/ed25519-20110926.pdf
        FP2Immutable tmp1, tmp2;
        tmp1 = gx0_den.pow(7); // v^7
        tmp2 = gx0_num.mul(tmp1); // u v^7
        tmp1 = tmp1.mul(tmp2).mul(gx0_den); // u v^15
        FP2Immutable sqrt_candidate = tmp2.mul(expChain(tmp1));

        // check if g(X0(t)) is square and return the sqrt if so
        for (FP2Immutable fp2Immutable : Constants.ROOTS_OF_UNITY) {
            FP2Immutable y0 = sqrt_candidate.mul(fp2Immutable);
            if (y0.sqr().mul(gx0_den).equals(gx0_num)) {
                // found sqrt(g(X0(t))). force sign of y to equal sign of t
                if (t.sgn0() != y0.sgn0()) {
                    y0 = y0.neg();
                }
                return new JacobianPoint(x0_num.mul(x0_den), y0.mul(x0_den.pow(3)), x0_den);
            }
        }

        // if we've gotten here, then g(X0(t)) is not square. convert srqt_candidate to sqrt(g(X1(t)))
        FP2Immutable x1_num = Constants.xi_2.mul(tPow2).mul(x0_num);
        FP2Immutable x1_den = x0_den;
        FP2Immutable tPow3 = tPow2.mul(t);
        FP2Immutable tPow6 = tPow3.sqr();
        FP2Immutable gx1_num = Constants.xi_2Pow3.mul(tPow6).mul(gx0_num);
        FP2Immutable gx1_den = gx0_den;
        sqrt_candidate = sqrt_candidate.mul(tPow3);
        for (FP2Immutable eta : Constants.etas) {
            FP2Immutable y1 = eta.mul(sqrt_candidate);
            if (y1.sqr().mul(gx1_den).equals(gx1_num)) {
                // found sqrt(g(X1(t))). force sign of y to equal sign of t
                if (t.sgn0() != y1.sgn0()) {
                    y1 = y1.neg();
                }
                return new JacobianPoint(x1_num.mul(x1_den), y1.mul(x1_den.pow(3)), x1_den);
            }
        }

        // Should never be reached
        throw new RuntimeException("mapToCurve failed for unknown reasons.");
    }


    public static JacobianPoint iso3(JacobianPoint p) {
        FP2Immutable x = new FP2Immutable(p.getX());
        FP2Immutable y = new FP2Immutable(p.getY());
        FP2Immutable z = new FP2Immutable(p.getZ());
        FP2Immutable[] mapvals = new FP2Immutable[4];

        // precompute the required powers of Z^2
        final FP2Immutable[] zpows = new FP2Immutable[4];
        zpows[0] = ONE;
        zpows[1] = z.sqr();
        zpows[2] = zpows[1].sqr();
        zpows[3] = zpows[2].mul(zpows[1]);

        // compute the numerator and denominator of the X and Y maps via Horner's rule
        FP2Immutable[] coeffs_z = new FP2Immutable[4];
        for (int idx = 0; idx < Constants.map_coeffs.length; idx++) {
            FP2Immutable[] coeffs = Constants.map_coeffs[idx];
            coeffs_z[0] = coeffs[coeffs.length - 1];
            for (int j = 1; j < coeffs.length; j++) {
                coeffs_z[j] = coeffs[coeffs.length - j - 1].mul(zpows[j]);
            }
            FP2Immutable tmp = coeffs_z[0];
            for (int j = 1; j < coeffs.length; j++) {
                tmp = tmp.mul(x).add(coeffs_z[j]);
            }
            mapvals[idx] = tmp;
        }

        // xden is of order 1 less than xnum, so need to multiply it by an extra factor of Z^2
        mapvals[1] = mapvals[1].mul(zpows[1]);

        // multiply result of Y map by the y-coordinate y / z^3
        mapvals[2] = mapvals[2].mul(y);
        mapvals[3] = mapvals[3].mul(z.pow(3));

        FP2Immutable zz = mapvals[1].mul(mapvals[3]);
        FP2Immutable xx = mapvals[0].mul(mapvals[3]).mul(zz);
        FP2Immutable yy = mapvals[2].mul(mapvals[1]).mul(zz.sqr());

        return new JacobianPoint(xx, yy, zz);
    }


    private static FP2Immutable qi_x(FP2Immutable x) {
        FP a = new FP(x.getFp2().getA());
        FP b = new FP(x.getFp2().getB());
        a.mul(k_qi_x);
        b.mul(k_qi_x);
        b.neg();
        return new FP2Immutable(new FP2(a, b));
    }


    private static FP2Immutable qi_y(FP2Immutable y) {
        FP y0 = new FP(y.getFp2().getA());
        FP y1 = new FP(y.getFp2().getB());
        FP a = new FP(y0);
        FP b = new FP(y0);
        a.add(y1);
        a.mul(k_qi_y);
        b.sub(y1);
        b.mul(k_qi_y);
        return new FP2Immutable(new FP2(a, b));
    }


    @VisibleForTesting
    public static JacobianPoint psi(JacobianPoint p) {
        FP2Immutable x = p.getX();
        FP2Immutable y = p.getY();
        FP2Immutable z = p.getZ();

        FP2Immutable z2 = z.sqr();
        FP2Immutable px = k_cx.mul(qi_x(iwsc.mul(x)));
        FP2Immutable pz2 = qi_x(iwsc.mul(z2));
        FP2Immutable py = k_cy.mul(qi_y(iwsc.mul(y)));
        FP2Immutable pz3 = qi_y(iwsc.mul(z2).mul(z));

        FP2Immutable zOut = pz2.mul(pz3);
        FP2Immutable xOut = px.mul(pz3).mul(zOut);
        FP2Immutable yOut = py.mul(pz2).mul(zOut.sqr());

        return new JacobianPoint(xOut, yOut, zOut);
    }


    @VisibleForTesting
    public static JacobianPoint psi2(JacobianPoint p) {
        return new JacobianPoint(p.getX().mul(k_cx_abs), p.getY().neg(), p.getZ());
    }


    public static JacobianPoint clearH2(JacobianPoint p) {
        // (-x + 1) P
        JacobianPoint work = mxChain(p).add(p);
        // -psi(P)
        JacobianPoint minus_psi_p = psi(p).neg();
        // (-x + 1) P - psi(P)
        work = work.add(minus_psi_p);
        // (x^2 - x) P + x psi(P)
        work = mxChain(work);
        // (x^2 - x) P + (x - 1) psi(P)
        work = work.add(minus_psi_p);
        // (x^2 - x - 1) P + (x - 1) psi(P)
        work = work.add(p.neg());
        // psi(psi(2P))
        JacobianPoint psi_psi_2p = psi2(p.dbl());
        // (x^2 - x - 1) P + (x - 1) psi(P) + psi(psi(2P))
        work = work.add(psi_psi_2p);

        return work;
    }
}
