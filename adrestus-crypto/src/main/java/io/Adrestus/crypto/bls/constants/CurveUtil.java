package io.Adrestus.crypto.bls.constants;

import io.Adrestus.crypto.bls.BLS381.BIG;
import io.Adrestus.crypto.bls.BLS381.FP;
import io.Adrestus.crypto.bls.BLS381.ROM;
import io.Adrestus.crypto.bls.model.FieldElement;
import org.apache.tuweni.bytes.Bytes;

import static com.google.common.base.Preconditions.checkArgument;

public class CurveUtil {
    public static final BIG P = new BIG(ROM.Modulus);
    public static final FieldElement curveOrder = new FieldElement(new BIG(ROM.CURVE_Order));
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
