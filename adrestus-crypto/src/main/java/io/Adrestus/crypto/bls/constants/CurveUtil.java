package io.Adrestus.crypto.bls.constants;

import io.Adrestus.crypto.bls.model.FieldElement;
import io.Adrestus.crypto.bls.model.G1Point;
import io.Adrestus.crypto.bls.model.G2Point;
import org.apache.milagro.amcl.BLS381.*;
import org.apache.tuweni.bytes.Bytes;

import static com.google.common.base.Preconditions.checkArgument;

public class CurveUtil {
    public static final BIG P = new BIG(ROM.Modulus);
    public static final FieldElement curveOrder = new FieldElement(new BIG(ROM.CURVE_Order));
    public static final G1Point g1Generator = new G1Point(ECP.generator());
    public static final G2Point g2Generator = new G2Point(ECP2.generator());
    private static final int SIZE_OF_BIG = BIG.MODBYTES;

    // The field modulus


    public static BIG bigFromHex(String hex) {
        checkArgument(
                hex.length() == 2 + 2 * SIZE_OF_BIG,
                "Expected %s chars, received %s.",
                2 + 2 * SIZE_OF_BIG,
                hex.length());
        return BIG.fromBytes(Bytes.fromHexString(hex).toArray());
    }


    public static FP fpFromHex(String hex) {
        return new FP(bigFromHex(hex));
    }

    public static FP negate(FP a) {
        FP aNeg = new FP(a);
        aNeg.neg();
        return aNeg;
    }

    public static FP os2ip_modP(Bytes b) {
        return new FP(new DBIGExtended(b.toArray()).mod(P));
    }
}
