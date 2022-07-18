package io.Adrestus.crypto.bls.model;


import io.Adrestus.crypto.bls.constants.FP2Immutable;
import org.apache.milagro.amcl.BLS381.ECP2;
import org.apache.tuweni.bytes.Bytes;

import java.nio.charset.StandardCharsets;

import static io.Adrestus.crypto.bls.utils.Helper.*;

public class HashToCurve {

    private static final Bytes ETH2_DST = Bytes.wrap("BLS_SIG_BLS12381G2_XMD:SHA-256_SSWU_RO_POP_".getBytes(StandardCharsets.US_ASCII));

    public static boolean isInGroupG2(ECP2 point) {
        return isInG2(new JacobianPoint(point));
    }

    public static ECP2 hashToG2(Bytes message, Bytes dst) {

        FP2Immutable[] u = hashToField(message, 2, dst);

        JacobianPoint q0 = mapToCurve(u[0]);
        JacobianPoint q1 = mapToCurve(u[1]);

        JacobianPoint r = iso3(q0.add(q1));

        // This should never fail, and the check is non-trivial, so we use an assert
        assert isOnCurve(r);

        JacobianPoint p = clearH2(r);

        // This should never fail, and the check is very expensive, so we use an assert
        assert isInG2(p);

        return p.toECP2();
    }

    public static ECP2 hashToG2(Bytes message) {
        return hashToG2(message, ETH2_DST);
    }
}
